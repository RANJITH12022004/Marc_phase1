@echo off
echo === MARC BUILD SYSTEM ===
echo Cleaning...
call gradlew.bat clean
echo Building...
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo BUILD FAILED - check errors above
    exit /b 1
)
echo Installing to phone...
adb -s 00255662M008297 install -r app\build\outputs\apk\debug\app-debug.apk
echo Launching...
adb -s 00255662M008297 shell am start -n com.marc.helmet/.activities.SplashActivity
echo === DONE ===