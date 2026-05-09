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

        return -1;
    }

    public void simpanSlotMakan(SlotMakan slot) throws SQLException {
        String sql = """
                INSERT INTO meal_slot
                (menu_id, recipe_id, meal_date, meal_time, is_eating_out, outside_cost)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, slot.getMenuId());

            if (slot.isEatingOut()) {
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(2, slot.getRecipeId());
            }

            stmt.setDate(3, Date.valueOf(slot.getMealDate()));
            stmt.setString(4, slot.getMealTime());
            stmt.setBoolean(5, slot.isEatingOut());
            stmt.setDouble(6, slot.getOutsideCost());

            stmt.executeUpdate();
        }
    }

    public void updateTotalEstimasi(int menuId, double totalEstimasi, String statusBudget) throws SQLException {
        String sql = """
                UPDATE weekly_menu
                SET total_estimation = ?, status_budget = ?
                WHERE menu_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, totalEstimasi);
            stmt.setString(2, statusBudget);
            stmt.setInt(3, menuId);

            stmt.executeUpdate();
        }
    }
}