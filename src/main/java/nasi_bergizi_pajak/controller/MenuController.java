package nasi_bergizi_pajak.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import nasi_bergizi_pajak.dao.MenuMingguanDAO;
import nasi_bergizi_pajak.model.MenuMingguan;
import nasi_bergizi_pajak.model.RekomendasiMenu;
import nasi_bergizi_pajak.model.SlotMakan;

public class MenuController {
    private static final Set<String> MEAL_TIMES = Set.of(
            "breakfast",
            "lunch",
            "dinner",
            "snack"
    );

    private final MenuMingguanDAO menuMingguanDAO;
    private final RekomendasiController rekomendasiController;

    public MenuController() {
        this.menuMingguanDAO = new MenuMingguanDAO();
        this.rekomendasiController = new RekomendasiController();
    }

    public MenuMingguan buatMenuMingguan(int userId, int parameterId, LocalDate weekStartDate, LocalDate weekEndDate) throws SQLException {
        validasiRentangMinggu(weekStartDate, weekEndDate);

        MenuMingguan menu = new MenuMingguan();
        menu.setUserId(userId);
        menu.setParameterId(parameterId);
        menu.setWeekStartDate(weekStartDate);
        menu.setWeekEndDate(weekEndDate);
        menu.setTotalEstimation(0);
        menu.setStatusBudget("draft");

        int menuId = menuMingguanDAO.simpanMenuMingguan(menu);
        menu.setMenuId(menuId);

        return menu;
    }

    public void tambahSlotResep(int menuId, int recipeId, LocalDate mealDate, String mealTime) throws SQLException {
        if (recipeId <= 0) {
            throw new IllegalArgumentException("Recipe ID harus valid.");
        }

        String mealTimeValid = validasiSlot(menuId, mealDate, mealTime, null);

        SlotMakan slot = new SlotMakan();
        slot.setMenuId(menuId);
        slot.setRecipeId(recipeId);
        slot.setMealDate(mealDate);
        slot.setMealTime(mealTimeValid);
        slot.setEatingOut(false);
        slot.setOutsideCost(0);

        menuMingguanDAO.simpanSlotMakanDanPerbaruiEstimasi(slot);
    }

    public void tambahSlotMakanDiLuar(int menuId, LocalDate mealDate, String mealTime, double outsideCost) throws SQLException {
        if (outsideCost < 0) {
            throw new IllegalArgumentException("Biaya makan di luar tidak boleh negatif.");
        }

        String mealTimeValid = validasiSlot(menuId, mealDate, mealTime, null);

        SlotMakan slot = new SlotMakan();
        slot.setMenuId(menuId);
        slot.setRecipeId(null);
        slot.setMealDate(mealDate);
        slot.setMealTime(mealTimeValid);
        slot.setEatingOut(true);
        slot.setOutsideCost(outsideCost);

        menuMingguanDAO.simpanSlotMakanDanPerbaruiEstimasi(slot);
    }

    public void ubahSlotResep(int slotId, int menuId, int recipeId, LocalDate mealDate, String mealTime) throws SQLException {
        if (slotId <= 0) {
            throw new IllegalArgumentException("Slot ID harus valid.");
        }

        if (recipeId <= 0) {
            throw new IllegalArgumentException("Recipe ID harus valid.");
        }

        String mealTimeValid = validasiSlot(menuId, mealDate, mealTime, slotId);

        SlotMakan slot = new SlotMakan();
        slot.setSlotId(slotId);
        slot.setMenuId(menuId);
        slot.setRecipeId(recipeId);
        slot.setMealDate(mealDate);
        slot.setMealTime(mealTimeValid);
        slot.setEatingOut(false);
        slot.setOutsideCost(0);

        menuMingguanDAO.ubahSlotMakanDanPerbaruiEstimasi(slot);
    }

    public void ubahSlotMakanDiLuar(int slotId, int menuId, LocalDate mealDate, String mealTime, double outsideCost) throws SQLException {
        if (slotId <= 0) {
            throw new IllegalArgumentException("Slot ID harus valid.");
        }

        if (outsideCost < 0) {
            throw new IllegalArgumentException("Biaya makan di luar tidak boleh negatif.");
        }

        String mealTimeValid = validasiSlot(menuId, mealDate, mealTime, slotId);

        SlotMakan slot = new SlotMakan();
        slot.setSlotId(slotId);
        slot.setMenuId(menuId);
        slot.setRecipeId(null);
        slot.setMealDate(mealDate);
        slot.setMealTime(mealTimeValid);
        slot.setEatingOut(true);
        slot.setOutsideCost(outsideCost);

        menuMingguanDAO.ubahSlotMakanDanPerbaruiEstimasi(slot);
    }

    public void hapusSlotMakan(int menuId, int slotId) throws SQLException {
        if (menuId <= 0 || slotId <= 0) {
            throw new IllegalArgumentException("Menu ID dan Slot ID harus valid.");
        }

        menuMingguanDAO.hapusSlotMakanDanPerbaruiEstimasi(menuId, slotId);
    }

    public MenuMingguan ambilMenuMingguan(int menuId) throws SQLException {
        return menuMingguanDAO.cariMenuMingguanById(menuId);
    }

    public List<MenuMingguan> ambilMenuMingguanPengguna(int userId) throws SQLException {
        return menuMingguanDAO.cariMenuMingguanByUserId(userId);
    }

    public List<SlotMakan> ambilSlotMakan(int menuId) throws SQLException {
        return menuMingguanDAO.cariSlotMakanByMenuId(menuId);
    }

    public void hitungUlangEstimasiMenu(int menuId) throws SQLException {
        menuMingguanDAO.hitungUlangEstimasiDanStatus(menuId);
    }

    public List<RekomendasiMenu> tampilkanRekomendasiMenu(int userId) throws SQLException {
        return rekomendasiController.buatRekomendasiMenu(userId);
    }

    private void validasiRentangMinggu(LocalDate weekStartDate, LocalDate weekEndDate) {
        if (weekStartDate == null || weekEndDate == null) {
            throw new IllegalArgumentException("Tanggal awal dan akhir minggu wajib diisi.");
        }

        if (weekEndDate.isBefore(weekStartDate)) {
            throw new IllegalArgumentException("Tanggal akhir minggu tidak boleh sebelum tanggal awal.");
        }
    }

    private String validasiSlot(int menuId, LocalDate mealDate, String mealTime, Integer kecualiSlotId) throws SQLException {
        if (menuId <= 0) {
            throw new IllegalArgumentException("Menu ID harus valid.");
        }

        if (mealDate == null) {
            throw new IllegalArgumentException("Tanggal makan wajib diisi.");
        }

        MenuMingguan menu = menuMingguanDAO.cariMenuMingguanById(menuId);
        if (menu == null) {
            throw new IllegalArgumentException("Menu mingguan tidak ditemukan.");
        }

        if (mealDate.isBefore(menu.getWeekStartDate()) || mealDate.isAfter(menu.getWeekEndDate())) {
            throw new IllegalArgumentException("Tanggal makan harus berada dalam rentang menu mingguan.");
        }

        String mealTimeValid = normalisasiMealTime(mealTime);

        if (menuMingguanDAO.slotSudahAda(menuId, mealDate, mealTimeValid, kecualiSlotId)) {
            throw new IllegalArgumentException("Slot makan pada tanggal dan waktu tersebut sudah ada.");
        }

        return mealTimeValid;
    }

    private String normalisasiMealTime(String mealTime) {
        if (mealTime == null || mealTime.isBlank()) {
            throw new IllegalArgumentException("Waktu makan wajib diisi.");
        }

        String mealTimeValid = mealTime.trim().toLowerCase(Locale.ROOT);

        if (!MEAL_TIMES.contains(mealTimeValid)) {
            throw new IllegalArgumentException("Waktu makan hanya boleh breakfast, lunch, dinner, atau snack.");
        }

        return mealTimeValid;
    }
}