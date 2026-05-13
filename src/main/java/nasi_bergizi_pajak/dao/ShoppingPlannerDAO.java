package nasi_bergizi_pajak.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.ShoppingItem;
import nasi_bergizi_pajak.model.ShoppingPlanner;
import nasi_bergizi_pajak.model.WeeklyMenuOption;

public class ShoppingPlannerDAO {
    public List<WeeklyMenuOption> cariMenuMingguanUser(int userId) throws SQLException {
        String sql = """
                SELECT wm.menu_id, wm.week_start_date, wm.week_end_date, wm.status_budget,
                       b.period_start AS budget_start_date,
                       b.period_end AS budget_end_date
                FROM weekly_menu wm
                LEFT JOIN parameter_planner pp ON pp.parameter_id = wm.parameter_id
                LEFT JOIN budget b ON b.budget_id = pp.budget_id
                WHERE wm.user_id = ?
                ORDER BY wm.week_start_date DESC, wm.menu_id DESC
                """;
        List<WeeklyMenuOption> menus = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    menus.add(new WeeklyMenuOption(
                            resultSet.getInt("menu_id"),
                            resultSet.getDate("week_start_date").toLocalDate(),
                            resultSet.getDate("week_end_date").toLocalDate(),
                            resultSet.getDate("budget_start_date") == null
                                    ? null
                                    : resultSet.getDate("budget_start_date").toLocalDate(),
                            resultSet.getDate("budget_end_date") == null
                                    ? null
                                    : resultSet.getDate("budget_end_date").toLocalDate(),
                            resultSet.getString("status_budget")
                    ));
                }
            }
        }

        return menus;
    }

    public ShoppingPlanner susunDaftarBelanja(int userId, int menuId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                validateMenuOwner(connection, userId, menuId);
                int plannerId = getOrCreateDraftPlanner(connection, menuId);
                replacePlannerItems(connection, userId, menuId, plannerId);
                updatePlannerTotal(connection, plannerId);
                ShoppingPlanner planner = cariPlannerById(connection, userId, plannerId);
                connection.commit();
                return planner;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public ShoppingPlanner cariPlannerUntukMenu(int userId, int menuId) throws SQLException {
        String sql = """
                SELECT sp.planner_id
                FROM shopping_planner sp
                JOIN weekly_menu wm ON wm.menu_id = sp.menu_id
                WHERE wm.user_id = ? AND sp.menu_id = ?
                ORDER BY sp.created_datetime DESC, sp.planner_id DESC
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, menuId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return cariPlannerById(connection, userId, resultSet.getInt("planner_id"));
                }
            }
        }

        return null;
    }

    public List<ShoppingItem> cariItems(int userId, int plannerId) throws SQLException {
        String sql = """
                SELECT spi.item_id, spi.planner_id, spi.ingredient_id, i.name AS ingredient_name,
                       spi.required_qty, spi.unit, spi.estimated_price, spi.actual_price, spi.status_beli
                FROM shopping_planner_item spi
                JOIN shopping_planner sp ON sp.planner_id = spi.planner_id
                JOIN weekly_menu wm ON wm.menu_id = sp.menu_id
                JOIN ingredient i ON i.ingredient_id = spi.ingredient_id
                WHERE wm.user_id = ? AND spi.planner_id = ?
                ORDER BY i.name
                """;
        List<ShoppingItem> items = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, plannerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapRowToItem(resultSet));
                }
            }
        }

        return items;
    }

    public ShoppingPlanner prosesHasilBelanja(int userId, int plannerId, List<ShoppingItem> items) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ShoppingPlanner planner = cariPlannerById(connection, userId, plannerId);
                if (planner == null) {
                    throw new SQLException("Planner belanja tidak ditemukan.");
                }
                if ("completed".equalsIgnoreCase(planner.getStatus())) {
                    throw new SQLException("Hasil belanja sudah pernah diproses ke stok dapur.");
                }

                BigDecimal totalActual = BigDecimal.ZERO;
                for (ShoppingItem item : items) {
                    updatePlannerItem(connection, plannerId, item);
                    if (item.isBought()) {
                        totalActual = totalActual.add(item.getActualPrice());
                        syncItemToKitchenStock(connection, userId, item);
                    }
                }

                String updatePlannerSql = """
                        UPDATE shopping_planner
                        SET total_actual = ?, status = 'completed'
                        WHERE planner_id = ?
                        """;
                try (PreparedStatement statement = connection.prepareStatement(updatePlannerSql)) {
                    statement.setBigDecimal(1, totalActual);
                    statement.setInt(2, plannerId);
                    statement.executeUpdate();
                }

                ShoppingPlanner updatedPlanner = cariPlannerById(connection, userId, plannerId);
                connection.commit();
                return updatedPlanner;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void validateMenuOwner(Connection connection, int userId, int menuId) throws SQLException {
        String sql = "SELECT menu_id FROM weekly_menu WHERE user_id = ? AND menu_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, menuId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Menu mingguan tidak ditemukan untuk pengguna ini.");
                }
            }
        }
    }

    private int getOrCreateDraftPlanner(Connection connection, int menuId) throws SQLException {
        String selectSql = """
                SELECT planner_id, status
                FROM shopping_planner
                WHERE menu_id = ?
                ORDER BY created_datetime DESC, planner_id DESC
                LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setInt(1, menuId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    if ("completed".equalsIgnoreCase(resultSet.getString("status"))) {
                        throw new SQLException("Planner belanja untuk menu ini sudah completed.");
                    }
                    return resultSet.getInt("planner_id");
                }
            }
        }

        String insertSql = "INSERT INTO shopping_planner (menu_id, status) VALUES (?, 'draft')";
        try (PreparedStatement statement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, menuId);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Gagal membuat planner belanja.");
    }

    private void replacePlannerItems(Connection connection, int userId, int menuId, int plannerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM shopping_planner_item WHERE planner_id = ?")) {
            statement.setInt(1, plannerId);
            statement.executeUpdate();
        }

        String insertSql = """
                INSERT INTO shopping_planner_item (planner_id, ingredient_id, required_qty, unit, estimated_price)
                SELECT ?, kebutuhan.ingredient_id, kebutuhan.qty_beli, kebutuhan.unit,
                       COALESCE(harga.price, 0) * kebutuhan.qty_beli AS estimated_price
                FROM (
                    SELECT ri.ingredient_id, i.unit,
                           GREATEST(SUM(ri.amount) - COALESCE(stok.quantity, 0), 0) AS qty_beli
                    FROM meal_slot ms
                    JOIN recipe_ingredient ri ON ri.recipe_id = ms.recipe_id
                    JOIN ingredient i ON i.ingredient_id = ri.ingredient_id
                    LEFT JOIN (
                        SELECT ingredient_id, SUM(quantity) AS quantity
                        FROM kitchen_stock
                        WHERE user_id = ?
                        GROUP BY ingredient_id
                    ) stok ON stok.ingredient_id = ri.ingredient_id
                    WHERE ms.menu_id = ? AND ms.is_eating_out = 0
                    GROUP BY ri.ingredient_id, i.unit, stok.quantity
                ) kebutuhan
                LEFT JOIN ingredient_price harga ON harga.price_id = (
                    SELECT ip.price_id
                    FROM ingredient_price ip
                    WHERE ip.ingredient_id = kebutuhan.ingredient_id
                    ORDER BY ip.effective_date DESC, ip.price_id DESC
                    LIMIT 1
                )
                WHERE kebutuhan.qty_beli > 0
                """;
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setInt(1, plannerId);
            statement.setInt(2, userId);
            statement.setInt(3, menuId);
            statement.executeUpdate();
        }
    }

    private void updatePlannerTotal(Connection connection, int plannerId) throws SQLException {
        String sql = """
                UPDATE shopping_planner
                SET total_estimation = COALESCE((
                    SELECT SUM(estimated_price)
                    FROM shopping_planner_item
                    WHERE planner_id = ?
                ), 0)
                WHERE planner_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, plannerId);
            statement.setInt(2, plannerId);
            statement.executeUpdate();
        }
    }

    private ShoppingPlanner cariPlannerById(Connection connection, int userId, int plannerId) throws SQLException {
        String sql = """
                SELECT sp.planner_id, sp.menu_id, sp.created_datetime, sp.total_estimation, sp.total_actual, sp.status,
                       b.amount AS budget_amount
                FROM shopping_planner sp
                JOIN weekly_menu wm ON wm.menu_id = sp.menu_id
                LEFT JOIN parameter_planner pp ON pp.parameter_id = wm.parameter_id
                LEFT JOIN budget b ON b.budget_id = pp.budget_id
                WHERE wm.user_id = ? AND sp.planner_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, plannerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Timestamp created = resultSet.getTimestamp("created_datetime");
                    return new ShoppingPlanner(
                            resultSet.getInt("planner_id"),
                            resultSet.getInt("menu_id"),
                            created == null ? null : created.toLocalDateTime(),
                            resultSet.getBigDecimal("total_estimation"),
                            resultSet.getBigDecimal("total_actual"),
                            resultSet.getString("status"),
                            resultSet.getBigDecimal("budget_amount")
                    );
                }
            }
        }

        return null;
    }

    private void updatePlannerItem(Connection connection, int plannerId, ShoppingItem item) throws SQLException {
        String sql = """
                UPDATE shopping_planner_item
                SET actual_price = ?, status_beli = ?
                WHERE planner_id = ? AND item_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, item.getActualPrice());
            statement.setString(2, item.getStatusBeli());
            statement.setInt(3, plannerId);
            statement.setInt(4, item.getItemId());
            statement.executeUpdate();
        }
    }

    private void syncItemToKitchenStock(Connection connection, int userId, ShoppingItem item) throws SQLException {
        String selectSql = """
                SELECT stock_id
                FROM kitchen_stock
                WHERE user_id = ? AND ingredient_id = ? AND unit = ?
                ORDER BY stock_id
                LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setInt(1, userId);
            statement.setInt(2, item.getIngredientId());
            statement.setString(3, item.getUnit());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String updateSql = "UPDATE kitchen_stock SET quantity = quantity + ? WHERE stock_id = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                        updateStatement.setDouble(1, item.getRequiredQty());
                        updateStatement.setInt(2, resultSet.getInt("stock_id"));
                        updateStatement.executeUpdate();
                    }
                    return;
                }
            }
        }

        String insertSql = """
                INSERT INTO kitchen_stock (user_id, ingredient_id, quantity, unit, storage_location, expiry_date)
                VALUES (?, ?, ?, ?, 'Pantry', ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setInt(1, userId);
            statement.setInt(2, item.getIngredientId());
            statement.setDouble(3, item.getRequiredQty());
            statement.setString(4, item.getUnit());
            statement.setDate(5, (Date) null);
            statement.executeUpdate();
        }
    }

    private ShoppingItem mapRowToItem(ResultSet resultSet) throws SQLException {
        return new ShoppingItem(
                resultSet.getInt("item_id"),
                resultSet.getInt("planner_id"),
                resultSet.getInt("ingredient_id"),
                resultSet.getString("ingredient_name"),
                resultSet.getDouble("required_qty"),
                resultSet.getString("unit"),
                resultSet.getBigDecimal("estimated_price"),
                resultSet.getBigDecimal("actual_price"),
                resultSet.getString("status_beli")
        );
    }
}
