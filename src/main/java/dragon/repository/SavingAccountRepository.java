package dragon.repository;

import dragon.entity.SavingAccount;

import java.util.UUID;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SavingAccountRepository {

    public void createSavingAccount(Connection connection, SavingAccount savingAccount) throws SQLException {
        String query = "INSERT INTO SavingAccounts (id, userID, balance, interestRate) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, savingAccount.getID().toString());
            preparedStatement.setString(2, savingAccount.getUserID().toString());
            preparedStatement.setDouble(3, savingAccount.getBalance());
            preparedStatement.setDouble(4, savingAccount.getInterestRate());
            preparedStatement.executeUpdate();
        }
    }

    public SavingAccount findByUserID(Connection connection, UUID userID) throws SQLException {
        String query = "SELECT id, userID, balance, interestRate FROM SavingAccounts WHERE UserID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new SavingAccount(
                        UUID.fromString(resultSet.getString("id")),
                        UUID.fromString(resultSet.getString("userID")),
                        resultSet.getDouble("balance"),
                        resultSet.getDouble("interestRate")
                );
            }
        }
    }

    public boolean updateSavingAccountBalance(Connection connection, SavingAccount savingAccount, Double balance) throws SQLException {
        if (balance == null || balance < 0) {
            return false;
        }

        String query = "UPDATE SavingAccounts SET balance = ? WHERE userID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDouble(1, balance);
            preparedStatement.setString(2, savingAccount.getUserID().toString());
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public boolean updateSavingAccountInterestRate(Connection connection, SavingAccount savingAccount, Double interestRate) throws SQLException {
        if (interestRate == null)  {
            return false;
        }

        String query = "UPDATE SavingAccounts SET interestRate = ? WHERE userID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDouble(1, interestRate);
            preparedStatement.setString(2, savingAccount.getUserID().toString());
            return preparedStatement.executeUpdate() == 1;
        }
    }
}
