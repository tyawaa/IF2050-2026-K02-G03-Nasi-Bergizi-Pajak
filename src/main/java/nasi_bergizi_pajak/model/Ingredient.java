package nasi_bergizi_pajak.model;

public class Ingredient {
    private int ingredientId;
    private String name;
    private String unit;

    // Constructors
    public Ingredient() {}

    public Ingredient(int ingredientId, String name, String unit) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.unit = unit;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return name;
    }
}