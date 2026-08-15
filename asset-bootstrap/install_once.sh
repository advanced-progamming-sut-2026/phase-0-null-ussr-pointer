#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BOOTSTRAP="$ROOT/asset-bootstrap"
INSTALLER="$BOOTSTRAP/install_custom_assets.py"

cd "$ROOT"

echo "=========================================="
echo "PvZ Custom Assets - One Time Installer"
echo "macOS / Linux"
echo "=========================================="
echo

# 1) Verify repository
if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "[ERROR] This must be run inside the Git repository."
  exit 1
fi

# 2) Verify required installer
if [ ! -f "$INSTALLER" ]; then
  echo "[ERROR] Missing $INSTALLER"
  exit 1
fi

# 3) Install/patch assets first
echo "[1/4] Installing custom assets..."
python3 "$INSTALLER"

# 4) Ignore this bootstrap folder locally.
# .git/info/exclude is local-only, so it does not create a Git change.
echo "[2/4] Adding local Git ignore rule..."
mkdir -p "$ROOT/.git/info"
touch "$ROOT/.git/info/exclude"

if ! grep -Fxq "asset-bootstrap/" "$ROOT/.git/info/exclude"; then
  printf '%s\n' "asset-bootstrap/" >> "$ROOT/.git/info/exclude"
fi

# 5) Hide deletion of any bootstrap files already tracked
echo "[3/4] Marking tracked bootstrap files skip-worktree..."
while IFS= read -r file; do
  [ -z "$file" ] && continue
  git update-index --skip-worktree "$file"
done < <(git ls-files 'asset-bootstrap/*')

# 6) Delete the whole bootstrap directory after this script exits
echo "[4/4] Scheduling self-cleanup..."
(
  sleep 2
  rm -rf "$BOOTSTRAP"
) >/dev/null 2>&1 &

echo
echo "SUCCESS."
echo "Custom assets were installed into pvz-assets."
echo "asset-bootstrap will now delete itself."
echo "Git should remain clean for these deleted bootstrap files."
