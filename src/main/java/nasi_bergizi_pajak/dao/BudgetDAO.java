package nasi_bergizi_pajak.dao;

import nasi_bergizi_pajak.config.DatabaseConnection;
import nasi_bergizi_pajak.model.Budget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {
    public Budget setBudget(Budget budget) throws SQLException {
        String sql = """
                INSERT INTO budget (user_id, name, amount, period_start, period_end, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            try {
                connection.setAutoCommit(false);

                if (budget.isActive()) {
                    nonaktifkanBudgetAktif(connection, budget.getUserId(), 0);
                }

                try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    fillBudgetStatement(statement, budget);
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            budget.setBudgetId(keys.getInt(1));
                        }
                    }
                }

                connection.commit();
                return budget;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public boolean updateBudget(Budget budget) throws SQLException {
        String sql = """
                UPDATE budget
                SET name = ?, amount = ?, period_start = ?, period_end = ?, status = ?
                WHERE budget_id = ? AND user_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            try {
                connection.setAutoCommit(false);

                if (budget.isActive()) {
                    nonaktifkanBudgetAktif(connection, budget.getUserId(), budget.getBudgetId());
                }

                boolean updated;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, budget.getName());
                    statement.setDouble(2, budget.getAmount());
                    statement.setString(3, budget.getPeriodStart().toString());
                    statement.setString(4, budget.getPeriodEnd().toString());
                    statement.setString(5, budget.getStatus());
                    statement.setInt(6, budget.getBudgetId());
                    statement.setInt(7, budget.getUserId());
                    updated = statement.executeUpdate() > 0;
                }

                connection.commit();
                return updated;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public boolean hapusBudget(int budgetId, int userId) throws SQLException {
        String sql = "DELETE FROM budget WHERE budget_id = ? AND user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, budgetId);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public Budget getBudgetById(int budgetId, int userId) throws SQLException {
        String sql = """
                SELECT budget_id, user_id, name, amount, period_start, period_end, status
                FROM budget
                WHERE budget_id = ? AND user_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, budgetId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRowToBudget(resultSet);
                }
            }
        }

        return null;
    }

    public Budget getBudgetAktif(int userId) throws SQLException {
        String sql = """
                SELECT budget_id, user_id, name, amount, period_start, period_end, status
                FROM budget
                WHERE user_id = ?
                  AND status = 'active'
                  AND ? BETWEEN period_start AND period_end
                ORDER BY period_start DESC, budget_id DESC
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, LocalDate.now().toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRowToBudget(resultSet);
                }
            }
        }

        return null;
    }

    public List<Budget> getDaftarBudget(int userId) throws SQLException {
        String sql = """
                SELECT budget_id, user_id, name, amount, period_start, period_end, status
                FROM budget
                WHERE user_id = ?
                ORDER BY period_start DESC, budget_id DESC
                """;

        List<Budget> budgets = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    budgets.add(mapRowToBudget(resultSet));
                }
            }
        }

        return budgets;
    }

    private void nonaktifkanBudgetAktif(Connection connection, int userId, int excludedBudgetId) throws SQLException {
        String sql = """
                UPDATE budget
                SET status = 'inactive'
                WHERE user_id = ?
                  AND status = 'active'
                  AND budget_id <> ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, excludedBudgetId);
            statement.executeUpdate();
        }
    }

    private void fillBudgetStatement(PreparedStatement statement, Budget budget) throws SQLException {
        statement.setInt(1, budget.getUserId());
        statement.setString(2, budget.getName());
        statement.setDouble(3, budget.getAmount());
        statement.setString(4, budget.getPeriodStart().toString());
        statement.setString(5, budget.getPeriodEnd().toString());
        statement.setString(6, budget.getStatus());
    }

    private Budget mapRowToBudget(ResultSet resultSet) throws SQLException {
        return new Budget(
                resultSet.getInt("budget_id"),
                resultSet.getInt("user_id"),
                resultSet.getString("name"),
                resultSet.getDouble("amount"),
                java.time.LocalDate.parse(resultSet.getString("period_start")),
                java.time.LocalDate.parse(resultSet.getString("period_end")),
                resultSet.getString("status")
        );
    }
}
