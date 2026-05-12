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
import nasi_bergizi_pajak.util.UnitOptions;

public class RecipeFormController {

    @FXML private TextField txtRecipeName;
    @FXML private TextArea txtRecipeDescription;
    @FXML private TextField txtServingSize;
    @FXML private ComboBox<String> cbStatus;

    @FXML private ComboBox<Ingredient> cbIngredient;
    @FXML private TextField txtIngredientQuantity;
    @FXML private ComboBox<String> cbIngredientUnit;
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
        cbStatus.getItems().addAll("Aktif", "Nonaktif");
        cbStatus.setValue("Aktif");

        loadIngredientOptions();
        setupRecipeIngredientTable();
        setupIngredientUnitOptions();
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

    private void setupIngredientUnitOptions() {
        cbIngredient.valueProperty().addListener((obs, oldValue, newValue) -> {
            cbIngredientUnit.getItems().clear();
            cbIngredientUnit.getSelectionModel().clearSelection();
            if (newValue != null) {
                cbIngredientUnit.getItems().setAll(UnitOptions.recipeUnitsFor(newValue.getUnit()));
                cbIngredientUnit.setValue(UnitOptions.defaultRecipeUnit(newValue.getUnit()));
            }
        });
    }

    @FXML
    private void handleAddIngredient() {
        Ingredient ingredient = cbIngredient.getValue();
        if (ingredient == null) {
            showAlert("Data belum lengkap", "Pilih bahan terlebih dahulu.");
            return;
        }

        String unit = cbIngredientUnit.getValue();
        if (unit == null || unit.isBlank()) {
            showAlert("Data belum lengkap", "Pilih unit bahan terlebih dahulu.");
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
            existing.setUnit(unit);
            recipeIngredientTable.refresh();
        } else {
            int recipeId = currentRecipe == null ? 0 : currentRecipe.getRecipeId();
            recipeIngredients.add(new RecipeIngredient(0, recipeId, ingredient.getIngredientId(), quantity, unit));
        }

        cbIngredient.getSelectionModel().clearSelection();
        cbIngredientUnit.getItems().clear();
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
            String status = cbStatus.getValue() == null ? "Aktif" : cbStatus.getValue();

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
                currentRecipe.setStatus(toDatabaseStatus(status));
                recipeDAO.saveRecipeWithIngredients(currentRecipe, recipeIngredients);
                recipeId = currentRecipe.getRecipeId();
            } else {
                Recipe recipe = new Recipe(0, name, description, servingSize, toDatabaseStatus(status));
                recipeId = recipeDAO.saveRecipeWithIngredients(recipe, recipeIngredients);
            }
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
        cbStatus.setValue(toDisplayStatus(recipe.getStatus()));

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

    private String toDisplayStatus(String status) {
        if (status == null) {
            return "Aktif";
        }
        return switch (status.trim().toLowerCase()) {
            case "inactive", "nonaktif", "non-aktif" -> "Nonaktif";
            default -> "Aktif";
        };
    }

    private String toDatabaseStatus(String status) {
        return "Nonaktif".equalsIgnoreCase(status) ? "inactive" : "active";
    }
}
