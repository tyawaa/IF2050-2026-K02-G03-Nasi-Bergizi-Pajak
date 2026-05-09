package nasi_bergizi_pajak.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    private final RecipeDAO recipeDAO = new RecipeDAO();
    private Recipe currentRecipe;
    @FXML
    private void initialize() {
        cbStatus.getItems().addAll(
            "ACTIVE",
            "INACTIVE"
        );
    }

    @FXML
private void handleSaveRecipe() {

    try {

        String name = txtRecipeName.getText();

        String description =
            txtRecipeDescription.getText();

        int servingSize =
            Integer.parseInt(txtServingSize.getText());

        String status =
            cbStatus.getValue();

        if (currentRecipe != null) {

                // EDIT

            currentRecipe.setName(name);
            currentRecipe.setDescription(description);
            currentRecipe.setServingSize(servingSize);
            currentRecipe.setStatus(status);

            recipeDAO.updateRecipe(currentRecipe);

            } 
        
        else {

            // TAMBAH BARU

            Recipe recipe = new Recipe(
                    0,
                    name,
                    description,
                    servingSize,
                    status
                );

                recipeDAO.insertRecipe(recipe);
            }

            closeWindow();

        } catch (Exception e) {

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

        txtRecipeDescription.setText(
            recipe.getDescription()
        );

        txtServingSize.setText(
            String.valueOf(recipe.getServingSize())
        );

        cbStatus.setValue(recipe.getStatus());
    }
    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}