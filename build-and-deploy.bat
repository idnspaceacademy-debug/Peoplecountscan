@echo off
REM Script untuk Build APK dan Copy ke Public Folder (Windows)
REM Usage: build-and-deploy.bat

echo.
echo ^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*
echo People Counting - Build ^& Deploy Script (Windows)
echo ^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*^*
echo.

REM Step 1: Build APK
echo 📦 Step 1: Building Release APK...
echo.
call gradlew.bat assembleRelease

if errorlevel 1 (
    echo.
    echo ❌ Error: Build failed!
    pause
    exit /b 1
)

REM Step 2: Check if APK exists
echo.
echo 🔍 Step 2: Checking APK build...
if not exist "app\build\outputs\apk\release\app-release.apk" (
    echo ❌ Error: APK not found at app\build\outputs\apk\release\app-release.apk
    pause
    exit /b 1
)
echo ✅ APK found: app\build\outputs\apk\release\app-release.apk

REM Step 3: Copy to public folder
echo.
echo 📋 Step 3: Copying APK to public folder...
copy /Y "app\build\outputs\apk\release\app-release.apk" "public\app-release.apk"
if errorlevel 1 (
    echo ❌ Error: Failed to copy APK
    pause
    exit /b 1
)
echo ✅ APK copied to public\app-release.apk

REM Step 4: Show file info
echo.
echo 📊 Step 4: File information:
dir /s "public\app-release.apk"

REM Step 5: Ready for deployment
echo.
echo ✨ Step 5: Ready for deployment!
echo.
echo Next steps:
echo   1. git add public/app-release.apk
echo   2. git commit -m "Update APK to latest build"
echo   3. git push origin main
echo   4. Vercel will auto-deploy!
echo.
echo ✅ Build ^& Deploy Script Completed!
echo.
pause
