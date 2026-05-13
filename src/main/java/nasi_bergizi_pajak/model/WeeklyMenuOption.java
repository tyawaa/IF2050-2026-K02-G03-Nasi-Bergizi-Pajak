package nasi_bergizi_pajak.model;

import java.time.LocalDate;

public class WeeklyMenuOption {
    private final int menuId;
    private final LocalDate weekStartDate;
    private final LocalDate weekEndDate;
    private final LocalDate budgetStartDate;
    private final LocalDate budgetEndDate;
    private final String statusBudget;

    public WeeklyMenuOption(int menuId, LocalDate weekStartDate, LocalDate weekEndDate, String statusBudget) {
        this(menuId, weekStartDate, weekEndDate, null, null, statusBudget);
    }

    public WeeklyMenuOption(int menuId, LocalDate weekStartDate, LocalDate weekEndDate,
                            LocalDate budgetStartDate, LocalDate budgetEndDate, String statusBudget) {
        this.menuId = menuId;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.budgetStartDate = budgetStartDate;
        this.budgetEndDate = budgetEndDate;
        this.statusBudget = statusBudget;
    }

    public int getMenuId() {
        return menuId;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public LocalDate getWeekEndDate() {
        return weekEndDate;
    }

    public LocalDate getBudgetStartDate() {
        return budgetStartDate;
    }

    public LocalDate getBudgetEndDate() {
        return budgetEndDate;
    }

    public String getStatusBudget() {
        return statusBudget;
    }

    @Override
    public String toString() {
        return "Menu #" + menuId + " (" + weekStartDate + " s.d. " + weekEndDate + ") - " + statusBudget;
    }
}
