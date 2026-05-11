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

    public IngredientNutrition(int nutritionId, int ingredientId, double calories,
                                double protein, double carbohydrate, double fat,
                                double fibre, String unit) {
        this.nutritionId   = nutritionId;
        this.ingredientId  = ingredientId;
        this.calories      = calories;
        this.protein       = protein;
        this.carbohydrate  = carbohydrate;
        this.fat           = fat;
        this.fibre         = fibre;
        this.unit          = unit;
    }

    public int    getNutritionId()   { return nutritionId; }
    public int    getIngredientId()  { return ingredientId; }
    public double getCalories()      { return calories; }
    public double getProtein()       { return protein; }
    public double getCarbohydrate()  { return carbohydrate; }
    public double getFat()           { return fat; }
    public double getFibre()         { return fibre; }
    public String getUnit()          { return unit; }
}