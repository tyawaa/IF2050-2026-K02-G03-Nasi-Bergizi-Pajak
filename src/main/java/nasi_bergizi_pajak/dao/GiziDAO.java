package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.KebutuhanGizi;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

public class GiziDAO {

    public KebutuhanGizi hitungKebutuhanGiziKeluarga(int userId) throws SQLException {
        String sql = "SELECT birth_date, height, weight FROM family_member WHERE user_id = ?";

        KebutuhanGizi total = new KebutuhanGizi();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date birthDate = rs.getDate("birth_date");
                    double weight = rs.getDouble("weight");

                    int usia = hitungUsia(birthDate);
                    KebutuhanGizi kebutuhanPerOrang = estimasiKebutuhanGizi(usia, weight);
                    total.tambah(kebutuhanPerOrang);
                }
            }
        }

        return total;
    }

    private int hitungUsia(Date birthDate) {
        if (birthDate == null) {
            return 18;
        }

        LocalDate tanggalLahir = birthDate.toLocalDate();
        return Period.between(tanggalLahir, LocalDate.now()).getYears();
    }

    private KebutuhanGizi estimasiKebutuhanGizi(int usia, double beratBadan) {
        double kalori;
        double protein;
        double karbohidrat;
        double lemak;
        double serat;

        if (usia < 5) {
            kalori = 1200;
            protein = 20;
            karbohidrat = 160;
            lemak = 40;
            serat = 15;
        } else if (usia < 13) {
            kalori = 1800;
            protein = 35;
            karbohidrat = 250;
            lemak = 55;
            serat = 22;
        } else if (usia < 18) {
            kalori = 2200;
            protein = 50;
            karbohidrat = 300;
            lemak = 65;
            serat = 28;
        } else {
            kalori = 2000;
            protein = Math.max(50, beratBadan * 0.8);
            karbohidrat = 300;
            lemak = 65;
            serat = 25;
        }

        return new KebutuhanGizi(kalori, protein, karbohidrat, lemak, serat);
    }
}