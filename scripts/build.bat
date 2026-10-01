@echo off
rem Compiles the game and its tests into out\, runs the tests, and builds dist\poker.jar.
rem Needs a JDK (17 or newer) on the PATH. Run from anywhere: scripts\build.bat
setlocal
cd /d "%~dp0.."

if exist out rmdir /s /q out
if exist dist rmdir /s /q dist
mkdir out\main out\test dist

javac -d out\main src\main\java\poker\*.java || exit /b 1
javac -cp out\main -d out\test src\test\java\poker\*.java || exit /b 1
java -cp "out\main;out\test;src\main\resources" poker.PokerTests || exit /b 1
jar --create --file dist\poker.jar --main-class poker.Poker -C out\main . -C src\main\resources . || exit /b 1
echo Built dist\poker.jar
