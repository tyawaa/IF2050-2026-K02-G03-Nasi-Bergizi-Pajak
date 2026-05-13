package nasi_bergizi_pajak.model;

import java.math.BigDecimal;

public class ShoppingItem {
    private int itemId;
    private int plannerId;
    private int ingredientId;
    private String ingredientName;
    private double requiredQty;
    private String unit;
    private BigDecimal estimatedPrice;
    private BigDecimal actualPrice;
    private boolean bought;

    public ShoppingItem(int itemId, int plannerId, int ingredientId, String ingredientName, double requiredQty,
                        String unit, BigDecimal estimatedPrice, BigDecimal actualPrice, String statusBeli) {
        this.itemId = itemId;
        this.plannerId = plannerId;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.requiredQty = requiredQty;
        this.unit = unit;
        this.estimatedPrice = estimatedPrice;
        this.actualPrice = actualPrice;
        this.bought = "sudah".equalsIgnoreCase(statusBeli);
    }

    public int getItemId() {
        return itemId;
    }

    public int getPlannerId() {
        return plannerId;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public double getRequiredQty() {
        return requiredQty;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getEstimatedPrice() {
        return estimatedPrice;
    }

    public BigDecimal getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(BigDecimal actualPrice) {
        this.actualPrice = actualPrice;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    public String getStatusBeli() {
        return bought ? "sudah" : "belum";
    }
}
