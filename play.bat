@echo off
cd /d "%~dp0"
start "PVZ2 Server" cmd /k gradlew.bat "server.run"
timeout /t 5 /nobreak >nul
call gradlew.bat run