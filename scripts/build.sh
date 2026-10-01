#!/usr/bin/env sh
# Compiles the game and its tests into out/, runs the tests, and builds dist/poker.jar.
# Needs a JDK (17 or newer) on the PATH. Run from anywhere: sh scripts/build.sh
set -e
cd "$(dirname "$0")/.."

case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SEP=';' ;;
  *) SEP=':' ;;
esac

rm -rf out dist
mkdir -p out/main out/test dist

javac -d out/main src/main/java/poker/*.java
javac -cp out/main -d out/test src/test/java/poker/*.java
java -cp "out/main${SEP}out/test${SEP}src/main/resources" poker.PokerTests
jar --create --file dist/poker.jar --main-class poker.Poker -C out/main . -C src/main/resources .
echo "Built dist/poker.jar"
