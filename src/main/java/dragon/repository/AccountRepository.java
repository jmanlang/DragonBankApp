package dragon.repository;

import dragon.entity.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AccountRepository {

    public Account findById(Connection connection, UUID accountId) throws SQLException {
        String checkingSql = "SELECT id, owner, balance FROM CheckingAccount WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(checkingSql)) {
            statement.setString(1, accountId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAccount(resultSet);
                }
            }
        }

        String savingSql = "SELECT id, owner, balance FROM SavingAccount WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(savingSql)) {
            statement.setString(1, accountId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAccount(resultSet);
                }
            }
        }

        return null;
    }


    public Account findByOwnerId(Connection connection, UUID ownerId) throws SQLException {
        String checkingSql = "SELECT id, owner, balance FROM CheckingAccount WHERE owner = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(checkingSql)) {
            statement.setString(1, ownerId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAccount(resultSet);
                }
            }
        }

        String savingSql = "SELECT id, owner, balance FROM SavingAccount WHERE owner = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(savingSql)) {
            statement.setString(1, ownerId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAccount(resultSet);
                }
            }
        }

        return null;
    }


    public void createCheckingAccount(Connection connection, Account account) throws SQLException {
        String sql = "INSERT INTO CheckingAccount (id, balance, owner) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account.getId().toString());
            statement.setBigDecimal(2, account.getBalance());
            statement.setString(3, account.getOwnerId().toString());
            statement.executeUpdate();
        }
    }

    public boolean updateBalance(Connection connection, UUID accountId, BigDecimal newBalance) throws SQLException {
        if (newBalance == null || newBalance.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        String checkingSql = "UPDATE CheckingAccount SET balance = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(checkingSql)) {
            statement.setBigDecimal(1, newBalance);
            statement.setString(2, accountId.toString());
            if (statement.executeUpdate() == 1) {
                return true;
            }
        }

        String savingSql = "UPDATE SavingAccount SET balance = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(savingSql)) {
            statement.setBigDecimal(1, newBalance);
            statement.setString(2, accountId.toString());
            return statement.executeUpdate() == 1;
        }
    }

    private Account mapAccount(ResultSet resultSet) throws SQLException {
        String owner = resultSet.getString("owner");
        return new Account(
                UUID.fromString(resultSet.getString("id")),
                owner == null ? null : UUID.fromString(owner),
                resultSet.getBigDecimal("balance")
        );
    }
}
