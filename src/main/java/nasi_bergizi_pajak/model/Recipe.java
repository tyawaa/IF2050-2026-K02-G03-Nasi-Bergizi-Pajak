package nasi_bergizi_pajak.model;

public class Recipe {
    private int recipeId;
    private String name;
    private String description;
    private int servingSize;
    private String status;

    // Constructors
    public Recipe() {}

    public Recipe(int recipeId, String name, String description, int servingSize, String status) {
        this.recipeId = recipeId;
        this.name = name;
        this.description = description;
        this.servingSize = servingSize;
        this.status = status;
    }

    // Getters and Setters
    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getServingSize() {
        return servingSize;
    }

    public void setServingSize(int servingSize) {
        this.servingSize = servingSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name;
    }
}