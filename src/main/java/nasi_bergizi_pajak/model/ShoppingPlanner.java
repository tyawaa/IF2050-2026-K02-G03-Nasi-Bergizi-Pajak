package nasi_bergizi_pajak.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShoppingPlanner {
    private int plannerId;
    private int menuId;
    private LocalDateTime createdDatetime;
    private BigDecimal totalEstimation;
    private BigDecimal totalActual;
    private String status;
    private BigDecimal budgetAmount;

    public ShoppingPlanner(int plannerId, int menuId, LocalDateTime createdDatetime, BigDecimal totalEstimation,
                           BigDecimal totalActual, String status, BigDecimal budgetAmount) {
        this.plannerId = plannerId;
        this.menuId = menuId;
        this.createdDatetime = createdDatetime;
        this.totalEstimation = totalEstimation;
        this.totalActual = totalActual;
        this.status = status;
        this.budgetAmount = budgetAmount;
    }

    public int getPlannerId() {
        return plannerId;
    }

    public int getMenuId() {
        return menuId;
    }

    public LocalDateTime getCreatedDatetime() {
        return createdDatetime;
    }

    public BigDecimal getTotalEstimation() {
        return totalEstimation;
    }

    public void setTotalEstimation(BigDecimal totalEstimation) {
        this.totalEstimation = totalEstimation;
    }

    public BigDecimal getTotalActual() {
        return totalActual;
    }

    public void setTotalActual(BigDecimal totalActual) {
        this.totalActual = totalActual;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public boolean isOverBudget() {
        return budgetAmount != null && totalEstimation != null && totalEstimation.compareTo(budgetAmount) > 0;
    }
}
