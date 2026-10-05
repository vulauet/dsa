#!/usr/bin/env python3
"""Run the mi-fitness-bridge CLI, optionally taking credentials from the environment.

mi-fitness-bridge only reads credentials from the OS keyring. That works on a
desktop, but under cron/systemd on a headless Linux box there is often no
unlocked keyring, so the scheduled run fails. If MI_FITNESS_USER_ID and
MI_FITNESS_PASS_TOKEN are set (e.g. from a chmod-600 EnvironmentFile), this
shim feeds them to the bridge instead; otherwise it behaves exactly like
`mi-fitness-bridge`.

Written against mi-fitness-data-bridge v0.3.3 (see requirements.txt).
"""

import os
import sys

import mi_fitness_mcp.main as bridge
from mi_fitness_mcp.config import Config

USER_ID = os.environ.get("MI_FITNESS_USER_ID", "").strip()
PASS_TOKEN = os.environ.get("MI_FITNESS_PASS_TOKEN", "").strip()
REGION = os.environ.get("MI_FITNESS_REGION", "").strip()

if USER_ID and PASS_TOKEN:
    bridge.load_mi_fitness_token = lambda: (USER_ID, PASS_TOKEN)

    _load_config = bridge.load_config

    def _load_config_from_env() -> Config:
        # Allows skipping the interactive `setup` step entirely on a server.
        config = _load_config()
        if config.mode == "not_configured":
            config.mode = "mi_fitness_cloud"
        if REGION:
            config.region = REGION
        return config

    bridge.load_config = _load_config_from_env

if __name__ == "__main__":
    sys.argv[0] = bridge.PROGRAM_NAME
    bridge.main()
