package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.MenuMingguan;
import nasi_bergizi_pajak.model.SlotMakan;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MenuMingguanDAO {

    public int simpanMenuMingguan(MenuMingguan menu) throws SQLException {
        String sql = """
                INSERT INTO weekly_menu
                (user_id, parameter_id, week_start_date, week_end_date, total_estimation, status_budget, created_datetime)
                VALUES (?, ?, ?, ?, ?, ?, NOW())
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, menu.getUserId());
            stmt.setInt(2, menu.getParameterId());
            stmt.setDate(3, Date.valueOf(menu.getWeekStartDate()));
            stmt.setDate(4, Date.valueOf(menu.getWeekEndDate()));
            stmt.setDouble(5, menu.getTotalEstimation());
            stmt.setString(6, menu.getStatusBudget());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int menuId = keys.getInt(1);
                    menu.setMenuId(menuId);
                    return menuId;
                }
            }
        }

        throw new SQLException("Gagal menyimpan menu mingguan.");
    }

    public MenuMingguan cariMenuMingguanById(int menuId) throws SQLException {
        String sql = """
                SELECT menu_id, user_id, parameter_id, week_start_date, week_end_date,
                       total_estimation, status_budget
                FROM weekly_menu
                WHERE menu_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, menuId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapMenuMingguan(rs);
                }
            }
        }

        return null;
    }

    public List<MenuMingguan> cariMenuMingguanByUserId(int userId) throws SQLException {
        String sql = """
                SELECT menu_id, user_id, parameter_id, week_start_date, week_end_date,
                       total_estimation, status_budget
                FROM weekly_menu
                WHERE user_id = ?
                ORDER BY week_start_date DESC
                """;

        List<MenuMingguan> daftarMenu = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    daftarMenu.add(mapMenuMingguan(rs));
                }
            }
        }

        return daftarMenu;
    }

    public List<SlotMakan> cariSlotMakanByMenuId(int menuId) throws SQLException {
        String sql = """
                SELECT slot_id, menu_id, recipe_id, meal_date, meal_time,
                       is_eating_out, outside_cost
                FROM meal_slot
                WHERE menu_id = ?
                ORDER BY meal_date ASC, meal_time ASC
                """;

        List<SlotMakan> daftarSlot = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, menuId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    daftarSlot.add(mapSlotMakan(rs));
                }
            }
        }

        return daftarSlot;
    }

    public boolean slotSudahAda(int menuId, LocalDate mealDate, String mealTime, Integer kecualiSlotId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS jumlah
                FROM meal_slot
                WHERE menu_id = ?
                  AND meal_date = ?
                  AND meal_time = ?
                """;

        if (kecualiSlotId != null) {
            sql += " AND slot_id <> ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, menuId);
            stmt.setDate(2, Date.valueOf(mealDate));
            stmt.setString(3, mealTime);

            if (kecualiSlotId != null) {
                stmt.setInt(4, kecualiSlotId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt("jumlah") > 0;
            }
        }
    }

    public void simpanSlotMakanDanPerbaruiEstimasi(SlotMakan slot) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                simpanSlotMakan(conn, slot);
                hitungUlangEstimasiDanStatus(conn, slot.getMenuId());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void ubahSlotMakanDanPerbaruiEstimasi(SlotMakan slot) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                ubahSlotMakan(conn, slot);
                hitungUlangEstimasiDanStatus(conn, slot.getMenuId());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void hapusSlotMakanDanPerbaruiEstimasi(int menuId, int slotId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                hapusSlotMakan(conn, menuId, slotId);
                hitungUlangEstimasiDanStatus(conn, menuId);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void hitungUlangEstimasiDanStatus(int menuId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                hitungUlangEstimasiDanStatus(conn, menuId);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void simpanSlotMakan(Connection conn, SlotMakan slot) throws SQLException {
        String sql = """
                INSERT INTO meal_slot
                (menu_id, recipe_id, meal_date, meal_time, is_eating_out, outside_cost)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, slot.getMenuId());

            if (slot.getRecipeId() == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, slot.getRecipeId());
            }

            stmt.setDate(3, Date.valueOf(slot.getMealDate()));
            stmt.setString(4, slot.getMealTime());
            stmt.setBoolean(5, slot.isEatingOut());
            stmt.setDouble(6, slot.getOutsideCost());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    slot.setSlotId(keys.getInt(1));
                }
            }
        }
    }

    private void ubahSlotMakan(Connection conn, SlotMakan slot) throws SQLException {
        String sql = """
                UPDATE meal_slot
                SET recipe_id = ?,
                    meal_date = ?,
                    meal_time = ?,
                    is_eating_out = ?,
                    outside_cost = ?
                WHERE slot_id = ?
                  AND menu_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (slot.getRecipeId() == null) {
                stmt.setNull(1, Types.INTEGER);
            } else {
                stmt.setInt(1, slot.getRecipeId());
            }

            stmt.setDate(2, Date.valueOf(slot.getMealDate()));
            stmt.setString(3, slot.getMealTime());
            stmt.setBoolean(4, slot.isEatingOut());
            stmt.setDouble(5, slot.getOutsideCost());
            stmt.setInt(6, slot.getSlotId());
            stmt.setInt(7, slot.getMenuId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Slot makan tidak ditemukan untuk diperbarui.");
            }
        }
    }

    private void hapusSlotMakan(Connection conn, int menuId, int slotId) throws SQLException {
        String sql = """
                DELETE FROM meal_slot
                WHERE menu_id = ?
                  AND slot_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);
            stmt.setInt(2, slotId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Slot makan tidak ditemukan untuk dihapus.");
            }
        }
    }

    private void hitungUlangEstimasiDanStatus(Connection conn, int menuId) throws SQLException {
        double totalEstimasi = hitungTotalEstimasi(conn, menuId);
        Double budget = ambilBudgetMenu(conn, menuId);
        String statusBudget = tentukanStatusBudget(totalEstimasi, budget);

        updateTotalEstimasi(conn, menuId, totalEstimasi, statusBudget);
    }

    private double hitungTotalEstimasi(Connection conn, int menuId) throws SQLException {
        String sql = """
                SELECT slot_id, recipe_id, is_eating_out, outside_cost
                FROM meal_slot
                WHERE menu_id = ?
                """;

        double total = 0;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    boolean eatingOut = rs.getBoolean("is_eating_out");
                    Object recipeObj = rs.getObject("recipe_id");

                    if (eatingOut) {
                        total += rs.getDouble("outside_cost");
                    } else if (recipeObj != null) {
                        int recipeId = ((Number) recipeObj).intValue();
                        total += hitungEstimasiHargaResep(conn, recipeId);
                    }
                }
            }
        }

        return total;
    }

    private double hitungEstimasiHargaResep(Connection conn, int recipeId) throws SQLException {
        String sql = """
                SELECT ri.amount, ip.price
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

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recipeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double amount = rs.getDouble("amount");
                    double harga = rs.getDouble("price");
                    totalHarga += (amount / 100.0) * harga;
                }
            }
        }

        return totalHarga;
    }

    private Double ambilBudgetMenu(Connection conn, int menuId) throws SQLException {
        String sql = """
                SELECT b.amount
                FROM weekly_menu wm
                JOIN parameter_planner pp
                    ON wm.parameter_id = pp.parameter_id
                JOIN budget b
                    ON pp.budget_id = b.budget_id
                WHERE wm.menu_id = ?
                LIMIT 1
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("amount");
                }
            }
        }

        return null;
    }

    private String tentukanStatusBudget(double totalEstimasi, Double budget) {
        if (budget == null) {
            return "draft";
        }

        return totalEstimasi > budget ? "overbudget" : "within_budget";
    }

    private void updateTotalEstimasi(Connection conn, int menuId, double totalEstimasi, String statusBudget) throws SQLException {
        String sql = """
                UPDATE weekly_menu
                SET total_estimation = ?,
                    status_budget = ?
                WHERE menu_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, totalEstimasi);
            stmt.setString(2, statusBudget);
            stmt.setInt(3, menuId);
            stmt.executeUpdate();
        }
    }

    private MenuMingguan mapMenuMingguan(ResultSet rs) throws SQLException {
        MenuMingguan menu = new MenuMingguan();
        menu.setMenuId(rs.getInt("menu_id"));
        menu.setUserId(rs.getInt("user_id"));
        menu.setParameterId(rs.getInt("parameter_id"));
        menu.setWeekStartDate(rs.getDate("week_start_date").toLocalDate());
        menu.setWeekEndDate(rs.getDate("week_end_date").toLocalDate());
        menu.setTotalEstimation(rs.getDouble("total_estimation"));
        menu.setStatusBudget(rs.getString("status_budget"));
        return menu;
    }

    private SlotMakan mapSlotMakan(ResultSet rs) throws SQLException {
        SlotMakan slot = new SlotMakan();
        slot.setSlotId(rs.getInt("slot_id"));
        slot.setMenuId(rs.getInt("menu_id"));

        Object recipeObj = rs.getObject("recipe_id");
        slot.setRecipeId(recipeObj == null ? null : ((Number) recipeObj).intValue());

        slot.setMealDate(rs.getDate("meal_date").toLocalDate());
        slot.setMealTime(rs.getString("meal_time"));
        slot.setEatingOut(rs.getBoolean("is_eating_out"));
        slot.setOutsideCost(rs.getDouble("outside_cost"));
        return slot;
    }
}