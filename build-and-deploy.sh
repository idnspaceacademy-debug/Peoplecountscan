#!/bin/bash

# Script untuk Build APK dan Copy ke Public Folder
# Usage: ./build-and-deploy.sh

set -e

echo "🔨 People Counting - Build & Deploy Script"
echo "=========================================="

# Step 1: Build APK
echo ""
echo "📦 Step 1: Building Release APK..."
./gradlew assembleRelease

# Step 2: Check if APK exists
echo ""
echo "🔍 Step 2: Checking APK build..."
APK_PATH="app/build/outputs/apk/release/app-release.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "❌ Error: APK not found at $APK_PATH"
    exit 1
fi

echo "✅ APK found: $APK_PATH"

# Step 3: Copy to public folder
echo ""
echo "📋 Step 3: Copying APK to public folder..."
cp "$APK_PATH" "public/app-release.apk"
echo "✅ APK copied to public/app-release.apk"

# Step 4: Show file info
echo ""
echo "📊 Step 4: File information:"
ls -lh public/app-release.apk
echo ""
echo "File size: $(du -h public/app-release.apk | cut -f1)"

# Step 5: Ready for deployment
echo ""
echo "✨ Step 5: Ready for deployment!"
echo ""
echo "Next steps:"
echo "  1. git add public/app-release.apk"
echo "  2. git commit -m 'Update APK to latest build'"
echo "  3. git push origin main"
echo "  4. Vercel will auto-deploy!"
echo ""
echo "✅ Build & Deploy Script Completed!"
