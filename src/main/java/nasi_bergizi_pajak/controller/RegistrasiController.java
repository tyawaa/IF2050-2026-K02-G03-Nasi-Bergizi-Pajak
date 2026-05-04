package nasi_bergizi_pajak.controller;

import java.io.File;
import java.sql.SQLException;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import nasi_bergizi_pajak.app.AppNavigator;
import nasi_bergizi_pajak.dao.AkunDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.util.PasswordUtil;
import nasi_bergizi_pajak.util.ValidationUtil;

public class RegistrasiController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField visibleConfirmPasswordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label errorLabel;
    @FXML private Label profileImageLabel;
    @FXML private Button registerButton;

    private final AkunDAO akunDAO;
    private String selectedProfileImageName;

    public RegistrasiController() {
        this.akunDAO = new AkunDAO();
    }

    public RegistrasiController(AkunDAO akunDAO) {
        this.akunDAO = akunDAO;
    }

    @FXML
    private void initialize() {
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        visibleConfirmPasswordField.setManaged(false);
        visibleConfirmPasswordField.setVisible(false);
        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        clearMessage();
    }

    @FXML
    private void handleTogglePassword() {
        boolean show = showPasswordCheckBox.isSelected();
        setPasswordVisibility(passwordField, visiblePasswordField, show);
        setPasswordVisibility(confirmPasswordField, visibleConfirmPasswordField, show);
    }

    @FXML
    private void handleChooseProfileImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Gambar PNG/JPG", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(profileImageLabel.getScene().getWindow());
        if (selectedFile != null) {
            selectedProfileImageName = selectedFile.getName();
            profileImageLabel.setText(selectedProfileImageName);
        }
    }

    @FXML
    private void handleRegister() {
        clearMessage();
        setLoading(true);

        try {
            prosesRegistrasi(
                    emailField.getText(),
                    getPasswordValue(),
                    getConfirmPasswordValue(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    selectedProfileImageName
            );

            setMessage("Registrasi berhasil. Kamu akan diarahkan ke halaman login.", false);
            PauseTransition delay = new PauseTransition(Duration.millis(900));
            delay.setOnFinished(event -> AppNavigator.showLogin());
            delay.play();
        } catch (SQLException e) {
            setMessage("Kesalahan koneksi database. Pastikan SQLite dan database/init.sql sudah benar.", true);
            e.printStackTrace();
            setLoading(false);
        } catch (IllegalArgumentException e) {
            setMessage(e.getMessage(), true);
            setLoading(false);
        }
    }

    @FXML
    private void goToLogin() {
        AppNavigator.showLogin();
    }

    public Akun prosesRegistrasi(String email, String password, String konfirmasiPassword,
                                 String firstName, String lastName, String profileImageName) throws SQLException {
        if (ValidationUtil.isBlank(firstName)) {
            throw new IllegalArgumentException("Nama depan wajib diisi.");
        }

        if (ValidationUtil.isBlank(lastName)) {
            throw new IllegalArgumentException("Nama belakang wajib diisi.");
        }

        if (ValidationUtil.isBlank(email)) {
            throw new IllegalArgumentException("Email wajib diisi.");
        }

        if (ValidationUtil.isBlank(password)) {
            throw new IllegalArgumentException("Password wajib diisi.");
        }

        if (ValidationUtil.isBlank(konfirmasiPassword)) {
            throw new IllegalArgumentException("Konfirmasi password wajib diisi.");
        }

        email = email.trim().toLowerCase();
        firstName = firstName.trim();
        lastName = lastName.trim();

        if (!ValidationUtil.validasiFormatEmail(email)) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }

        if (!ValidationUtil.validasiPassword(password)) {
            throw new IllegalArgumentException("Password minimal 8 karakter.");
        }

        if (!password.equals(konfirmasiPassword)) {
            throw new IllegalArgumentException("Konfirmasi password tidak sesuai.");
        }

        if (akunDAO.cekEmailTerdaftar(email)) {
            throw new IllegalArgumentException("Email sudah terdaftar.");
        }

        String passwordTerenkripsi = PasswordUtil.hashPassword(password);
        Akun akunBaru = new Akun(email, passwordTerenkripsi, firstName, lastName, true, profileImageName);
        return akunDAO.simpanAkun(akunBaru);
    }

    private void setPasswordVisibility(PasswordField passwordField, TextField visibleField, boolean show) {
        visibleField.setManaged(show);
        visibleField.setVisible(show);
        passwordField.setManaged(!show);
        passwordField.setVisible(!show);
    }

    private String getPasswordValue() {
        return showPasswordCheckBox.isSelected() ? visiblePasswordField.getText() : passwordField.getText();
    }

    private String getConfirmPasswordValue() {
        return showPasswordCheckBox.isSelected() ? visibleConfirmPasswordField.getText() : confirmPasswordField.getText();
    }

    private void setLoading(boolean loading) {
        if (registerButton != null) {
            registerButton.setDisable(loading);
            registerButton.setText(loading ? "Mendaftarkan..." : "Daftar");
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
