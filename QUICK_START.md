# 🚀 Quick Start - Deploy ke Vercel

Panduan cepat deployment People Counting ke Vercel dalam 5 menit!

## Prerequisites
- Repository sudah ada di GitHub
- Android APK sudah di-build
- Vercel account (bisa sign up gratis)

## 5 Langkah Mudah

### 1️⃣ Build & Prepare APK

**Option A - Menggunakan Script (Recommended)**
```bash
# Linux/Mac
./build-and-deploy.sh

# Windows
build-and-deploy.bat
```

**Option B - Manual**
```bash
# Build
./gradlew assembleRelease

# Copy APK
cp app/build/outputs/apk/release/app-release.apk public/app-release.apk
```

### 2️⃣ Commit & Push ke GitHub

```bash
git add .
git commit -m "Add deployment setup with APK"
git push origin main
```

### 3️⃣ Buka Vercel Dashboard

Kunjungi: https://vercel.com

### 4️⃣ Import Project

1. Klik "Add New Project"
2. Pilih "Import Git Repository"
3. Cari & pilih "Peoplecountscan"
4. Klik "Import"

### 5️⃣ Configure & Deploy

1. Di halaman setup:
   - **Framework Preset**: Other
   - **Root Directory**: `.` (default)
   - **Build Command**: `echo 'No build required'`
   - **Output Directory**: `public`

2. Klik "Deploy"
3. Tunggu ~1-3 menit sampai selesai

## ✅ Done! 

Aplikasi Anda sudah live di:
```
https://peoplecountscan-xxxx.vercel.app
```

Users bisa download APK dari:
```
https://peoplecountscan-xxxx.vercel.app/app-release.apk
```

## 🔄 Update APK

Setiap kali ada build APK baru:

```bash
# Build
./gradlew assembleRelease

# Copy ke public
cp app/build/outputs/apk/release/app-release.apk public/app-release.apk

# Push ke GitHub
git add public/app-release.apk
git commit -m "Update APK"
git push origin main

# ✅ Vercel auto-deploy!
```

## 📋 Checklist

- [ ] APK sudah di-build
- [ ] APK sudah di-copy ke `public/app-release.apk`
- [ ] File sudah di-commit dan di-push
- [ ] GitHub repository terhubung dengan Vercel
- [ ] Deployment selesai

## 🆘 Troubleshooting

| Masalah | Solusi |
|---------|--------|
| APK tidak bisa di-download | Pastikan file ada di `public/app-release.apk` |
| Deploy gagal | Cek logs di Vercel dashboard |
| Repository tidak terlihat | Pastikan sudah login dengan GitHub account yang benar |

## 📚 Links

- 📖 [Full Documentation](./DEPLOYMENT.md)
- 🐙 [GitHub Repository](https://github.com/idnspaceacademy-debug/Peoplecountscan)
- 🔗 [Vercel Documentation](https://vercel.com/docs)

---

**Happy Deploying! 🎉**
