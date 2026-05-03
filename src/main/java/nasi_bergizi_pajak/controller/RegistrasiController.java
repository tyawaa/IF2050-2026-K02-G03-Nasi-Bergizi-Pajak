package nasi_bergizi_pajak.controller;

import nasi_bergizi_pajak.dao.AkunDAO;
import nasi_bergizi_pajak.model.Akun;
import nasi_bergizi_pajak.util.PasswordUtil;
import nasi_bergizi_pajak.util.ValidationUtil;

import java.sql.SQLException;

public class RegistrasiController {
    private final AkunDAO akunDAO;

    public RegistrasiController() {
        this.akunDAO = new AkunDAO();
    }

    public RegistrasiController(AkunDAO akunDAO) {
        this.akunDAO = akunDAO;
    }

    public Akun prosesRegistrasi(String email, String password, String konfirmasiPassword,
                                 String firstName, String lastName, String profileImageName) throws SQLException {
        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(password)
                || ValidationUtil.isBlank(konfirmasiPassword) || ValidationUtil.isBlank(firstName)) {
            throw new IllegalArgumentException("Data registrasi belum lengkap.");
        }

        email = email.trim().toLowerCase();
        firstName = firstName.trim();
        lastName = lastName == null ? null : lastName.trim();

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
}
