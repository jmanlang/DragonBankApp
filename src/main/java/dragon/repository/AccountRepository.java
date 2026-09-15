package dragon.repository;

import dragon.entity.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AccountRepository {
    public Account findByOwnerId(Connection connection, UUID ownerId) throws SQLException {
        String sql = "SELECT id, owner, balance FROM CheckingAccount WHERE owner = ? LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ownerId.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return new Account(
                        UUID.fromString(resultSet.getString("id")),
                        UUID.fromString(resultSet.getString("owner")),
                        resultSet.getDouble("balance")
                );
            }
        }
    }

    public void createCheckingAccount(Connection connection, Account account) throws SQLException {
        String sql = "INSERT INTO CheckingAccount (id, balance, owner) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account.getId().toString());
            statement.setDouble(2, account.getBalance());
            statement.setString(3, account.getOwnerId().toString());
            statement.executeUpdate();
        }
    }

    public void createSavingAccount(Connection connection, Account account) throws SQLException {
        String sql = "INSERT INTO SavingAccount (id, balance, owner) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account.getId().toString());
            statement.setDouble(2, account.getBalance());
            statement.setString(3, account.getOwnerId().toString());
            statement.executeUpdate();
        }
    }

    public boolean updateBalance(Connection connection, UUID accountId, double newBalance) throws SQLException {
        if (newBalance < 0) {
            return false;
        }

        String sql = "UPDATE CheckingAccount SET balance = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, newBalance);
            statement.setString(2, accountId.toString());
            return statement.executeUpdate() == 1;
        }
    }
}
