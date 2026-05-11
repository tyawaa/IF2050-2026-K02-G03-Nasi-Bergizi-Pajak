package nasi_bergizi_pajak.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.dao.IngredientDAO;
import nasi_bergizi_pajak.dao.RecipeDAO;
import nasi_bergizi_pajak.dao.RecipeIngredientDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.model.Ingredient;
import nasi_bergizi_pajak.model.Recipe;
import nasi_bergizi_pajak.model.RecipeIngredient;
import nasi_bergizi_pajak.controller.RecipeFormController;
import java.io.IOException;
import java.util.List;
import javafx.collections.transformation.FilteredList;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label emailLabel;

    // Sidebar buttons
    @FXML private Button btnResep;
    @FXML private Button btnBahanHarga;
    @FXML private Button btnLogout;

    // TabPane and Tabs
    @FXML private TabPane tabPane;
    @FXML private Tab tabResep;
    @FXML private Tab tabBahanHarga;

    // Recipe Table
    @FXML private TableView<Recipe> recipeTable;
    @FXML private TextField txtSearchRecipe;
    @FXML private TableColumn<Recipe, Integer> colRecipeId;
    @FXML private TableColumn<Recipe, String> colRecipeName;
    @FXML private TableColumn<Recipe, String> colRecipeDescription;
    @FXML private TableColumn<Recipe, Integer> colServingSize;
    @FXML private TableColumn<Recipe, String> colStatus;

    // Ingredient Table
    @FXML private TableView<Ingredient> ingredientTable;
    @FXML private TableColumn<Ingredient, Integer> colIngredientId;
    @FXML private TableColumn<Ingredient, String> colIngredientName;
    @FXML private TableColumn<Ingredient, String> colUnit;

    @FXML private TextField txtIngredientName;
    @FXML private TextField txtUnit;
    @FXML private Button btnSaveIngredient;
    @FXML private Button btnDeleteIngredient;
    @FXML private Button btnClearIngredient;


    // Detail Card
    @FXML private VBox detailCard;
    @FXML private Label lblDetailRecipeName;
    @FXML private Label lblDetailDescription;
    @FXML private Label lblDetailServingSize;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailIngredients;

    // DAOs
    private RecipeDAO recipeDAO = new RecipeDAO();
    private IngredientDAO ingredientDAO = new IngredientDAO();
    private RecipeIngredientDAO recipeIngredientDAO = new RecipeIngredientDAO();

    // ObservableLists
    private ObservableList<Recipe> recipeList = FXCollections.observableArrayList();
    private ObservableList<Ingredient> ingredientList = FXCollections.observableArrayList();
    
    @FXML
    private void initialize() {
        Akun akun = AppNavigator.getCurrentUser();
        if (akun == null) {
            welcomeLabel.setText("Dashboard Admin");
            emailLabel.setText("");
            return;
        }

        String nama = akun.getFirstName();
        if (akun.getLastName() != null && !akun.getLastName().isBlank()) {
            nama += " " + akun.getLastName();
        }

        welcomeLabel.setText("Halo Admin, " + nama + "!");
        emailLabel.setText(akun.getEmail());

        setupRecipeTable();
        setupIngredientTable();
        loadData();
        if (!recipeList.isEmpty()) {
            recipeTable.getSelectionModel().selectFirst();
        }
    }

    private void setupRecipeTable() {
        colRecipeId.setCellValueFactory(new PropertyValueFactory<>("recipeId"));
        colRecipeName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRecipeDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colServingSize.setCellValueFactory(new PropertyValueFactory<>("servingSize"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        setupActionColumn();
        FilteredList<Recipe> filteredData =
            new FilteredList<>(recipeList, b -> true);

        txtSearchRecipe.textProperty().addListener((obs, oldValue, newValue) -> {

            filteredData.setPredicate(recipe -> {

                if (newValue == null || newValue.isBlank()) {
                    return true;
                }

                String keyword = newValue.toLowerCase();

                return recipe.getName()
                    .toLowerCase()
                    .contains(keyword);
            });
        });
        
        recipeTable.setItems(filteredData);
        recipeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateDetailCard(newSelection);
                }
        });
    }

    private void setupIngredientTable() {
        colIngredientId.setCellValueFactory(new PropertyValueFactory<>("ingredientId"));
        colIngredientName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));

        ingredientTable.setItems(ingredientList);
        ingredientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateIngredientForm(newSelection);
            }
        });
    }


    private void loadData() {
        recipeList.clear();
        recipeList.addAll(recipeDAO.listAllRecipes());

        ingredientList.clear();
        ingredientList.addAll(ingredientDAO.listAllIngredients());
    }


    private void populateIngredientForm(Ingredient ingredient) {
        txtIngredientName.setText(ingredient.getName());
        txtUnit.setText(ingredient.getUnit());
    }

    private void updateDetailCard(Recipe recipe) {
        lblDetailRecipeName.setText(recipe.getName());
        lblDetailDescription.setText(recipe.getDescription());
        lblDetailServingSize.setText(String.valueOf(recipe.getServingSize()));
        lblDetailStatus.setText(recipe.getStatus());

        List<RecipeIngredient> ingredients = recipeIngredientDAO.getRecipeIngredientsByRecipeId(recipe.getRecipeId());
        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ri : ingredients) {
            Ingredient ing = ingredientDAO.getIngredientById(ri.getIngredientId());
            if (ing != null) {
                sb.append(ing.getName()).append(": ").append(ri.getAmount()).append(" ").append(ing.getUnit()).append("\n");
            }
        }
        lblDetailIngredients.setText(sb.toString());
    }



    @FXML
    private void handleSaveIngredient() {
        try {
            String name = txtIngredientName.getText();
            String unit = txtUnit.getText();

            if (name == null || name.isBlank() || unit == null || unit.isBlank()) {
                showAlert("Error", "Nama dan satuan tidak boleh kosong");
                return;
            }

            Ingredient selectedIngredient = ingredientTable.getSelectionModel().getSelectedItem();
            if (selectedIngredient != null) {
                // Update
                selectedIngredient.setName(name);
                selectedIngredient.setUnit(unit);
                ingredientDAO.updateIngredient(selectedIngredient);
            } else {
                // Insert
                Ingredient newIngredient = new Ingredient(0, name, unit);
                ingredientDAO.insertIngredient(newIngredient);
            }

            loadData();
            clearIngredientForm();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteIngredient() {
        Ingredient selectedIngredient = ingredientTable.getSelectionModel().getSelectedItem();
        if (selectedIngredient != null) {
            ingredientDAO.deleteIngredient(selectedIngredient.getIngredientId());
            loadData();
            clearIngredientForm();
        }
    }

    @FXML
    private void handleClearIngredient() {
        clearIngredientForm();
        ingredientTable.getSelectionModel().clearSelection();
    }

    private void clearIngredientForm() {
        txtIngredientName.clear();
        txtUnit.clear();
    }

    @FXML
    private void handleResep() {
        tabPane.getSelectionModel().select(tabResep);
    }

    @FXML
    private void handleBahanHarga() {
        tabPane.getSelectionModel().select(tabBahanHarga);
    }

    @FXML
    private void handleLogout() {
        AppNavigator.showLogin();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleOpenRecipeForm() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/RecipeFormView.fxml")
            );

            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Form Resep");

            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(recipeTable.getScene().getWindow());

            Scene scene = new Scene(root);
            dialog.setScene(scene);

            dialog.setResizable(false);
            dialog.showAndWait();
            loadData();

        } catch (IOException e) {
            AppNavigator.showFatalError(
                "Gagal membuka form",
                e.getMessage()
            );
        }
    }

    @FXML
    private TableColumn<Recipe, Void> colActions;

    private void setupActionColumn() {

    colActions.setCellFactory(param -> new TableCell<>() {

        private final Button btnEdit =
            new Button("✏");

        private final Button btnToggle =
            new Button("Nonaktifkan");

        private final HBox pane =
            new HBox(8, btnToggle, btnEdit);

        {

            btnEdit.getStyleClass()
                .add("table-edit-button");

            btnToggle.getStyleClass()
                .add("table-danger-button");
            
            btnEdit.setFocusTraversable(false);
            btnToggle.setFocusTraversable(false);
            
            btnEdit.setOnAction(event -> {

                Recipe recipe =
                    getTableView().getItems().get(getIndex());

                openEditRecipe(recipe);
            });

            btnToggle.setOnAction(event -> {

                Recipe recipe =
                    getTableView().getItems().get(getIndex());

                toggleRecipeStatus(recipe);
            });
        }

        @Override
    protected void updateItem(Void item, boolean empty) {

        super.updateItem(item, empty);

        if (empty) {

            setGraphic(null);

        } else {

            Recipe recipe =
                getTableView().getItems().get(getIndex());

            if (recipe.getStatus()
                    .equalsIgnoreCase("ACTIVE")) {

                btnToggle.setText("Nonaktifkan");

            } else {

                btnToggle.setText("Aktifkan");
            }

            setGraphic(pane);
        }
    }
    });
    }
    private void openEditRecipe(Recipe recipe) {

    try {

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/RecipeFormView.fxml")
        );

        Parent root = loader.load();

        RecipeFormController controller =
            loader.getController();

        controller.setRecipe(recipe);

        Stage dialog = new Stage();

        dialog.setTitle("Edit Resep");

        dialog.initModality(Modality.APPLICATION_MODAL);

        dialog.initOwner(
            recipeTable.getScene().getWindow()
        );

        Scene scene = new Scene(root);

        dialog.setScene(scene);

        dialog.setResizable(false);

        dialog.showAndWait();

        loadData();

    } catch (IOException e) {

        AppNavigator.showFatalError(
            "Gagal membuka form edit",
            e.getMessage()
        );
    }
    }
    private void toggleRecipeStatus(Recipe recipe) {

        if (recipe.getStatus().equalsIgnoreCase("ACTIVE")) {
            recipe.setStatus("INACTIVE");
        } else {
            recipe.setStatus("ACTIVE");
        }

        recipeDAO.updateRecipe(recipe);

        recipeTable.refresh();
        updateDetailCard(recipe);
    }
    
}
