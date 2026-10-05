#!/usr/bin/env python3
"""Daily Mi Fitness data collection.

Pulls the last few days of data from the Mi Fitness cloud into a local SQLite
database (via mi-fitness-data-bridge), then refreshes CSV exports and writes a
dated JSON snapshot. Meant to be run once a day by cron, a systemd timer,
launchd or Windows Task Scheduler.

Re-fetching an overlapping window every run is deliberate: the watch/band often
syncs to the phone (and the phone to the cloud) hours late, so yesterday's
numbers keep changing for a while. The bridge upserts, so overlap is safe.
"""

from __future__ import annotations

import argparse
import contextlib
import logging
import os
import subprocess
import sys
import time
from datetime import date, datetime, timedelta
from pathlib import Path
from zoneinfo import ZoneInfo

HERE = Path(__file__).resolve().parent
SHIM = HERE / "bridge_shim.py"

DEFAULT_DATA_DIR = Path(os.environ.get("MI_FITNESS_DATA_DIR", Path.home() / "mi-fitness-data"))

log = logging.getLogger("mi_fitness_daily")


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    p = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    p.add_argument(
        "--data-dir",
        type=Path,
        default=DEFAULT_DATA_DIR,
        help="Where the database, exports and logs live (default: %(default)s, "
        "or $MI_FITNESS_DATA_DIR)",
    )
    p.add_argument(
        "--days-back",
        type=int,
        default=int(os.environ.get("MI_FITNESS_DAYS_BACK", 3)),
        help="Re-sync this many days before today to catch late uploads (default: %(default)s)",
    )
    p.add_argument(
        "--start-date",
        help="Override the window start (YYYY-MM-DD), e.g. for a one-off backfill",
    )
    p.add_argument(
        "--end-date",
        help="Override the window end (YYYY-MM-DD); defaults to today",
    )
    p.add_argument(
        "--timezone",
        default=os.environ.get("MI_FITNESS_TZ", "UTC"),
        help="IANA timezone used to decide what 'today' is (default: %(default)s, "
        "or $MI_FITNESS_TZ)",
    )
    p.add_argument(
        "--type",
        action="append",
        dest="types",
        help="Only sync this data type (repeatable). Default: everything the account supports",
    )
    p.add_argument(
        "--retries",
        type=int,
        default=3,
        help="Attempts for the sync step before giving up (default: %(default)s)",
    )
    p.add_argument(
        "--no-export",
        action="store_true",
        help="Only update the SQLite database, skip CSV/JSON exports",
    )
    p.add_argument(
        "--dry-run",
        action="store_true",
        help="Print the commands that would run and exit",
    )
    return p.parse_args(argv)


def sync_window(args: argparse.Namespace) -> tuple[date, date]:
    today = datetime.now(ZoneInfo(args.timezone)).date()
    end = date.fromisoformat(args.end_date) if args.end_date else today
    if args.start_date:
        start = date.fromisoformat(args.start_date)
    else:
        start = end - timedelta(days=max(args.days_back, 0))
    if start > end:
        raise SystemExit(f"start date {start} is after end date {end}")
    return start, end


def bridge(*bridge_args: str, dry_run: bool = False) -> int:
    """Run a mi-fitness-bridge subcommand through the credential shim."""
    cmd = [sys.executable, str(SHIM), *bridge_args]
    log.info("$ mi-fitness-bridge %s", " ".join(bridge_args))
    if dry_run:
        return 0
    proc = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    for line in (proc.stdout + proc.stderr).splitlines():
        if line.strip():
            log.info("  %s", line)
    return proc.returncode


def run_sync(args: argparse.Namespace, db: Path, start: date, end: date) -> bool:
    window = ["--start-date", start.isoformat(), "--end-date", end.isoformat(), "--db", str(db)]
    types = args.types or [None]  # None: let the bridge sync every supported type
    jobs = [["sync", *(["--type", t] if t else []), *window] for t in types]

    ok = True
    for job in jobs:
        for attempt in range(1, args.retries + 1):
            if bridge(*job, dry_run=args.dry_run) == 0:
                break
            if attempt < args.retries:
                delay = 30 * 2 ** (attempt - 1)
                log.warning(
                    "sync attempt %d/%d failed, retrying in %ds", attempt, args.retries, delay
                )
                time.sleep(delay)
        else:
            log.error("sync failed after %d attempts: %s", args.retries, " ".join(job))
            ok = False
    return ok


def run_export(args: argparse.Namespace, db: Path, start: date, end: date) -> bool:
    exports = args.data_dir / "exports"
    snapshot = exports / "daily" / f"{end.isoformat()}.json"
    snapshot.parent.mkdir(parents=True, exist_ok=True)

    # Full-history CSVs, rewritten each day so they are always complete.
    csv_rc = bridge(
        "export", "--format", "csv", "--output", str(exports / "csv"), "--db", str(db),
        dry_run=args.dry_run,
    )
    # A small JSON snapshot of just the window that was synced today.
    json_rc = bridge(
        "export", "--format", "json", "--output", str(snapshot), "--db", str(db),
        "--start-date", start.isoformat(), "--end-date", end.isoformat(),
        dry_run=args.dry_run,
    )
    return csv_rc == 0 and json_rc == 0


@contextlib.contextmanager
def single_instance(lock_path: Path):
    """Skip the run if the previous one is still going (e.g. stuck on the network)."""
    try:
        import fcntl
    except ImportError:  # Windows: Task Scheduler already prevents overlapping runs
        yield
        return
    with open(lock_path, "w") as fh:
        try:
            fcntl.flock(fh, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError:
            raise SystemExit(f"another run holds {lock_path}; exiting") from None
        yield


def setup_logging(data_dir: Path) -> None:
    log_dir = data_dir / "logs"
    log_dir.mkdir(parents=True, exist_ok=True)
    fmt = logging.Formatter("%(asctime)s %(levelname)s %(message)s")
    file_handler = logging.FileHandler(log_dir / "daily_sync.log", encoding="utf-8")
    stream_handler = logging.StreamHandler()
    for handler in (file_handler, stream_handler):
        handler.setFormatter(fmt)
        log.addHandler(handler)
    log.setLevel(logging.INFO)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    args.data_dir.mkdir(parents=True, exist_ok=True)
    with contextlib.suppress(OSError):
        args.data_dir.chmod(0o700)  # health data: keep it private on shared machines
    setup_logging(args.data_dir)

    start, end = sync_window(args)
    db = args.data_dir / "mi_fitness.db"
    log.info("collecting Mi Fitness data for %s .. %s into %s", start, end, args.data_dir)

    with single_instance(args.data_dir / ".daily_sync.lock"):
        synced = run_sync(args, db, start, end)
        # Export even after a partial sync failure: whatever did land is still useful.
        exported = True if args.no_export else run_export(args, db, start, end)

    if synced and exported:
        log.info("done")
        return 0
    log.error("finished with errors (sync ok=%s, export ok=%s)", synced, exported)
    return 1


if __name__ == "__main__":
    sys.exit(main())
