package nasi_bergizi_pajak.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import nasi_bergizi_pajak.dao.AkunDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.util.PasswordUtil;
import nasi_bergizi_pajak.util.ValidationUtil;

import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private CheckBox showPasswordCheckBox;

    @FXML
    private Label errorLabel;

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
    }

    @FXML
    private void handleTogglePassword() {
        boolean show = showPasswordCheckBox.isSelected();
        visiblePasswordField.setManaged(show);
        visiblePasswordField.setVisible(show);
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);
        passwordField.setText(visiblePasswordField.getText());
    }

    @FXML
    private void handleLogin() {
        errorLabel.setText("");
        try {
            Akun akun = prosesLogin(emailField.getText(), passwordField.isVisible() ? passwordField.getText() : visiblePasswordField.getText());
            errorLabel.setText("Login berhasil: " + akun.getEmail());
        } catch (SQLException e) {
            errorLabel.setText("Kesalahan koneksi database.");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void goToRegister() {
        errorLabel.setText("Halaman registrasi belum diimplementasikan.");
    }

    public Akun prosesLogin(String email, String password) throws SQLException {
        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(password)) {
            throw new IllegalArgumentException("Email atau password belum diisi.");
        }

        email = email.trim().toLowerCase();

        if (!ValidationUtil.validasiFormatEmail(email)) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }

        Akun akun = akunDAO.cariAkunByEmail(email);
        if (akun == null) {
            throw new IllegalArgumentException("Akun tidak ditemukan.");
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
}
