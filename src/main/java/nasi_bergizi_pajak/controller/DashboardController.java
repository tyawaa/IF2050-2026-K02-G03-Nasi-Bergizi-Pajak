package nasi_bergizi_pajak.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.model.Akun;

public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label emailLabel;

    @FXML
    private void initialize() {
        Akun akun = AppNavigator.getCurrentUser();
        if (akun == null) {
            welcomeLabel.setText("Selamat datang");
            emailLabel.setText("");
            return;
        }

        String nama = akun.getFirstName();
        if (akun.getLastName() != null && !akun.getLastName().isBlank()) {
            nama += " " + akun.getLastName();
        }

        welcomeLabel.setText(nama);
        emailLabel.setText(akun.getEmail());
    }

    @FXML
    private void handleLogout() {
        AppNavigator.showLogin();
    }

    @FXML
    private void handleOpenRecipeForm() {
        AppNavigator.showRecipeForm();
    }
}
