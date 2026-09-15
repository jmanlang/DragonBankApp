package dragon.repository;

import dragon.entity.SavingAccount;

import java.util.UUID;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SavingAccountRepository {

    public void createSavingAccount(Connection connection, SavingAccount savingAccount) throws SQLException {
        String query = "INSERT INTO SavingAccount (id, owner, balance, interestRate) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, savingAccount.getID().toString());
            preparedStatement.setString(2, savingAccount.getOwnerID().toString());
            preparedStatement.setDouble(3, savingAccount.getBalance());
            preparedStatement.setDouble(4, savingAccount.getInterestRate());
            preparedStatement.executeUpdate();
        }
    }

    public SavingAccount findByOwnerID(Connection connection, UUID ownerID) throws SQLException {
        String query = "SELECT id, owner, balance, interestRate FROM SavingAccount WHERE owner = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, ownerID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new SavingAccount(
                        UUID.fromString(resultSet.getString("id")),
                        UUID.fromString(resultSet.getString("owner")),
                        resultSet.getDouble("balance"),
                        resultSet.getDouble("interestRate")
                );
            }
        }
    }

    public boolean updateSavingAccountBalance(Connection connection, SavingAccount savingAccount, double balance) throws SQLException {
        if (balance < 0) {
            return false;
        }

        String query = "UPDATE SavingAccount SET balance = ? WHERE owner = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDouble(1, balance);
            preparedStatement.setString(2, savingAccount.getOwnerID().toString());
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public boolean updateSavingAccountInterestRate(Connection connection, SavingAccount savingAccount, double interestRate) throws SQLException {
        if (interestRate < 0) {
            return false;
        }
        String query = "UPDATE SavingAccount SET interestRate = ? WHERE owner = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDouble(1, interestRate);
            preparedStatement.setString(2, savingAccount.getOwnerID().toString());
            return preparedStatement.executeUpdate() == 1;
        }
    }
}
