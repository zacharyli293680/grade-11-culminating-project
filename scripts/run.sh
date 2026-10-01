#!/usr/bin/env sh
# Starts the game, building it first if dist/poker.jar does not exist yet.
set -e
cd "$(dirname "$0")/.."
[ -f dist/poker.jar ] || sh scripts/build.sh
java -jar dist/poker.jar
