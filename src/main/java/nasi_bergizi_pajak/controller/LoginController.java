package nasi_bergizi_pajak.controller;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.dao.AkunDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.util.PasswordUtil;
import nasi_bergizi_pajak.util.ValidationUtil;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AkunDAO akunDAO;

    public LoginController() {
        this.akunDAO = new AkunDAO();
    }

    public LoginController(AkunDAO akunDAO) {
        this.akunDAO = akunDAO;
    }

    @FXML
    private void initialize() {
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        clearMessage();
    }

    @FXML
    private void handleTogglePassword() {
        boolean show = showPasswordCheckBox.isSelected();
        visiblePasswordField.setManaged(show);
        visiblePasswordField.setVisible(show);
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);
    }

    @FXML
    private void handleLogin() {
        clearMessage();
        setLoading(true);

        try {
            Akun akun = prosesLogin(emailField.getText(), getPasswordValue());
            AppNavigator.showDashboard(akun);
        } catch (SQLException e) {
            setMessage("Kesalahan koneksi database. Pastikan SQLite dan database/init.sql sudah benar.", true);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            setMessage(e.getMessage(), true);
        } finally {
            setLoading(false);
        }
    }

    @FXML
    private void goToRegister() {
        AppNavigator.showRegister();
    }

    public Akun prosesLogin(String email, String password) throws SQLException {
        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(password)) {
            throw new IllegalArgumentException("Email dan password wajib diisi.");
        }

        email = email.trim().toLowerCase();

        if (!ValidationUtil.validasiFormatEmail(email)) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }

        Akun akun = akunDAO.cariAkunByEmail(email);
        if (akun == null) {
            throw new IllegalArgumentException("Akun tidak ditemukan. Daftar akun terlebih dahulu.");
        }

        if (!akun.isActive()) {
            throw new IllegalArgumentException("Akun tidak aktif.");
        }

        boolean passwordValid = PasswordUtil.verifyPassword(password, akun.getPassword());
        if (!passwordValid) {
            throw new IllegalArgumentException("Password salah.");
        }

        return akun;
    }

    private String getPasswordValue() {
        return showPasswordCheckBox.isSelected() ? visiblePasswordField.getText() : passwordField.getText();
    }

    private void setLoading(boolean loading) {
        if (loginButton != null) {
            loginButton.setDisable(loading);
            loginButton.setText(loading ? "Memproses..." : "Masuk");
        }
    }

    private void clearMessage() {
        if (errorLabel == null) {
            return;
        }
        errorLabel.setText("");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        errorLabel.getStyleClass().setAll("message-box");
    }

    private void setMessage(String message, boolean error) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
        errorLabel.getStyleClass().setAll("message-box", error ? "message-error" : "message-success");
    }
}
