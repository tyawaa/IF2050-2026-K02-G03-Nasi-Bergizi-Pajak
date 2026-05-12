package nasi_bergizi_pajak.model;

import java.time.LocalDate;

public class MenuMingguan {
    private int menuId;
    private int userId;
    private int parameterId;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private double totalEstimation;
    private String statusBudget;

    public MenuMingguan() {
    }

    public MenuMingguan(int menuId, int userId, int parameterId, LocalDate weekStartDate, LocalDate weekEndDate, double totalEstimation, String statusBudget) {
        this.menuId = menuId;
        this.userId = userId;
        this.parameterId = parameterId;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.totalEstimation = totalEstimation;
        this.statusBudget = statusBudget;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getParameterId() {
        return parameterId;
    }

    public void setParameterId(int parameterId) {
        this.parameterId = parameterId;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public LocalDate getWeekEndDate() {
        return weekEndDate;
    }

    public void setWeekEndDate(LocalDate weekEndDate) {
        this.weekEndDate = weekEndDate;
    }

    public double getTotalEstimation() {
        return totalEstimation;
    }

    public void setTotalEstimation(double totalEstimation) {
        this.totalEstimation = totalEstimation;
    }

    public String getStatusBudget() {
        return statusBudget;
    }

    public void setStatusBudget(String statusBudget) {
        this.statusBudget = statusBudget;
    }
}