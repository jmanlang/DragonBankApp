package dragon.repository;

import dragon.entity.CheckingAccount;

import java.util.UUID;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckingAccountRepository {

    public void createCheckingAccount(Connection connection, CheckingAccount checkingAccount) throws SQLException {
        String query = "INSERT INTO CheckingAccount (id, owner, balance) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, checkingAccount.getID().toString());
            preparedStatement.setString(2, checkingAccount.getOwnerID().toString());
            preparedStatement.setDouble(3, checkingAccount.getBalance());
            preparedStatement.executeUpdate();
        }
    }

    public CheckingAccount findByOwnerID(Connection connection, UUID owner) throws SQLException {
        String query = "SELECT id, owner, balance FROM CheckingAccount WHERE owner = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, owner.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new CheckingAccount(
                        UUID.fromString(resultSet.getString("id")),
                        UUID.fromString(resultSet.getString("owner")),
                        resultSet.getDouble("balance")
                );
            }
        }
    }

    public boolean updateCheckingBalance(Connection connection, CheckingAccount checkingAccount, double balance) throws SQLException {
        if (balance < 0) {
           return false;
        }

        String query = "UPDATE CheckingAccount SET balance = ? WHERE owner = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDouble(1, balance);
            preparedStatement.setString(2, checkingAccount.getOwnerID().toString());
            return preparedStatement.executeUpdate() == 1;
        }
    }
}

