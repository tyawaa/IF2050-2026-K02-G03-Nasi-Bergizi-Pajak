package nasi_bergizi_pajak.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import nasi_bergizi_pajak.dao.IngredientDAO;
import nasi_bergizi_pajak.dao.RecipeDAO;
import nasi_bergizi_pajak.dao.RecipeIngredientDAO;
import nasi_bergizi_pajak.model.Ingredient;
import nasi_bergizi_pajak.model.Recipe;
import nasi_bergizi_pajak.model.RecipeIngredient;

public class RecipeFormController {

    @FXML private TextField txtRecipeName;
    @FXML private TextArea txtRecipeDescription;
    @FXML private TextField txtServingSize;
    @FXML private ComboBox<String> cbStatus;

    @FXML private ComboBox<Ingredient> cbIngredient;
    @FXML private TextField txtIngredientQuantity;
    @FXML private TableView<RecipeIngredient> recipeIngredientTable;
    @FXML private TableColumn<RecipeIngredient, String> colFormIngredientName;
    @FXML private TableColumn<RecipeIngredient, Double> colFormIngredientQuantity;
    @FXML private TableColumn<RecipeIngredient, String> colFormIngredientUnit;

    private final RecipeDAO recipeDAO = new RecipeDAO();
    private final IngredientDAO ingredientDAO = new IngredientDAO();
    private final RecipeIngredientDAO recipeIngredientDAO = new RecipeIngredientDAO();
    private final ObservableList<Ingredient> ingredientOptions = FXCollections.observableArrayList();
    private final ObservableList<RecipeIngredient> recipeIngredients = FXCollections.observableArrayList();
    private final Map<Integer, Ingredient> ingredientById = new HashMap<>();

    private Recipe currentRecipe;

    @FXML
    private void initialize() {
        cbStatus.getItems().addAll("ACTIVE", "INACTIVE");
        cbStatus.setValue("ACTIVE");

        loadIngredientOptions();
        setupRecipeIngredientTable();
    }

    private void loadIngredientOptions() {
        ingredientOptions.setAll(ingredientDAO.listAllIngredients());
        ingredientById.clear();
        for (Ingredient ingredient : ingredientOptions) {
            ingredientById.put(ingredient.getIngredientId(), ingredient);
        }
        cbIngredient.setItems(ingredientOptions);
    }

    private void setupRecipeIngredientTable() {
        colFormIngredientName.setCellValueFactory(cellData -> {
            Ingredient ingredient = ingredientById.get(cellData.getValue().getIngredientId());
            String name = ingredient == null ? "-" : ingredient.getName();
            return new SimpleStringProperty(name);
        });
        colFormIngredientQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colFormIngredientUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        recipeIngredientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        recipeIngredientTable.setItems(recipeIngredients);
    }

    @FXML
    private void handleAddIngredient() {
        Ingredient ingredient = cbIngredient.getValue();
        if (ingredient == null) {
            showAlert("Data belum lengkap", "Pilih bahan terlebih dahulu.");
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(txtIngredientQuantity.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Data belum valid", "Jumlah bahan harus berupa angka.");
            return;
        }

        if (quantity <= 0) {
            showAlert("Data belum valid", "Jumlah bahan harus lebih dari 0.");
            return;
        }

        RecipeIngredient existing = findRecipeIngredient(ingredient.getIngredientId());
        if (existing != null) {
            existing.setQuantity(quantity);
            existing.setUnit(ingredient.getUnit());
            recipeIngredientTable.refresh();
        } else {
            int recipeId = currentRecipe == null ? 0 : currentRecipe.getRecipeId();
            recipeIngredients.add(new RecipeIngredient(0, recipeId, ingredient.getIngredientId(), quantity, ingredient.getUnit()));
        }

        cbIngredient.getSelectionModel().clearSelection();
        txtIngredientQuantity.clear();
    }

    @FXML
    private void handleRemoveIngredient() {
        RecipeIngredient selected = recipeIngredientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            recipeIngredients.remove(selected);
        }
    }

    @FXML
    private void handleSaveRecipe() {
        try {
            String name = txtRecipeName.getText().trim();
            String description = txtRecipeDescription.getText().trim();
            int servingSize = Integer.parseInt(txtServingSize.getText().trim());
            String status = cbStatus.getValue() == null ? "ACTIVE" : cbStatus.getValue();

            if (name.isBlank()) {
                showAlert("Data belum lengkap", "Nama resep wajib diisi.");
                return;
            }

            if (servingSize <= 0) {
                showAlert("Data belum valid", "Porsi harus lebih dari 0.");
                return;
            }

            int recipeId;
            if (currentRecipe != null) {
                currentRecipe.setName(name);
                currentRecipe.setDescription(description);
                currentRecipe.setServingSize(servingSize);
                currentRecipe.setStatus(status);
                recipeDAO.updateRecipe(currentRecipe);
                recipeId = currentRecipe.getRecipeId();
            } else {
                Recipe recipe = new Recipe(0, name, description, servingSize, status);
                recipeId = recipeDAO.insertRecipe(recipe);
            }

            recipeIngredientDAO.replaceRecipeIngredients(recipeId, recipeIngredients);
            closeWindow();
        } catch (NumberFormatException e) {
            showAlert("Data belum valid", "Porsi harus berupa angka bulat.");
        } catch (RuntimeException e) {
            showAlert("Gagal menyimpan resep", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    public void setRecipe(Recipe recipe) {
        this.currentRecipe = recipe;

        txtRecipeName.setText(recipe.getName());
        txtRecipeDescription.setText(recipe.getDescription());
        txtServingSize.setText(String.valueOf(recipe.getServingSize()));
        cbStatus.setValue(recipe.getStatus());

        List<RecipeIngredient> existingIngredients =
                recipeIngredientDAO.getRecipeIngredientsByRecipeId(recipe.getRecipeId());
        recipeIngredients.setAll(existingIngredients);
    }

    private RecipeIngredient findRecipeIngredient(int ingredientId) {
        for (RecipeIngredient recipeIngredient : recipeIngredients) {
            if (recipeIngredient.getIngredientId() == ingredientId) {
                return recipeIngredient;
            }
        }
        return null;
    }

    private void closeWindow() {
        Stage stage = (Stage) txtRecipeName.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
