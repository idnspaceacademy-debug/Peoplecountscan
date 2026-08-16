# Public Folder - Web Distribution

Folder ini berisi file-file yang akan di-serve oleh Vercel.

## File Structure

```
public/
├── index.html              # Halaman utama aplikasi
├── app-release.apk         # APK Android (copy ke sini dari build)
└── README.md              # File ini
```

## APK Placement

File APK harus di-tempatkan di sini sebagai `app-release.apk`.

### Cara menempatkan APK:

1. **Build APK di Android Studio:**
   ```bash
   ./gradlew assembleRelease
   ```

2. **Copy ke folder public:**
   ```bash
   cp app/build/outputs/apk/release/app-release.apk public/app-release.apk
   ```

3. **Atau gunakan file explorer:**
   - Cari file: `app/build/outputs/apk/release/app-release.apk`
   - Copy file tersebut
   - Paste di folder `public/` dengan nama `app-release.apk`

## Testing Lokal

Untuk test secara lokal sebelum deploy:

```bash
# Install http-server (if not installed)
npm install -g http-server

# Jalankan server di folder public
cd public
http-server

# Buka di browser: http://localhost:8080
```

## Vercel Deployment

Setelah APK ada di sini, push ke GitHub dan Vercel akan otomatis deploy:

```bash
git add .
git commit -m "Add APK to public folder"
git push origin main
```

File akan tersedia di:
- **Web**: `https://your-vercel-url.vercel.app`
- **APK Download**: `https://your-vercel-url.vercel.app/app-release.apk`

## Catatan

- APK harus berformat `.apk` (bukan `.aab` atau format lain)
- Ukuran file APK harus < 100MB (limit Vercel)
- Untuk release production, gunakan signed APK
