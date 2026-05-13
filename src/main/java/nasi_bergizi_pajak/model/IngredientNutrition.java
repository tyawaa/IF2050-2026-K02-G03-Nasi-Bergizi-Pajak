package nasi_bergizi_pajak.model;

public class IngredientNutrition {
    private int nutritionId;
    private int ingredientId;
    private double calories;
    private double protein;
    private double carbohydrate;
    private double fat;
    private double fibre;
    private String unit;

    public IngredientNutrition() {
    }

    public IngredientNutrition(int nutritionId, int ingredientId, double calories, double protein,
                               double carbohydrate, double fat, double fibre, String unit) {
        this.nutritionId = nutritionId;
        this.ingredientId = ingredientId;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrate = carbohydrate;
        this.fat = fat;
        this.fibre = fibre;
        this.unit = unit;
    }

    public int getNutritionId() {
        return nutritionId;
    }

    public void setNutritionId(int nutritionId) {
        this.nutritionId = nutritionId;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(double carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getFibre() {
        return fibre;
    }

    public void setFibre(double fibre) {
        this.fibre = fibre;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
