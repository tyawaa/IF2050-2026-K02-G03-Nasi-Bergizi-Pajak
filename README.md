# Nasi Bergizi Pajak

## Prasyarat

Sebelum menjalankan aplikasi ini, pastikan lingkungan Anda memenuhi hal berikut:

- Java JDK 17 atau lebih baru
- Apache Maven terpasang dan dapat diakses melalui terminal
- Sistem operasi Windows, macOS, atau Linux
- MySQL Server aktif dan dapat diakses

## Instalasi

1. Buka terminal di direktori proyek:
   ```bash
   cd "c:\Users\Tyara\Downloads\Nasi Bergizi Pajak Final\IF2050-2026-K02-G03"
   ```
2. Jalankan perintah berikut untuk mengunduh dependensi dan membangun aplikasi:
   ```bash
   ./mvnw clean install
   ```
   atau pada Windows:
   ```powershell
   .\mvnw.cmd clean install
   ```
3. Pastikan tidak ada error pada proses build sebelum melanjutkan.

## Database MySQL

Aplikasi memakai database MySQL `nasi_bergizi_pajak` secara default.

Konfigurasi default:

```text
host: localhost
port: 3306
database: nasi_bergizi_pajak
user: root
password: kosong
```

Jika password atau user MySQL berbeda, set environment variable sebelum menjalankan aplikasi:

```powershell
$env:DB_USER="root"
$env:DB_PASSWORD="password_mysql_kamu"
```

Database akan dibuat otomatis saat aplikasi berjalan jika user MySQL punya izin `CREATE DATABASE`. Untuk import data dummy, jalankan file `database/nasi_bergizi_pajak.sql` lewat MySQL.

## Cara Menjalankan Aplikasi

1. Setelah build selesai, jalankan aplikasi dengan perintah:
   ```bash
   ./mvnw javafx:run
   ```
   atau pada Windows:
   ```powershell
   .\mvnw.cmd javafx:run
   ```
2. Aplikasi akan terbuka dalam tampilan antarmuka pengguna (GUI).



