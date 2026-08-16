# 📱 People Counting - Panduan Deployment ke Vercel

## Struktur Folder Web

```
Peoplecountscan/
├── public/
│   ├── index.html          (Halaman utama)
│   └── app-release.apk     (APK Android - tempat ini)
├── vercel.json             (Konfigurasi Vercel)
└── DEPLOYMENT.md           (Panduan ini)
```

## Langkah-Langkah Deployment

### 1. Persiapan APK
Pastikan Anda sudah memiliki file APK yang sudah di-build:

**Opsi A: Build lokal menggunakan Android Studio**
```bash
# Di project root
./gradlew assembleRelease
```

APK akan tersimpan di: `app/build/outputs/apk/release/app-release.apk`

**Opsi B: Build melalui Android Studio GUI**
- Buka Android Studio
- Klik menu `Build` > `Build Bundle(s) / APK(s)` > `Build APK(s)`
- Tunggu proses build selesai
- APK akan tersimpan otomatis

### 2. Tempatkan APK di Folder Public
Setelah APK selesai di-build:

```bash
# Dari root project
cp app/build/outputs/apk/release/app-release.apk public/app-release.apk
```

### 3. Push ke GitHub
```bash
# Staging changes
git add .

# Commit
git commit -m "Add web deployment and APK download"

# Push ke repository
git push origin main
```

### 4. Deploy ke Vercel

**Metode 1: Via Dashboard Vercel (Recommended)**
1. Buka [vercel.com](https://vercel.com)
2. Login dengan GitHub account Anda
3. Klik tombol "Add New Project"
4. Pilih repository `Peoplecountscan`
5. Di "Root Directory", pilih `./` (root)
6. Klik "Deploy"
7. Tunggu proses deploy selesai (~1-3 menit)

**Metode 2: Via Vercel CLI**
```bash
# Install Vercel CLI (jika belum)
npm install -g vercel

# Deploy
vercel

# Ikuti instruksi yang muncul
```

### 5. Hasil Deployment
Setelah selesai, Anda akan mendapatkan:
- **URL Web**: `https://peoplecountscan-xxx.vercel.app`
- **APK Download**: `https://peoplecountscan-xxx.vercel.app/app-release.apk`

## Fitur Web

✅ **Halaman Informasi Aplikasi**
- Deskripsi lengkap People Counting
- Fitur-fitur utama dengan icons
- Stack teknologi yang digunakan

✅ **Tombol Download APK**
- Download langsung dari link: `/app-release.apk`
- Responsive design untuk semua device
- Syarat sistem untuk instalasi

✅ **Link Dokumentasi**
- GitHub repository
- Panduan lengkap

## Update APK

Setiap kali Anda membuat build APK baru:

```bash
# 1. Build APK baru
./gradlew assembleRelease

# 2. Copy ke folder public
cp app/build/outputs/apk/release/app-release.apk public/app-release.apk

# 3. Push ke GitHub
git add public/app-release.apk
git commit -m "Update APK to latest build"
git push origin main

# 4. Vercel akan auto-deploy (jika sudah connect ke GitHub)
```

## Troubleshooting

### APK tidak bisa di-download
- Pastikan file `public/app-release.apk` ada
- Refresh browser Anda
- Cek browser console untuk error

### Vercel deployment gagal
- Cek logs di dashboard Vercel
- Pastikan repository sudah di-push ke GitHub
- Verifikasi struktur folder `public/`

### Build APK error di lokal
- Pastikan Android SDK sudah terinstall
- Update SDK ke versi terbaru
- Pastikan `local.properties` sudah setup dengan benar
- Cek file `my-upload-key.jks` untuk signing

## Tips Penting

1. **Jangan upload keystore ke GitHub** - File `my-upload-key.jks` sudah di-gitignore
2. **APK ukuran besar** - Vercel mendukung file hingga 100MB
3. **Auto-deployment** - Setiap push ke GitHub, Vercel akan otomatis deploy
4. **Custom domain** - Anda bisa menambahkan domain custom di dashboard Vercel

## Support

Jika ada masalah:
1. Cek dokumentasi GitHub: https://github.com/idnspaceacademy-debug/Peoplecountscan
2. Buka GitHub Issues untuk pertanyaan
3. Verifikasi semua requirement sudah terpenuhi

---

**Selamat! Web Anda siap di-deploy ke Vercel dan user bisa langsung download APK dari sana! 🚀**
