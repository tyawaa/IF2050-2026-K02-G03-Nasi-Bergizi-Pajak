package nasi_bergizi_pajak.model;

import java.time.LocalDate;

public class Budget {
    private int budgetId;
    private int userId;
    private String name;
    private double amount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String status;

    public Budget() {
    }

    public Budget(int userId, String name, double amount, LocalDate periodStart, LocalDate periodEnd, String status) {
        this.userId = userId;
        this.name = name;
        this.amount = amount;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.status = status;
    }

    public Budget(int budgetId, int userId, String name, double amount, LocalDate periodStart, LocalDate periodEnd, String status) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.name = name;
        this.amount = amount;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.status = status;
    }

    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Budget{" +
                "budgetId=" + budgetId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", periodStart=" + periodStart +
                ", periodEnd=" + periodEnd +
                ", status='" + status + '\'' +
                '}';
    }
}
