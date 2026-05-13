package nasi_bergizi_pajak.controller;

import nasi_bergizi_pajak.model.Budget;
import nasi_bergizi_pajak.service.BudgetService;

import java.sql.SQLException;
import java.util.List;

public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController() {
        this(new BudgetService());
    }

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    public Budget setBudget(Budget budget) throws SQLException {
        try {
            return budgetService.setBudget(budget);
        } catch (SQLException e) {
            throw new SQLException("Gagal menyimpan budget: " + e.getMessage(), e);
        }
    }

    public boolean updateBudget(int budgetId, Budget budget) throws SQLException {
        try {
            return budgetService.updateBudget(budgetId, budget);
        } catch (SQLException e) {
            throw new SQLException("Gagal mengubah budget: " + e.getMessage(), e);
        }
    }

    public boolean hapusBudget(int budgetId, int userId) throws SQLException {
        try {
            return budgetService.hapusBudget(budgetId, userId);
        } catch (SQLException e) {
            throw new SQLException("Gagal menghapus budget: " + e.getMessage(), e);
        }
    }

    public Budget getBudgetAktif(int userId) throws SQLException {
        try {
            return budgetService.getBudgetAktif(userId);
        } catch (SQLException e) {
            throw new SQLException("Gagal mengambil budget aktif: " + e.getMessage(), e);
        }
    }

    public List<Budget> getDaftarBudget(int userId) throws SQLException {
        try {
            return budgetService.getDaftarBudget(userId);
        } catch (SQLException e) {
            throw new SQLException("Gagal mengambil daftar budget: " + e.getMessage(), e);
        }
    }

    public double getSisaBudget(int userId) throws SQLException {
        try {
            return budgetService.getSisaBudget(userId);
        } catch (SQLException e) {
            throw new SQLException("Gagal menghitung sisa budget: " + e.getMessage(), e);
        }
    }
}
