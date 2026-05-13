package nasi_bergizi_pajak.service;

import nasi_bergizi_pajak.dao.BudgetDAO;
import nasi_bergizi_pajak.model.Budget;

import java.sql.SQLException;
import java.util.List;

public class BudgetService {
    private final BudgetDAO budgetDAO;

    public BudgetService() {
        this(new BudgetDAO());
    }

    public BudgetService(BudgetDAO budgetDAO) {
        this.budgetDAO = budgetDAO;
    }

    public Budget setBudget(Budget budget) throws SQLException {
        validasiBudget(budget);
        normalisasiStatus(budget);
        return budgetDAO.setBudget(budget);
    }

    public boolean updateBudget(int budgetId, Budget budget) throws SQLException {
        if (budget == null) {
            throw new IllegalArgumentException("Data budget wajib diisi.");
        }
        budget.setBudgetId(budgetId);
        validasiBudget(budget);
        normalisasiStatus(budget);
        return budgetDAO.updateBudget(budget);
    }

    public boolean hapusBudget(int budgetId, int userId) throws SQLException {
        if (budgetId <= 0) {
            throw new IllegalArgumentException("Budget tidak valid.");
        }
        if (userId <= 0) {
            throw new IllegalArgumentException("User tidak valid.");
        }

        return budgetDAO.hapusBudget(budgetId, userId);
    }

    public Budget getBudgetAktif(int userId) throws SQLException {
        validasiUserId(userId);
        return budgetDAO.getBudgetAktif(userId);
    }

    public List<Budget> getDaftarBudget(int userId) throws SQLException {
        validasiUserId(userId);
        return budgetDAO.getDaftarBudget(userId);
    }

    public double getSisaBudget(int userId) throws SQLException {
        Budget budgetAktif = getBudgetAktif(userId);
        if (budgetAktif == null) {
            return 0;
        }

        return budgetAktif.getAmount();
    }

    private void validasiBudget(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Data budget wajib diisi.");
        }
        validasiUserId(budget.getUserId());
        if (budget.getName() == null || budget.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama budget wajib diisi.");
        }
        if (budget.getAmount() < 0) {
            throw new IllegalArgumentException("Nominal budget tidak boleh negatif.");
        }
        if (budget.getPeriodStart() == null || budget.getPeriodEnd() == null) {
            throw new IllegalArgumentException("Periode budget wajib diisi.");
        }
        if (budget.getPeriodEnd().isBefore(budget.getPeriodStart())) {
            throw new IllegalArgumentException("Tanggal akhir budget tidak boleh sebelum tanggal mulai.");
        }
    }

    private void normalisasiStatus(Budget budget) {
        if (budget.getStatus() == null || budget.getStatus().trim().isEmpty()) {
            budget.setStatus("active");
            return;
        }

        String status = budget.getStatus().trim().toLowerCase();
        if (!"active".equals(status) && !"inactive".equals(status)) {
            throw new IllegalArgumentException("Status budget harus active atau inactive.");
        }

        budget.setStatus(status);
    }

    private void validasiUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User tidak valid.");
        }
    }
}
