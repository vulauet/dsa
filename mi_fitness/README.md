# Mi Fitness daily data collection

Pulls your Mi Fitness (Xiaomi Smart Band / Watch) data from the Xiaomi cloud
once a day and keeps it on your own machine:

```
~/mi-fitness-data/
├── mi_fitness.db               # SQLite, everything ever synced (upserted, no duplicates)
├── exports/csv/*.csv           # full history per dataset, refreshed daily (opens in Excel/pandas)
├── exports/daily/YYYY-MM-DD.json  # snapshot of the window synced that day
└── logs/daily_sync.log
```

Datasets: daily activity (steps, distance, calories), heart rate, sleep and
sleep stages, workouts, SpO₂, stress, body measurements, abnormal heartbeat
events. What you actually get depends on your device and account region.

> **Heads-up:** Xiaomi has no public Mi Fitness API. This uses the unofficial
> [mi-fitness-data-bridge](https://github.com/shkyyy18/mi_fitness_data_bridge)
> client, which talks to the same endpoints as the app. It can break when
> Xiaomi changes things, and your `passToken` is effectively your Xiaomi
> account login — treat it like a password.

## How it works

`daily_sync.py` runs three steps each day:

1. `sync` the last 3 days up to today (`--days-back`). The overlap is on
   purpose: the band often reaches the cloud hours late, so recent days keep
   changing. Records are upserted, so re-syncing is harmless. Failed syncs
   are retried with backoff (30s, 60s).
2. Re-export all history to CSV.
3. Write a dated JSON snapshot of the synced window.

It exits non-zero if anything failed, so cron/systemd will flag the run.

## Setup

Needs Python 3.11+.

```bash
cd mi_fitness
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```

### 1. Get your credentials

1. In a desktop browser, sign in at <https://account.xiaomi.com> with the
   account your Mi Fitness app uses.
2. Open DevTools → Application/Storage → Cookies → `account.xiaomi.com`.
3. Copy the values of `userId` and `passToken`.

Region: `cn` for mainland-China accounts; otherwise the server your account
lives on (`sg`, `de`, `us`, `ru`, `i2` for India). If unsure, try `sg` or `de`
and check with `doctor`.

### 2. Store them — pick one

**Desktop (macOS, Windows, Linux with a desktop session)** — store in the OS keyring:

```bash
.venv/bin/mi-fitness-bridge setup     # prompts for user_id, passToken, region
.venv/bin/mi-fitness-bridge doctor    # checks the cloud connection
```

**Headless Linux / server** — cron and systemd usually can't reach the keyring,
so use an env file instead (read by `bridge_shim.py`):

```bash
mkdir -p ~/.config/mi-fitness-daily
cp deploy/credentials.env.example ~/.config/mi-fitness-daily/credentials.env
chmod 600 ~/.config/mi-fitness-daily/credentials.env
$EDITOR ~/.config/mi-fitness-daily/credentials.env
```

### 3. First run and backfill

```bash
.venv/bin/python daily_sync.py --dry-run                      # shows what would run
.venv/bin/python daily_sync.py --start-date 2025-01-01        # one-off backfill of history
.venv/bin/python daily_sync.py                                # what the scheduler runs
```

If you use the env file, load it first:
`set -a; . ~/.config/mi-fitness-daily/credentials.env; set +a`.

## Schedule it

Run it in the morning so last night's sleep has had time to upload.

### Linux — systemd user timer (recommended)

```bash
mkdir -p ~/.config/systemd/user
cp deploy/mi-fitness-daily.service deploy/mi-fitness-daily.timer ~/.config/systemd/user/
# edit the paths in the .service if the repo isn't at ~/dsa
systemctl --user daemon-reload
systemctl --user enable --now mi-fitness-daily.timer
loginctl enable-linger "$USER"     # keep running when you're logged out

systemctl --user start mi-fitness-daily.service   # run once now
journalctl --user -u mi-fitness-daily -e          # see the output
```

`Persistent=true` makes it catch up if the machine was off at 09:17.

### macOS / Linux — cron

`crontab -e`, then (adjust paths):

```cron
17 9 * * * cd $HOME/dsa/mi_fitness && .venv/bin/python daily_sync.py >/dev/null 2>&1
```

Add `set -a; . $HOME/.config/mi-fitness-daily/credentials.env; set +a;` in front
of `cd` if you use the env file. Cron skips runs while the Mac is asleep;
the next day's 3-day window covers the gap.

### Windows — Task Scheduler

```powershell
schtasks /Create /TN "Mi Fitness daily" /SC DAILY /ST 09:17 `
  /TR "C:\path\to\dsa\mi_fitness\.venv\Scripts\python.exe C:\path\to\dsa\mi_fitness\daily_sync.py"
```

Then in Task Scheduler → task Properties → Settings, tick
*Run task as soon as possible after a scheduled start is missed*.

## Options

| Flag | Env var | Default | |
| --- | --- | --- | --- |
| `--data-dir` | `MI_FITNESS_DATA_DIR` | `~/mi-fitness-data` | where db/exports/logs go |
| `--days-back` | `MI_FITNESS_DAYS_BACK` | `3` | overlap window |
| `--timezone` | `MI_FITNESS_TZ` | `UTC` | decides what "today" is |
| `--type` | | all | limit to e.g. `--type sleep --type heart_rate` |
| `--start-date` / `--end-date` | | | backfills |
| `--retries` | | `3` | sync attempts |
| `--no-export` | | | only update the database |

## Troubleshooting

- **`未找到 Mi Fitness 凭据` / "no credentials"** — the keyring isn't reachable
  from the scheduler; switch to the env file.
- **Auth errors after weeks of working** — the `passToken` expired or was
  revoked (e.g. you changed your password). Copy a fresh one.
- **Sync succeeds with 0 records** — wrong region, or the band hasn't synced
  with the phone app yet.
- Messages from the bridge are partly in Chinese; ✅ / ⚠️ / ❌ per data type
  tell you what happened.

## Privacy

Everything stays local. `daily_sync.py` makes the data dir `chmod 700`. Don't
commit the data dir, the env file, or logs — `.gitignore` covers the defaults
inside this folder, but the data dir lives in your home directory by default
for that reason. Don't run this in GitHub Actions or another shared CI: your
token would live in a third-party runner, and Xiaomi tends to challenge logins
from datacenter IPs.
