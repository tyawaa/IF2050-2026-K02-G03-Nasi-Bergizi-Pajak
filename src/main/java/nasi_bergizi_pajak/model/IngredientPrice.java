package nasi_bergizi_pajak.model;

import java.time.LocalDate;

public class IngredientPrice {
    private int priceId;
    private int ingredientId;
    private double price;
    private LocalDate effectiveDate;

    public IngredientPrice() {
    }

    public IngredientPrice(int priceId, int ingredientId, double price, LocalDate effectiveDate) {
        this.priceId = priceId;
        this.ingredientId = ingredientId;
        this.price = price;
        this.effectiveDate = effectiveDate;
    }

    public int getPriceId() {
        return priceId;
    }

    public void setPriceId(int priceId) {
        this.priceId = priceId;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
