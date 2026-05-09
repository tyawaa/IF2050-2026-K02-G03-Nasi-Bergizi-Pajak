package nasi_bergizi_pajak.controller;

import nasi_bergizi_pajak.dao.MenuMingguanDAO;
import nasi_bergizi_pajak.dao.ResepDAO;
import nasi_bergizi_pajak.model.MenuMingguan;
import nasi_bergizi_pajak.model.RekomendasiMenu;
import nasi_bergizi_pajak.model.SlotMakan;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MenuController {
    private final MenuMingguanDAO menuMingguanDAO;
    private final ResepDAO resepDAO;
    private final RekomendasiController rekomendasiController;

    public MenuController() {
        this.menuMingguanDAO = new MenuMingguanDAO();
        this.resepDAO = new ResepDAO();
        this.rekomendasiController = new RekomendasiController();
    }

    public MenuMingguan buatMenuMingguan(int userId, int parameterId, LocalDate weekStartDate, LocalDate weekEndDate) throws SQLException {
        MenuMingguan menu = new MenuMingguan();
        menu.setUserId(userId);
        menu.setParameterId(parameterId);
        menu.setWeekStartDate(weekStartDate);
        menu.setWeekEndDate(weekEndDate);
        menu.setTotalEstimation(0);
        menu.setStatusBudget("DRAFT");

        int menuId = menuMingguanDAO.simpanMenuMingguan(menu);
        menu.setMenuId(menuId);

        return menu;
    }

    public void tambahSlotResep(int menuId, int recipeId, LocalDate mealDate, String mealTime) throws SQLException {
        SlotMakan slot = new SlotMakan();
        slot.setMenuId(menuId);
        slot.setRecipeId(recipeId);
        slot.setMealDate(mealDate);
        slot.setMealTime(mealTime);
        slot.setEatingOut(false);
        slot.setOutsideCost(0);

        menuMingguanDAO.simpanSlotMakan(slot);
    }

    public void tambahSlotMakanDiLuar(int menuId, LocalDate mealDate, String mealTime, double outsideCost) throws SQLException {
        SlotMakan slot = new SlotMakan();
        slot.setMenuId(menuId);
        slot.setRecipeId(0);
        slot.setMealDate(mealDate);
        slot.setMealTime(mealTime);
        slot.setEatingOut(true);
        slot.setOutsideCost(outsideCost);

        menuMingguanDAO.simpanSlotMakan(slot);
    }

    public double hitungEstimasiBiayaResep(int recipeId) throws SQLException {
        return resepDAO.hitungEstimasiHargaResep(recipeId);
    }

    public List<RekomendasiMenu> tampilkanRekomendasiMenu(int userId) throws SQLException {
        return rekomendasiController.buatRekomendasiMenu(userId);
    }

    public void perbaruiEstimasiMenu(int menuId, double totalEstimasi, double budgetAktif) throws SQLException {
        String statusBudget = totalEstimasi > budgetAktif ? "OVERBUDGET" : "SESUAI_BUDGET";
        menuMingguanDAO.updateTotalEstimasi(menuId, totalEstimasi, statusBudget);
    }
}