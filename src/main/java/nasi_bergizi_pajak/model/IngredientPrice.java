package nasi_bergizi_pajak.model;

import java.time.LocalDate;

public class IngredientPrice {
    private int priceId;
    private int ingredientId;
    private double price;
    private LocalDate effectiveDate;
    private String ingredientName;

    public IngredientPrice() {}

    public IngredientPrice(int ingredientId, double price, LocalDate effectiveDate) {
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

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    @Override
    public String toString() {
        return "IngredientPrice{" +
                "priceId=" + priceId +
                ", ingredientId=" + ingredientId +
                ", ingredientName='" + ingredientName + '\'' +
                ", price=" + price +
                ", effectiveDate=" + effectiveDate +
                '}';
    }
}
