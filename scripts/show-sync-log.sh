#!/bin/sh
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
LOG="$ROOT/.git/cainiao-sync.log"
if [ -f "$LOG" ]; then tail -n 80 "$LOG"; else echo "尚无同步日志。"; fi
