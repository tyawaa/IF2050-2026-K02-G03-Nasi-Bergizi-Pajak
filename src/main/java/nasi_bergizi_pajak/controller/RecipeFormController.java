package nasi_bergizi_pajak.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import nasi_bergizi_pajak.dao.IngredientDAO;
import nasi_bergizi_pajak.dao.RecipeIngredientDAO;
import nasi_bergizi_pajak.model.Ingredient;
import nasi_bergizi_pajak.model.RecipeIngredient;
import nasi_bergizi_pajak.dao.RecipeDAO;
import nasi_bergizi_pajak.model.Recipe;

public class RecipeFormController {

    @FXML
    private TextField txtRecipeName;

    @FXML
    private TextArea txtRecipeDescription;

    @FXML
    private TextField txtServingSize;

    @FXML
    private ComboBox<String> cbStatus;

    @FXML
    private ComboBox<Ingredient> cmbIngredient;

    @FXML
    private TextField txtIngredientAmount;

    @FXML
    private TableView<RecipeIngredient> recipeIngredientTable;

    @FXML
    private TableColumn<RecipeIngredient, String> colIngredientName;

    @FXML
    private TableColumn<RecipeIngredient, Double> colIngredientAmount;

    @FXML
    private TableColumn<RecipeIngredient, String> colIngredientUnit;

    @FXML
    private TableColumn<RecipeIngredient, Void> colIngredientAction;

    @FXML
    private Button btnDeleteRecipe;

    private final RecipeDAO recipeDAO = new RecipeDAO();
    private Recipe currentRecipe;

    private final IngredientDAO ingredientDAO =
        new IngredientDAO();

    private final RecipeIngredientDAO recipeIngredientDAO =
        new RecipeIngredientDAO();

    private final ObservableList<RecipeIngredient>
        recipeIngredients =
        FXCollections.observableArrayList();
    
    @FXML
    private void initialize() {

        cbStatus.getItems().addAll(
                "ACTIVE",
                "INACTIVE"
        );

        loadIngredients();
        setupIngredientTable();
    }

    private void loadIngredients() {

        cmbIngredient.getItems().setAll(
                ingredientDAO.listAllIngredients()
        );

        cmbIngredient.setCellFactory(param ->
                new ListCell<>() {

                    @Override
                    protected void updateItem(
                            Ingredient item,
                            boolean empty
                    ) {

                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                });

        cmbIngredient.setButtonCell(
                new ListCell<>() {

                    @Override
                    protected void updateItem(
                            Ingredient item,
                            boolean empty
                    ) {

                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                });
    }

    @FXML
    private void handleSaveRecipe() {

        try {

            String name =
                    txtRecipeName.getText();

            String description =
                    txtRecipeDescription.getText();

            int servingSize =
                    Integer.parseInt(
                            txtServingSize.getText()
                    );

            String status =
                    cbStatus.getValue();

            if (currentRecipe != null) {

                currentRecipe.setName(name);
                currentRecipe.setDescription(description);
                currentRecipe.setServingSize(servingSize);
                currentRecipe.setStatus(status);

                recipeDAO.updateRecipe(currentRecipe);

                recipeIngredientDAO.deleteByRecipe(
                        currentRecipe.getRecipeId()
                );

                for (RecipeIngredient ri :
                        recipeIngredients) {

                    recipeIngredientDAO.insert(
                            currentRecipe.getRecipeId(),
                            ri.getIngredientId(),
                            ri.getAmount(),
                            ri.getUnit()
                    );
                }

            } else {

                Recipe recipe = new Recipe(
                        0,
                        name,
                        description,
                        servingSize,
                        status
                );

                int recipeId =
                        recipeDAO.insertRecipe(recipe);

                for (RecipeIngredient ri :
                        recipeIngredients) {

                    recipeIngredientDAO.insert(
                            recipeId,
                            ri.getIngredientId(),
                            ri.getAmount(),
                            ri.getUnit()
                    );
                }
            }

            closeWindow();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Gagal menyimpan resep"
            );
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage =
            (Stage) txtRecipeName.getScene().getWindow();

        stage.close();
    }


    public void setRecipe(Recipe recipe) {

        this.currentRecipe = recipe;

        txtRecipeName.setText(recipe.getName());
        txtRecipeDescription.setText(recipe.getDescription());
        txtServingSize.setText(String.valueOf(recipe.getServingSize()));
        cbStatus.setValue(recipe.getStatus());

        btnDeleteRecipe.setVisible(true);
        btnDeleteRecipe.setManaged(true);

        // Load existing ingredients into the table
        recipeIngredients.setAll(
            recipeIngredientDAO.getRecipeIngredientsByRecipeId(recipe.getRecipeId())
        );
    }

    private void setupIngredientTable() {

        colIngredientName.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getIngredientName()
                )
        );

        colIngredientAmount.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getAmount()
                )
        );

        colIngredientUnit.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getUnit()
                )
        );

        recipeIngredientTable.setItems(
                recipeIngredients
        );

        colIngredientAction.setCellFactory(param ->
                new TableCell<>() {

                    private final Button btnDelete =
                            new Button("Hapus");

                    {

                        btnDelete.getStyleClass()
                                .add("table-danger-button");

                        btnDelete.setOnAction(event -> {

                            RecipeIngredient ingredient =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            recipeIngredients.remove(
                                    ingredient
                            );
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty
                    ) {

                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnDelete);
                        }
                    }
                });
    }


    @FXML
    private void handleAddIngredient() {

        Ingredient ingredient =
                cmbIngredient.getValue();

        if (ingredient == null) {

            showAlert(
                    "Error",
                    "Pilih bahan terlebih dahulu"
            );

            return;
        }

        double amount;

        try {

            amount = Double.parseDouble(
                    txtIngredientAmount.getText()
            );

        } catch (Exception e) {

            showAlert(
                    "Error",
                    "Jumlah tidak valid"
            );

            return;
        }

        boolean alreadyExists =
                recipeIngredients.stream()
                        .anyMatch(ri ->
                                ri.getIngredientId()
                                        == ingredient.getIngredientId()
                        );

        if (alreadyExists) {

            showAlert(
                    "Error",
                    "Bahan sudah ditambahkan"
            );

            return;
        }

        RecipeIngredient recipeIngredient =
                new RecipeIngredient(
                        ingredient.getIngredientId(),
                        ingredient.getName(),
                        amount,
                        ingredient.getUnit()
                );

        recipeIngredients.add(recipeIngredient);

        cmbIngredient.setValue(null);

        txtIngredientAmount.clear();
    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    @FXML
    private void handleDeleteRecipe() {

        if (currentRecipe == null) {
            return;
        }

        Alert confirm =
            new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Konfirmasi");
        confirm.setHeaderText(null);

        confirm.setContentText(
            "Yakin ingin menghapus resep ini?"
        );

        ButtonType result =
            confirm.showAndWait()
                .orElse(ButtonType.CANCEL);

        if (result != ButtonType.OK) {
            return;
        }

        try {

            recipeDAO.deleteRecipe(
                currentRecipe.getRecipeId()
            );

            closeWindow();

        } catch (Exception e) {

            showAlert(
                "Error",
                "Gagal menghapus resep"
            );
        }
    }
}