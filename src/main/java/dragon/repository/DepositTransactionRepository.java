package dragon.repository;

import dragon.entity.DepositTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DepositTransactionRepository {
    public void save(Connection connection, DepositTransaction transaction) throws SQLException {
        String sql = "INSERT INTO DepositTransaction (id, userId, amount, date, accountId) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transaction.getId().toString());
            statement.setString(2, transaction.getUserId().toString());
            statement.setDouble(3, transaction.getAmount());
            statement.setString(4, transaction.getDate().toString());
            statement.setString(5, transaction.getAccountId().toString());
            statement.executeUpdate();
        }
    }
}
