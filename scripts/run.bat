@echo off
rem Starts the game, building it first if dist\poker.jar does not exist yet.
setlocal
cd /d "%~dp0.."
if not exist dist\poker.jar call scripts\build.bat || exit /b 1
java -jar dist\poker.jar
