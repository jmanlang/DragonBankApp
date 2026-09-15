package dragon.repository;

import dragon.entity.TransferTransaction;
import dragon.entity.WithdrawalTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransferTransactionRepository {
    public void save(Connection connection, TransferTransaction transaction) throws SQLException {
        String sql = "INSERT INTO TransferTransaction (id, userId, fromAccount, toAccount, amount, date) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transaction.getId().toString());
            statement.setString(2, transaction.getUserId().toString());
            statement.setString(3, transaction.getFromAccount().toString());
            statement.setString(4, transaction.getToAccount().toString());
            statement.setDouble(5, transaction.getAmount());
            statement.setString(6, transaction.getDate().toString());
            statement.executeUpdate();
        }
    }
}
