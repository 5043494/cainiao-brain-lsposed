#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT"
git config core.hooksPath .githooks
chmod +x .githooks/post-commit
echo "已启用 post-commit 自动同步。"
echo "远程仓库：$(git remote get-url origin 2>/dev/null || echo 尚未配置)"
echo "同步日志：$ROOT/.git/cainiao-sync.log"
