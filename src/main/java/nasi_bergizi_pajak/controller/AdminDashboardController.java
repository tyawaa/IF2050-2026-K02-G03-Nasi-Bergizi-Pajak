package nasi_bergizi_pajak.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.dao.IngredientDAO;
import nasi_bergizi_pajak.dao.RecipeDAO;
import nasi_bergizi_pajak.dao.RecipeIngredientDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.model.Ingredient;
import nasi_bergizi_pajak.model.Recipe;
import nasi_bergizi_pajak.model.RecipeIngredient;

import java.util.List;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label emailLabel;

    // Sidebar buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnResep;
    @FXML private Button btnBahanHarga;
    @FXML private Button btnLogout;

    // TabPane and Tabs
    @FXML private TabPane tabPane;
    @FXML private Tab tabResep;
    @FXML private Tab tabBahanHarga;

    // Recipe Table and Form
    @FXML private TableView<Recipe> recipeTable;
    @FXML private TableColumn<Recipe, Integer> colRecipeId;
    @FXML private TableColumn<Recipe, String> colRecipeName;
    @FXML private TableColumn<Recipe, String> colRecipeDescription;
    @FXML private TableColumn<Recipe, Integer> colServingSize;
    @FXML private TableColumn<Recipe, String> colStatus;

    @FXML private TextField txtRecipeName;
    @FXML private TextArea txtRecipeDescription;
    @FXML private TextField txtServingSize;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnSaveRecipe;
    @FXML private Button btnDeleteRecipe;
    @FXML private Button btnClearRecipe;

    // Ingredient Table and Form
    @FXML private TableView<Ingredient> ingredientTable;
    @FXML private TableColumn<Ingredient, Integer> colIngredientId;
    @FXML private TableColumn<Ingredient, String> colIngredientName;
    @FXML private TableColumn<Ingredient, String> colUnit;
    @FXML private TableColumn<Ingredient, Double> colPricePerUnit;

    @FXML private TextField txtIngredientName;
    @FXML private TextField txtUnit;
    @FXML private TextField txtPricePerUnit;
    @FXML private Button btnSaveIngredient;
    @FXML private Button btnDeleteIngredient;
    @FXML private Button btnClearIngredient;

    // Recipe Ingredient Management
    @FXML private ComboBox<Recipe> cbRecipeForIngredient;
    @FXML private ComboBox<Ingredient> cbIngredientForRecipe;
    @FXML private TextField txtQuantity;
    @FXML private Button btnAddIngredientToRecipe;
    @FXML private Button btnRemoveIngredientFromRecipe;
    @FXML private ListView<String> lvRecipeIngredients;

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
        setupComboBoxes();
        loadData();
    }

    private void setupRecipeTable() {
        colRecipeId.setCellValueFactory(new PropertyValueFactory<>("recipeId"));
        colRecipeName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRecipeDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colServingSize.setCellValueFactory(new PropertyValueFactory<>("servingSize"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        recipeTable.setItems(recipeList);
        recipeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateRecipeForm(newSelection);
                updateDetailCard(newSelection);
            }
        });
    }

    private void setupIngredientTable() {
        colIngredientId.setCellValueFactory(new PropertyValueFactory<>("ingredientId"));
        colIngredientName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colPricePerUnit.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));

        ingredientTable.setItems(ingredientList);
        ingredientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateIngredientForm(newSelection);
            }
        });
    }

    private void setupComboBoxes() {
        cbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
        cbRecipeForIngredient.setItems(recipeList);
        cbIngredientForRecipe.setItems(ingredientList);
    }

    private void loadData() {
        recipeList.clear();
        recipeList.addAll(recipeDAO.listAllRecipes());

        ingredientList.clear();
        ingredientList.addAll(ingredientDAO.listAllIngredients());
    }

    private void populateRecipeForm(Recipe recipe) {
        txtRecipeName.setText(recipe.getName());
        txtRecipeDescription.setText(recipe.getDescription());
        txtServingSize.setText(String.valueOf(recipe.getServingSize()));
        cbStatus.setValue(recipe.getStatus());
    }

    private void populateIngredientForm(Ingredient ingredient) {
        txtIngredientName.setText(ingredient.getName());
        txtUnit.setText(ingredient.getUnit());
        txtPricePerUnit.setText(String.valueOf(ingredient.getPricePerUnit()));
    }

    private void updateDetailCard(Recipe recipe) {
        lblDetailRecipeName.setText(recipe.getName());
        lblDetailDescription.setText(recipe.getDescription());
        lblDetailServingSize.setText("Porsi: " + recipe.getServingSize());
        lblDetailStatus.setText("Status: " + recipe.getStatus());

        List<RecipeIngredient> ingredients = recipeIngredientDAO.getRecipeIngredientsByRecipeId(recipe.getRecipeId());
        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ri : ingredients) {
            Ingredient ing = ingredientDAO.getIngredientById(ri.getIngredientId());
            if (ing != null) {
                sb.append(ing.getName()).append(": ").append(ri.getQuantity()).append(" ").append(ing.getUnit()).append("\n");
            }
        }
        lblDetailIngredients.setText(sb.toString());
    }

    @FXML
    private void handleSaveRecipe() {
        try {
            String name = txtRecipeName.getText();
            String description = txtRecipeDescription.getText();
            int servingSize = Integer.parseInt(txtServingSize.getText());
            String status = cbStatus.getValue();

            Recipe selectedRecipe = recipeTable.getSelectionModel().getSelectedItem();
            if (selectedRecipe != null) {
                // Update
                selectedRecipe.setName(name);
                selectedRecipe.setDescription(description);
                selectedRecipe.setServingSize(servingSize);
                selectedRecipe.setStatus(status);
                recipeDAO.updateRecipe(selectedRecipe);
            } else {
                // Insert
                Recipe newRecipe = new Recipe(0, name, description, servingSize, status);
                recipeDAO.insertRecipe(newRecipe);
            }

            loadData();
            clearRecipeForm();
        } catch (NumberFormatException e) {
            showAlert("Error", "Serving size must be a number");
        }
    }

    @FXML
    private void handleDeleteRecipe() {
        Recipe selectedRecipe = recipeTable.getSelectionModel().getSelectedItem();
        if (selectedRecipe != null) {
            recipeDAO.deleteRecipe(selectedRecipe.getRecipeId());
            loadData();
            clearRecipeForm();
        }
    }

    @FXML
    private void handleClearRecipe() {
        clearRecipeForm();
        recipeTable.getSelectionModel().clearSelection();
    }

    private void clearRecipeForm() {
        txtRecipeName.clear();
        txtRecipeDescription.clear();
        txtServingSize.clear();
        cbStatus.setValue(null);
    }

    @FXML
    private void handleSaveIngredient() {
        try {
            String name = txtIngredientName.getText();
            String unit = txtUnit.getText();
            double price = Double.parseDouble(txtPricePerUnit.getText());

            Ingredient selectedIngredient = ingredientTable.getSelectionModel().getSelectedItem();
            if (selectedIngredient != null) {
                // Update
                selectedIngredient.setName(name);
                selectedIngredient.setUnit(unit);
                selectedIngredient.setPricePerUnit(price);
                ingredientDAO.updateIngredient(selectedIngredient);
            } else {
                // Insert
                Ingredient newIngredient = new Ingredient(0, name, unit, price);
                ingredientDAO.insertIngredient(newIngredient);
            }

            loadData();
            clearIngredientForm();
        } catch (NumberFormatException e) {
            showAlert("Error", "Price per unit must be a number");
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
        txtPricePerUnit.clear();
    }

    @FXML
    private void handleAddIngredientToRecipe() {
        Recipe selectedRecipe = cbRecipeForIngredient.getValue();
        Ingredient selectedIngredient = cbIngredientForRecipe.getValue();
        try {
            double quantity = Double.parseDouble(txtQuantity.getText());
            if (selectedRecipe != null && selectedIngredient != null) {
                RecipeIngredient ri = new RecipeIngredient(0, selectedRecipe.getRecipeId(), selectedIngredient.getIngredientId(), quantity);
                recipeIngredientDAO.insertRecipeIngredient(ri);
                updateDetailCard(selectedRecipe);
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Quantity must be a number");
        }
    }

    @FXML
    private void handleRemoveIngredientFromRecipe() {
        // Implement removal logic if needed
    }

    @FXML
    private void handleDashboard() {
        // Already on dashboard
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
}
