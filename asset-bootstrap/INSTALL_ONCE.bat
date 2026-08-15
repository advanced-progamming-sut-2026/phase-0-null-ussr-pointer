@echo off
setlocal EnableExtensions
cd /d "%~dp0.."

set "BOOTSTRAP=asset-bootstrap"
set "INSTALLER=%BOOTSTRAP%\install_custom_assets.py"

echo ==========================================
echo PvZ Custom Assets - One Time Installer
echo Windows
echo ==========================================
echo.

REM 1) Verify repository
git rev-parse --is-inside-work-tree >nul 2>&1
if errorlevel 1 (
    echo [ERROR] This must be run inside the Git repository.
    pause
    exit /b 1
)

REM 2) Verify required installer
if not exist "%INSTALLER%" (
    echo [ERROR] Missing %INSTALLER%
    pause
    exit /b 1
)

REM 3) Install/patch assets first.
echo [1/4] Installing custom assets...
python "%INSTALLER%"
if errorlevel 1 (
    echo.
    echo [ERROR] Asset installation failed.
    echo Nothing was deleted.
    pause
    exit /b 1
)

REM 4) Ignore this bootstrap folder locally.
REM .git/info/exclude is local-only, so it does not create a Git change.
echo [2/4] Adding local Git ignore rule...
if not exist ".git\info" mkdir ".git\info"

findstr /x /c:"asset-bootstrap/" ".git\info\exclude" >nul 2>&1
if errorlevel 1 (
    >>".git\info\exclude" echo asset-bootstrap/
)

REM 5) Hide deletion of any bootstrap files that are already tracked.
echo [3/4] Marking tracked bootstrap files skip-worktree...
for /f "delims=" %%F in ('git ls-files "asset-bootstrap/*"') do (
    git update-index --skip-worktree "%%F"
)

REM 6) Delete the whole bootstrap folder after this batch file exits.
echo [4/4] Scheduling self-cleanup...
set "BOOTSTRAP_ABS=%CD%\asset-bootstrap"

start "" /b cmd /c "timeout /t 2 /nobreak >nul & rmdir /s /q \"%BOOTSTRAP_ABS%\""

echo.
echo SUCCESS.
echo Custom assets were installed into pvz-assets.
echo asset-bootstrap will now delete itself.
echo Git should remain clean for these deleted bootstrap files.
echo.
pause
endlocal
