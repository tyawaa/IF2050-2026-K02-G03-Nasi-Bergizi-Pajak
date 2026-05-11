package nasi_bergizi_pajak.model;

public class RecipeIngredient {
    private int recipeIngredientId;
    private int recipeId;
    private int ingredientId;
    private double amount;

    // Constructors
    public RecipeIngredient() {}

    public RecipeIngredient(int recipeIngredientId, int recipeId, int ingredientId, double amount) {
        this.recipeIngredientId = recipeIngredientId;
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.amount = amount;
    }

    // Getters and Setters
    public int getRecipeIngredientId() {
        return recipeIngredientId;
    }

    public void setRecipeIngredientId(int recipeIngredientId) {
        this.recipeIngredientId = recipeIngredientId;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}