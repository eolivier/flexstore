#!/usr/bin/env bash
# Generates docs/database.png from the Mermaid diagram of docs/database.md (needs Node.js; the first run downloads
# the Mermaid CLI). Set PUPPETEER_EXECUTABLE_PATH to use an installed Chrome instead of the one Puppeteer downloads.
set -euo pipefail

docs_dir="$(cd "$(dirname "$0")" && pwd)"
work_dir="$(mktemp -d)"
trap 'rm -rf "$work_dir"' EXIT

# The diagram: the lines between ```mermaid and the next ```
awk '/^```mermaid$/ { inside = 1; next } /^```$/ { inside = 0 } inside' "$docs_dir/database.md" > "$work_dir/database.mmd"

puppeteer_config="$work_dir/puppeteer.json"
if [[ -n "${PUPPETEER_EXECUTABLE_PATH:-}" ]]; then
  printf '{"executablePath": "%s", "args": ["--no-sandbox"]}\n' "$PUPPETEER_EXECUTABLE_PATH" > "$puppeteer_config"
else
  printf '{"args": ["--no-sandbox"]}\n' > "$puppeteer_config"
fi

npx -y @mermaid-js/mermaid-cli@11.4.2 -p "$puppeteer_config" \
  -i "$work_dir/database.mmd" -o "$docs_dir/database.png" -b white -w 2400 -s 2
echo "Generated $docs_dir/database.png"
