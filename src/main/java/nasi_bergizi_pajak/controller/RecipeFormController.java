package nasi_bergizi_pajak.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import nasi_bergizi_pajak.dao.IngredientDAO;
import nasi_bergizi_pajak.dao.IngredientNutritionDAO;
import nasi_bergizi_pajak.dao.RecipeDAO;
import nasi_bergizi_pajak.dao.RecipeIngredientDAO;
import nasi_bergizi_pajak.model.Ingredient;
import nasi_bergizi_pajak.model.IngredientNutrition;
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
    private final IngredientNutritionDAO nutritionDAO = new IngredientNutritionDAO();
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
    private void handleAddNewIngredient() {
        Dialog<NewIngredientFormResult> dialog = new Dialog<>();
        dialog.setTitle("Tambah Bahan Baru");
        dialog.setHeaderText("Tambah bahan, harga, dan nutrisi");

        ButtonType saveType = new ButtonType("Simpan Bahan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, saveType);

        TextField nameField = new TextField();
        nameField.setPromptText("Nama bahan");
        ComboBox<String> unitCombo = new ComboBox<>(FXCollections.observableArrayList(UnitOptions.masterUnitsWith(null)));
        unitCombo.setPromptText("Unit dasar");
        TextField priceField = new TextField();
        priceField.setPromptText("Harga saat ini");
        TextField caloriesField = new TextField("0");
        TextField proteinField = new TextField("0");
        TextField carbohydrateField = new TextField("0");
        TextField fatField = new TextField("0");
        TextField fibreField = new TextField("0");
        TextField nutritionUnitField = new TextField("100g");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.add(new Label("Nama"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Unit dasar"), 0, 1);
        form.add(unitCombo, 1, 1);
        form.add(new Label("Harga saat ini"), 0, 2);
        form.add(priceField, 1, 2);
        form.add(new Label("Kalori"), 0, 3);
        form.add(caloriesField, 1, 3);
        form.add(new Label("Protein"), 0, 4);
        form.add(proteinField, 1, 4);
        form.add(new Label("Karbohidrat"), 0, 5);
        form.add(carbohydrateField, 1, 5);
        form.add(new Label("Lemak"), 0, 6);
        form.add(fatField, 1, 6);
        form.add(new Label("Serat"), 0, 7);
        form.add(fibreField, 1, 7);
        form.add(new Label("Nutrisi per"), 0, 8);
        form.add(nutritionUnitField, 1, 8);
        dialog.getDialogPane().setContent(form);

        dialog.getDialogPane().lookupButton(saveType).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String error = validateNewIngredientForm(
                    nameField.getText(),
                    unitCombo.getValue(),
                    priceField.getText(),
                    caloriesField.getText(),
                    proteinField.getText(),
                    carbohydrateField.getText(),
                    fatField.getText(),
                    fibreField.getText(),
                    nutritionUnitField.getText()
            );
            if (error != null) {
                showAlert("Data bahan belum valid", error);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != saveType) {
                return null;
            }

            return new NewIngredientFormResult(
                    nameField.getText().trim(),
                    unitCombo.getValue(),
                    Double.parseDouble(priceField.getText().trim()),
                    Double.parseDouble(caloriesField.getText().trim()),
                    Double.parseDouble(proteinField.getText().trim()),
                    Double.parseDouble(carbohydrateField.getText().trim()),
                    Double.parseDouble(fatField.getText().trim()),
                    Double.parseDouble(fibreField.getText().trim()),
                    nutritionUnitField.getText().trim()
            );
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                Ingredient ingredient = new Ingredient(0, result.name(), result.unit(), result.price());
                int ingredientId = ingredientDAO.insertIngredient(ingredient);
                nutritionDAO.upsertNutrition(new IngredientNutrition(
                        0,
                        ingredientId,
                        result.calories(),
                        result.protein(),
                        result.carbohydrate(),
                        result.fat(),
                        result.fibre(),
                        result.nutritionUnit()
                ));

                loadIngredientOptions();
                Ingredient savedIngredient = ingredientById.get(ingredientId);
                if (savedIngredient != null) {
                    cbIngredient.setValue(savedIngredient);
                }
            } catch (RuntimeException e) {
                showAlert("Gagal menyimpan bahan", e.getMessage());
            }
        });
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

    private String validateNewIngredientForm(String name,
                                             String unit,
                                             String price,
                                             String calories,
                                             String protein,
                                             String carbohydrate,
                                             String fat,
                                             String fibre,
                                             String nutritionUnit) {
        if (name == null || name.trim().isBlank()) {
            return "Nama bahan wajib diisi.";
        }

        if (unit == null || unit.trim().isBlank()) {
            return "Unit dasar bahan wajib dipilih.";
        }

        if (nutritionUnit == null || nutritionUnit.trim().isBlank()) {
            return "Satuan nutrisi per wajib diisi, misalnya 100g.";
        }

        try {
            if (Double.parseDouble(price.trim()) < 0
                    || Double.parseDouble(calories.trim()) < 0
                    || Double.parseDouble(protein.trim()) < 0
                    || Double.parseDouble(carbohydrate.trim()) < 0
                    || Double.parseDouble(fat.trim()) < 0
                    || Double.parseDouble(fibre.trim()) < 0) {
                return "Harga dan nilai nutrisi tidak boleh negatif.";
            }
        } catch (Exception e) {
            return "Harga dan nilai nutrisi harus berupa angka.";
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

    private record NewIngredientFormResult(String name,
                                           String unit,
                                           double price,
                                           double calories,
                                           double protein,
                                           double carbohydrate,
                                           double fat,
                                           double fibre,
                                           String nutritionUnit) {
    }
}
