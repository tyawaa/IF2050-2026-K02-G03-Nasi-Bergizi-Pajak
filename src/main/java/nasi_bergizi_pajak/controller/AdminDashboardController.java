package nasi_bergizi_pajak.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.model.Akun;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label emailLabel;

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
    }

    @FXML
    private void handleLogout() {
        AppNavigator.showLogin();
    }
}
