package nasi_bergizi_pajak.model;

import java.time.LocalDate;

public class SlotMakan {
    private int slotId;
    private int menuId;
    private int recipeId;
    private LocalDate mealDate;
    private String mealTime;
    private boolean eatingOut;
    private double outsideCost;

    public SlotMakan() {
    }

    public SlotMakan(int slotId, int menuId, int recipeId, LocalDate mealDate, String mealTime, boolean eatingOut, double outsideCost) {
        this.slotId = slotId;
        this.menuId = menuId;
        this.recipeId = recipeId;
        this.mealDate = mealDate;
        this.mealTime = mealTime;
        this.eatingOut = eatingOut;
        this.outsideCost = outsideCost;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }

    public String getMealTime() {
        return mealTime;
    }

    public void setMealTime(String mealTime) {
        this.mealTime = mealTime;
    }

    public boolean isEatingOut() {
        return eatingOut;
    }

    public void setEatingOut(boolean eatingOut) {
        this.eatingOut = eatingOut;
    }

    public double getOutsideCost() {
        return outsideCost;
    }

    public void setOutsideCost(double outsideCost) {
        this.outsideCost = outsideCost;
    }
}