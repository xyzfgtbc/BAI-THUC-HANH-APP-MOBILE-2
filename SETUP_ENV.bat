@echo off
rem Keep user configuration; otherwise discover Android Studio's bundled Java.
if defined JAVA_HOME goto sdk
where java >nul 2>nul
if not errorlevel 1 goto sdk
if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe" set "JAVA_HOME=%ProgramFiles%\Android\Android Studio\jbr"
if defined JAVA_HOME goto sdk
if exist "%LOCALAPPDATA%\Programs\Android Studio\jbr\bin\java.exe" set "JAVA_HOME=%LOCALAPPDATA%\Programs\Android Studio\jbr"
:sdk
if defined ANDROID_HOME exit /b 0
if exist "%LOCALAPPDATA%\Android\Sdk\platforms" set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
exit /b 0
