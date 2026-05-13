# Nasi Bergizi Pajak

Nasi Bergizi Pajak adalah aplikasi desktop berbasis JavaFX yang membantu pengguna mengelola perencanaan makanan keluarga secara lebih sehat dan hemat. Aplikasi ini menyediakan fitur pengelolaan akun, profil keluarga, budget makanan, stok dapur, menu mingguan, rekomendasi menu, serta pengelolaan data resep dan bahan makanan oleh admin. Sistem menggunakan MySQL sebagai basis data utama untuk menyimpan data pengguna, resep, stok, harga bahan, menu, dan rencana belanja.

---

## Prasyarat dan Instalasi

### Prasyarat

Sebelum menjalankan aplikasi, pastikan perangkat sudah memiliki:

- Java JDK 21 atau versi yang kompatibel dengan konfigurasi Maven proyek
- Apache Maven atau Maven Wrapper bawaan proyek
- MySQL Server
- Git, jika proyek dijalankan dari repository
- IDE Java, misalnya IntelliJ IDEA, Visual Studio Code, atau IDE lain yang mendukung Maven

### Instalasi

1. Clone repository atau buka folder proyek.

   ```bash
   git clone <url-repository>
   cd IF2050-2026-K02-G03-Nasi-Bergizi-Pajak
   ```

2. Pastikan MySQL Server sudah aktif.

3. Buat dan isi database menggunakan salah satu file SQL yang tersedia pada folder `database`.

   ```bash
   mysql -u root -p < database/init.sql
   ```

   Jika ingin menggunakan data dummy yang sudah disiapkan, jalankan juga file SQL dummy atau script yang tersedia di folder `database` sesuai kebutuhan proyek.

4. Sesuaikan konfigurasi koneksi database pada file:

   ```text
   src/main/java/nasi_bergizi_pajak/config/DatabaseConnection.java
   ```

   Konfigurasi default yang digunakan aplikasi:

   ```text
   host     : localhost
   port     : 3306
   database : nasi_bergizi_pajak
   user     : root
   password : kosong / sesuai konfigurasi lokal
   ```

5. Build project menggunakan Maven Wrapper.

   Untuk Windows:

   ```powershell
   .\mvnw.cmd clean install
   ```

   Untuk macOS/Linux:

   ```bash
   ./mvnw clean install
   ```

---

## Cara Menjalankan Aplikasi

Jalankan aplikasi melalui Maven Wrapper:

Untuk Windows:

```powershell
.\mvnw.cmd javafx:run
```

Untuk macOS/Linux:

```bash
./mvnw javafx:run
```

Aplikasi akan membuka tampilan GUI JavaFX. Pengguna dapat melakukan registrasi/login, sedangkan akun admin dapat mengakses halaman pengelolaan data resep serta bahan dan harga.

---

## Struktur Proyek

```text
IF2050-2026-K02-G03-Nasi-Bergizi-Pajak/
├── database/
│   ├── init.sql
│   ├── nasi_bergizi_pajak.sql
│   └── dummy.py
├── img/
├── src/main/java/nasi_bergizi_pajak/
│   ├── app/
│   ├── config/
│   ├── controller/
│   ├── dao/
│   ├── model/
│   ├── service/
│   ├── util/
│   └── Main.java
├── src/main/resources/
│   ├── auth.css
│   └── view/
├── pom.xml
├── mvnw
└── mvnw.cmd
```

---

## Daftar Modul yang Diimplementasi

| No | Nama Modul | Deskripsi Singkat | Kelas Utama / Pembagian Komponen |
|---:|---|---|---|
| 1 | Autentikasi Akun | Menangani registrasi, login, validasi akun, role pengguna/admin, dan proses keluar dari aplikasi. | `LoginController`, `RegistrasiController`, `AkunDAO`, `Akun`, `PasswordUtil`, `ValidationUtil`, `LoginView.fxml`, `RegisterView.fxml` |
| 2 | Dashboard Pengguna | Menampilkan navigasi utama, ringkasan profil, budget, stok dapur, menu mingguan, dan notifikasi penting. | `DashboardController`, `AppNavigator`, `Notification`, `DashboardView.fxml` |
| 3 | Profil Keluarga | Mengelola data anggota keluarga yang digunakan dalam perhitungan kebutuhan gizi. | `FamilyMemberDAO`, `FamilyMember`, bagian profil pada `DashboardController` |
| 4 | Budget Makanan | Mengelola anggaran makanan pengguna berdasarkan periode tertentu dan menghitung status/sisa budget. | `BudgetController`, `BudgetService`, `BudgetDAO`, `Budget` |
| 5 | Parameter Planner | Menyimpan parameter perencanaan seperti periode belanja, budget terpilih, jumlah makan per hari, dan snack per hari. | Tabel `parameter_planner`, integrasi pada dashboard/menu planner |
| 6 | Stok Dapur | Menambah, mengubah, menghapus, dan memantau stok bahan makanan pengguna, termasuk stok rendah dan bahan mendekati kadaluarsa. | `KitchenStockController`, `KitchenStockViewController`, `KitchenStockService`, `KitchenStockDAO`, `KitchenStock`, `KitchenStockView.fxml`, `KitchenStockContent.fxml` |
| 7 | Menu Mingguan | Menyusun dan mengubah menu mingguan berdasarkan slot makan, resep, tanggal, serta status makan di luar. | `MenuController`, `MenuMingguanDAO`, `MenuMingguan`, `SlotMakan` |
| 8 | Rekomendasi Menu | Menghasilkan rekomendasi menu berdasarkan data resep, gizi, stok dapur, budget, dan alergi pengguna. | `RekomendasiController`, `RekomendasiDAO`, `RekomendasiMenu`, `KebutuhanGizi`, `GiziDAO` |
| 9 | Kelola Resep Admin | Admin dapat melihat, menambah, mengubah, dan menghapus data resep beserta bahan penyusunnya. | `AdminDashboardController`, `RecipeFormController`, `RecipeDAO`, `RecipeIngredientDAO`, `Recipe`, `RecipeIngredient`, `RecipeFormView.fxml`, `AdminDashboardView.fxml` |
| 10 | Kelola Bahan, Nutrisi, dan Harga Admin | Admin dapat mengelola data master bahan makanan, kandungan nutrisi, serta riwayat harga bahan. | `IngredientController`, `IngredientService`, `IngredientDAO`, `IngredientNutritionDAO`, `IngredientPriceDAO`, `Ingredient`, `IngredientNutrition`, `IngredientPrice` |
| 11 | Koneksi dan Inisialisasi Database | Menangani koneksi aplikasi Java ke MySQL dan inisialisasi database. | `DatabaseConnection`, `DatabaseInitializer`, `database/init.sql`, `database/nasi_bergizi_pajak.sql` |

### Modul dalam Proses

| Nama Modul | Status | Keterangan |
|---|---|---|
| Shopping Planner / Daftar Belanja | Dalam proses pengembangan | Tabel `shopping_planner` dan `shopping_planner_item` sudah tersedia pada database, tetapi implementasi penuh controller, model, dan tampilan masih dapat dilengkapi setelah modul selesai. |

---

## Daftar Tabel Basis Data yang Diimplementasi

| No | Nama Tabel | Atribut |
|---:|---|---|
| 1 | `user_account` | `user_id`, `email`, `password`, `first_name`, `last_name`, `active`, `signup_datetime`, `profile_image_name`, `tipe_admin` |
| 2 | `family_member` | `member_id`, `user_id`, `name`, `relationship`, `birth_date`, `height`, `weight`, `allergy` |
| 3 | `budget` | `budget_id`, `user_id`, `name`, `amount`, `period_start`, `period_end`, `status` |
| 4 | `parameter_planner` | `parameter_id`, `user_id`, `budget_id`, `shopping_period_start`, `shopping_period_end`, `meals_per_day`, `snack_per_day` |
| 5 | `weekly_menu` | `menu_id`, `user_id`, `parameter_id`, `week_start_date`, `week_end_date`, `total_estimation`, `status_budget`, `created_datetime` |
| 6 | `ingredient` | `ingredient_id`, `name`, `unit` |
| 7 | `ingredient_nutrition` | `nutrition_id`, `ingredient_id`, `calories`, `protein`, `carbohydrate`, `fat`, `fibre`, `unit` |
| 8 | `ingredient_price` | `price_id`, `ingredient_id`, `price`, `effective_date` |
| 9 | `kitchen_stock` | `stock_id`, `user_id`, `ingredient_id`, `quantity`, `initial_quantity`, `unit`, `storage_location`, `expiry_date` |
| 10 | `recipe` | `recipe_id`, `name`, `description`, `serving_size`, `status` |
| 11 | `recipe_ingredient` | `recipe_ingredient_id`, `recipe_id`, `ingredient_id`, `amount`, `unit` |
| 12 | `meal_slot` | `slot_id`, `menu_id`, `recipe_id`, `meal_date`, `meal_time`, `is_eating_out`, `outside_cost` |
| 13 | `shopping_planner` | `planner_id`, `menu_id`, `created_datetime`, `total_estimation`, `total_actual`, `status` |
| 14 | `shopping_planner_item` | `item_id`, `planner_id`, `ingredient_id`, `required_qty`, `unit`, `estimated_price`, `actual_price`, `status_beli` |

### Relasi Utama Basis Data

- `user_account` menjadi tabel utama untuk data pengguna.
- `family_member`, `budget`, `parameter_planner`, `weekly_menu`, dan `kitchen_stock` terhubung ke `user_account` melalui `user_id`.
- `parameter_planner` menggunakan `budget_id` untuk menghubungkan perencanaan dengan budget aktif.
- `weekly_menu` terhubung ke `parameter_planner` melalui `parameter_id`.
- `meal_slot` menyimpan detail slot makan dari `weekly_menu` dan dapat terhubung ke `recipe`.
- `recipe_ingredient` menghubungkan resep dengan bahan makanan.
- `ingredient_nutrition`, `ingredient_price`, dan `kitchen_stock` menggunakan `ingredient_id` dari tabel `ingredient`.
- `shopping_planner` terhubung ke `weekly_menu`, sedangkan `shopping_planner_item` terhubung ke `shopping_planner` dan `ingredient`.

---

## Pembagian Tugas Implementasi

| Anggota | Modul / Tanggung Jawab | Kelas / Bagian yang Diimplementasikan |
|---|---|---|
| Daniel Wicaksono Godjali | Dashboard User, Admin Resep, Integrasi, Belanja/Shopping Planner, Reviewer, Database, GUI Shopping | `DashboardController`, `HalamanDashboard`, `KelolaResepController`, `HalamanKelolaResep`, `Resep`, `ShoppingPlanner`, bagian integrasi antarmodul, review implementasi, serta dukungan implementasi database |
| Tyara Penelope Lumban Gaol | Login, Register, Akun/Profile, Settings, Profil Keluarga, Parameter Planner, Integrasi, Reviewer, Database, Setup Awal Project, Backend Admin Nutrisi Bahan Harga | `FormLogin`, `LoginController`, `FormRegistrasi`, `RegistrasiController`, `Akun`, `ProfilKeluarga`, `ParameterPlanner`, `IngredientDAO`, `IngredientNutritionDAO`, `IngredientPriceDAO`, `Ingredient`, `IngredientNutrition`, `IngredientPrice`, `BahanMakanan` bagian pengaturan akun/profile/settings, setup awal struktur project, integrasi antarmodul, review implementasi, serta dukungan implementasi database |
| Adham Sachadeva Purwadi | Budget dan Belanja/Shopping Planner | `Budget`, `ShoppingPlanner`, `ShoppingPlannerController`, serta fitur perhitungan kebutuhan belanja berdasarkan menu dan stok |
| Muhammad Reyna Athallah Agoes | Menu Mingguan dan Rekomendasi Menu | `HalamanMenuMingguan`, `MenuController`, `MenuMingguan`, `HalamanRekomendasi`, `RekomendasiController`, `Resep`, serta logika rekomendasi menu berdasarkan profil keluarga, budget, stok, dan parameter planner |
| Ibrahim Ferizarizqi Permana | Backend Stok Dapur, Nutrisi Bahan, dan Harga Bahan | `KitchenStockController`, `KitchenStockViewController`, `KitchenStockService`, `KitchenStockDAO`, `KitchenStock`, `IngredientController`, `IngredientService`, serta query/backend yang mendukung pengelolaan stok, pengolahan data nutrisi bahan

## Build dan Testing

Untuk memastikan project dapat dikompilasi:

```bash
./mvnw clean test
```

atau pada Windows:

```powershell
.\mvnw.cmd clean test
```

Jika test belum tersedia, perintah ini tetap berguna untuk mengecek apakah project dapat dikompilasi tanpa error.

---

## Catatan Pengembangan

- Pastikan MySQL berjalan sebelum aplikasi dijalankan.
- Pastikan konfigurasi username dan password database sesuai dengan perangkat lokal.
- Jika terdapat perubahan skema database, jalankan ulang file SQL pada folder `database`.
