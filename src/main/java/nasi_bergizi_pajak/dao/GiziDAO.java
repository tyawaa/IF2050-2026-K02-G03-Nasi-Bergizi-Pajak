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
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class GiziDAO {

    public KebutuhanGizi hitungKebutuhanGiziKeluarga(int userId) throws SQLException {
        String sql = """
                SELECT birth_date, height, weight
                FROM family_member
                WHERE user_id = ?
                """;

        KebutuhanGizi total = new KebutuhanGizi();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date birthDate = rs.getDate("birth_date");
                    double height = rs.getDouble("height");
                    double weight = rs.getDouble("weight");

                    int usia = hitungUsia(birthDate);
                    KebutuhanGizi kebutuhanPerOrang = estimasiKebutuhanGizi(usia, height, weight);
                    total.tambah(kebutuhanPerOrang);
                }
            }
        }

        return total;
    }

    public int hitungJumlahAnggotaKeluarga(int userId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS jumlah
                FROM family_member
                WHERE user_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("jumlah");
                }
            }
        }

        return 0;
    }

    public Set<String> ambilAlergiKeluarga(int userId) throws SQLException {
        String sql = """
                SELECT allergy
                FROM family_member
                WHERE user_id = ?
                  AND allergy IS NOT NULL
                  AND TRIM(allergy) <> ''
                """;

        Set<String> allergies = new LinkedHashSet<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String allergyText = rs.getString("allergy");
                    if (allergyText == null || allergyText.isBlank()) {
                        continue;
                    }

                    String[] tokens = allergyText.split(",");
                    for (String token : tokens) {
                        String normalized = token.trim().toLowerCase(Locale.ROOT);
                        if (!normalized.isBlank()) {
                            allergies.add(normalized);
                        }
                    }
                }
            }
        }

        return allergies;
    }

    private int hitungUsia(Date birthDate) {
        if (birthDate == null) {
            return 18;
        }

        LocalDate tanggalLahir = birthDate.toLocalDate();
        return Math.max(0, Period.between(tanggalLahir, LocalDate.now()).getYears());
    }

    private KebutuhanGizi estimasiKebutuhanGizi(int usia, double tinggiBadan, double beratBadan) {
        if (usia < 5) {
            return new KebutuhanGizi(1200, 20, 160, 40, 15);
        }

        if (usia < 13) {
            return new KebutuhanGizi(1800, 35, 250, 55, 22);
        }

        if (usia < 18) {
            return new KebutuhanGizi(2200, 50, 300, 65, 28);
        }

        if (tinggiBadan <= 0 || beratBadan <= 0) {
            return new KebutuhanGizi(2000, 50, 275, 60, 25);
        }

        double bmrPerkiraan = 10.0 * beratBadan
                + 6.25 * tinggiBadan
                - 5.0 * usia
                - 78.0;

        double kalori = Math.max(1200.0, bmrPerkiraan * 1.2);
        double protein = Math.max(50.0, beratBadan * 0.8);
        double karbohidrat = (kalori * 0.55) / 4.0;
        double lemak = (kalori * 0.25) / 9.0;
        double serat = Math.max(25.0, (kalori / 1000.0) * 14.0);

        return new KebutuhanGizi(kalori, protein, karbohidrat, lemak, serat);
    }
}