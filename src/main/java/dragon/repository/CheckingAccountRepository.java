package dragon.repository;

import dragon.entity.CheckingAccount;

import java.util.UUID;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckingAccountRepository {

    public void createCheckingAccount(Connection connection, CheckingAccount checkingAccount) throws SQLException {
        String query = "INSERT INTO CheckingAccounts (id, userID, balance) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, checkingAccount.getID().toString());
            preparedStatement.setString(2, checkingAccount.getUserID().toString());
            preparedStatement.setDouble(3, checkingAccount.getBalance());
            preparedStatement.executeUpdate();
        }
    }

    public CheckingAccount findByUserID(Connection connection, UUID userID) throws SQLException {
        String query = "SELECT id, userID, balance FROM CheckingAccounts WHERE userID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userID.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new CheckingAccount(
                        UUID.fromString(resultSet.getString("id")),
                        UUID.fromString(resultSet.getString("userID")),
                        resultSet.getDouble("balance")
                );
            }
        }
    }

    public boolean updateCheckingBalance(Connection connection, CheckingAccount checkingAccount, Double balance) throws SQLException {
        if (balance == null || balance < 0) {
           return false;
        }

        String query = "UPDATE CheckingAccounts SET balance = ? WHERE userID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDouble(1, balance);
            preparedStatement.setString(2, checkingAccount.getUserID().toString());
            return preparedStatement.executeUpdate() == 1;
        }
    }
}

