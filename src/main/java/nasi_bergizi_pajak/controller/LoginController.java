package nasi_bergizi_pajak.controller;

import nasi_bergizi_pajak.dao.AkunDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.util.PasswordUtil;
import nasi_bergizi_pajak.util.ValidationUtil;

import java.sql.SQLException;

public class LoginController {
    private final AkunDAO akunDAO;

    public LoginController() {
        this.akunDAO = new AkunDAO();
    }

    public LoginController(AkunDAO akunDAO) {
        this.akunDAO = akunDAO;
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
