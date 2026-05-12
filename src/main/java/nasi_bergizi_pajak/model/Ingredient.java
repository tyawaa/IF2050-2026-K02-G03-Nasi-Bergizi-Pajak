package nasi_bergizi_pajak.model;

import java.time.LocalDate;

public class Ingredient {
    private int ingredientId;
    private String name;
    private String unit;
    private double pricePerUnit;
    private LocalDate priceEffectiveDate;

    public Ingredient() {
    }

    public Ingredient(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }

    public Ingredient(int ingredientId, String name, String unit) {
        this(ingredientId, name, unit, 0);
    }

    public Ingredient(int ingredientId, String name, String unit, double pricePerUnit) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public double getCurrentPrice() {
        return pricePerUnit;
    }

    public void setCurrentPrice(double currentPrice) {
        this.pricePerUnit = currentPrice;
    }

    public LocalDate getPriceEffectiveDate() {
        return priceEffectiveDate;
    }

    public void setPriceEffectiveDate(LocalDate priceEffectiveDate) {
        this.priceEffectiveDate = priceEffectiveDate;
    }

    @Override
    public String toString() {
        return name == null ? "" : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return ingredientId == that.ingredientId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(ingredientId);
    }
}
