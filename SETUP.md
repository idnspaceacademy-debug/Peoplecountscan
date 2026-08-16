# 📦 People Counting - Deployment Setup Guide

Struktur lengkap untuk deployment web dan APK ke Vercel.

## 📁 Struktur Folder yang Telah Dibuat

```
Peoplecountscan/
│
├── 📄 QUICK_START.md              ← Mulai dari sini! (5 menit setup)
├── 📄 DEPLOYMENT.md               ← Dokumentasi lengkap
├── 📄 build-and-deploy.sh         ← Script Linux/Mac
├── 📄 build-and-deploy.bat        ← Script Windows
├── 📄 vercel.json                 ← Config Vercel
│
├── public/                        ← Folder untuk Vercel deployment
│   ├── index.html                 ← Landing page
│   ├── README.md                  ← Info folder
│   ├── .nojekyll                  ← Vercel config
│   └── app-release.apk            ← (Copy APK ke sini)
│
└── app/
    └── build/
        └── outputs/
            └── apk/
                └── release/
                    └── app-release.apk    ← Source APK (hasil build)
```

## 🎯 Workflow Deployment

### Untuk First Time Setup:

1. **Setup Repository**
   ```bash
   # Pastikan di branch main
   git checkout main
   git pull origin main
   ```

2. **Build APK**
   ```bash
   # Linux/Mac
   ./build-and-deploy.sh
   
   # Windows
   build-and-deploy.bat
   
   # Atau manual
   ./gradlew assembleRelease
   cp app/build/outputs/apk/release/app-release.apk public/app-release.apk
   ```

3. **Push ke GitHub**
   ```bash
   git add .
   git commit -m "Initial deployment setup"
   git push origin main
   ```

4. **Setup di Vercel**
   - Buka vercel.com
   - Login dengan GitHub
   - Import repository "Peoplecountscan"
   - Klik Deploy
   - Selesai! 🎉

### Untuk Update APK:

```bash
# 1. Build APK baru
./gradlew assembleRelease

# 2. Copy ke public
cp app/build/outputs/apk/release/app-release.apk public/app-release.apk

# 3. Commit & Push
git add public/app-release.apk
git commit -m "Update APK to v[version]"
git push origin main

# 4. ✅ Vercel auto-deploy dalam 1-2 menit!
```

## 📋 File yang Dibuat

| File | Fungsi |
|------|--------|
| `public/index.html` | Landing page dengan info & download button |
| `public/README.md` | Dokumentasi folder public |
| `public/.nojekyll` | Signal ke Vercel untuk static site |
| `vercel.json` | Konfigurasi Vercel (build, headers, routing) |
| `QUICK_START.md` | Panduan cepat (5 menit) |
| `DEPLOYMENT.md` | Dokumentasi lengkap deployment |
| `build-and-deploy.sh` | Script otomatis (Linux/Mac) |
| `build-and-deploy.bat` | Script otomatis (Windows) |

## 🔐 Security Notes

✅ **Sudah Aman:**
- Keystore file (`my-upload-key.jks`) di-ignore di `.gitignore`
- Environment variables di `.env.example` (template saja)
- API keys tidak di-hardcode

⚠️ **Yang Perlu Diperhatian:**
- APK yang di-commit adalah signed release APK
- Jangan commit debug keystore
- Pastikan `.env` tidak di-push (sudah di-gitignore)

## 🌐 Hasil Deployment

Setelah deployment selesai, user akan bisa akses:

```
Halaman Utama:    https://peoplecountscan-xxx.vercel.app/
Download APK:     https://peoplecountscan-xxx.vercel.app/app-release.apk
GitHub Repo:      https://github.com/idnspaceacademy-debug/Peoplecountscan
```

## 📊 Performance Tips

1. **Optimize APK Size** - Gunakan R8/ProGuard
2. **Cache Strategy** - Vercel sudah menangani caching otomatis
3. **CDN** - Vercel menggunakan global CDN
4. **Custom Domain** - Setup di Vercel dashboard

## 🆘 Troubleshooting

### Build Error
```bash
# Clean & rebuild
./gradlew clean
./gradlew assembleRelease
```

### APK tidak terdownload
- Periksa `public/app-release.apk` exists
- Refresh browser cache (Ctrl+Shift+R)
- Check Vercel logs

### Deployment stuck
- Cek GitHub push berhasil
- Tunggu 2-3 menit untuk auto-redeploy
- Redeploy manual di Vercel dashboard

## 📚 Learn More

- 🔗 [Vercel Documentation](https://vercel.com/docs)
- 📖 [Android APK Signing](https://developer.android.com/studio/publish/app-signing)
- 🐙 [GitHub Guides](https://guides.github.com/)

---

## ✅ Checklist Completion

- [x] Folder `public/` dibuat dengan `index.html`
- [x] APK download link tersedia
- [x] `vercel.json` dikonfigurasi
- [x] Script build otomatis dibuat (Bash & Batch)
- [x] Dokumentasi lengkap dibuat
- [x] GitHub ready untuk push
- [x] Siap untuk Vercel deployment

**Status: ✅ Siap untuk deployment!**

Untuk memulai, baca [QUICK_START.md](./QUICK_START.md) 🚀
