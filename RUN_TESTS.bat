@echo off
setlocal
cd /d "%~dp0"
call SETUP_ENV.bat
call gradlew.bat testDebugUnitTest assembleDebug
set "RESULT=%ERRORLEVEL%"
echo.
echo See app\build\reports\tests\testDebugUnitTest\index.html for results.
pause
exit /b %RESULT%
