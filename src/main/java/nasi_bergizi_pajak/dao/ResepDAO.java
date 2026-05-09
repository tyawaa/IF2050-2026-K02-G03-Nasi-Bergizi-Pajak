package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.KebutuhanGizi;
import nasi_bergizi_pajak.model.Resep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResepDAO {

    public List<Resep> getResepAktif() throws SQLException {
        String sql = "SELECT recipe_id, name, description, serving_size, status FROM recipe WHERE status = 'active'";

        List<Resep> daftarResep = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Resep resep = new Resep(
                        rs.getInt("recipe_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("serving_size"),
                        rs.getString("status")
                );

                daftarResep.add(resep);
            }
        }

        return daftarResep;
    }

    public KebutuhanGizi hitungNilaiGiziResep(int recipeId) throws SQLException {
        String sql = """
                SELECT 
                    ri.amount,
                    n.calories,
                    n.protein,
                    n.carbohydrate,
                    n.fat,
                    n.fibre
                FROM recipe_ingredient ri
                JOIN ingredient_nutrition n 
                    ON ri.ingredient_id = n.ingredient_id
                WHERE ri.recipe_id = ?
                """;

        KebutuhanGizi total = new KebutuhanGizi();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, recipeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double amount = rs.getDouble("amount");

                    double faktor = amount / 100.0;

                    total.setKalori(total.getKalori() + rs.getDouble("calories") * faktor);
                    total.setProtein(total.getProtein() + rs.getDouble("protein") * faktor);
                    total.setKarbohidrat(total.getKarbohidrat() + rs.getDouble("carbohydrate") * faktor);
                    total.setLemak(total.getLemak() + rs.getDouble("fat") * faktor);
                    total.setSerat(total.getSerat() + rs.getDouble("fibre") * faktor);
                }
            }
        }

        return total;
    }

    public double hitungEstimasiHargaResep(int recipeId) throws SQLException {
        String sql = """
                SELECT 
                    ri.amount,
                    ip.price
                FROM recipe_ingredient ri
                JOIN ingredient_price ip
                    ON ri.ingredient_id = ip.ingredient_id
                WHERE ri.recipe_id = ?
                  AND ip.effective_date = (
                        SELECT MAX(ip2.effective_date)
                        FROM ingredient_price ip2
                        WHERE ip2.ingredient_id = ri.ingredient_id
                  )
                """;

        double totalHarga = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, recipeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double amount = rs.getDouble("amount");
                    double harga = rs.getDouble("price");

                    double faktor = amount / 100.0;
                    totalHarga += harga * faktor;
                }
            }
        }

        return totalHarga;
    }
}