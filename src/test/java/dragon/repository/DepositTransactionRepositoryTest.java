package dragon.repository;

import dragon.database.Database;
import dragon.entity.DepositTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DepositTransactionRepositoryTest {

    private Connection connection;
    private DepositTransactionRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    Database.CREATE_DEPOSIT_TRANSACTION
            );
        }
        repository = new DepositTransactionRepository();
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void saveDepositPositive() throws SQLException {
        // Valid Deposit Transaction
        UUID userId = UUID.randomUUID();
        DepositTransaction transaction = new DepositTransaction(userId, 125.50);

        repository.save(connection, transaction);

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT COUNT(*) FROM DepositTransaction WHERE userId = ? AND amount = ?")) {
            preparedStatement.setString(1, userId.toString());
            preparedStatement.setDouble(2, 125.50);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                assertEquals(1, resultSet.getInt(1), "Expected one saved deposit record");
            }
        }
    }

    @Test
    void saveDepositNegative() throws SQLException {
        // Deposit Transaction w/ duplicate id
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        DepositTransaction first = new DepositTransaction(transactionId, userId, 50.00, java.time.Instant.now());
        DepositTransaction duplicate = new DepositTransaction(transactionId, userId, 75.00, java.time.Instant.now());

        repository.save(connection, first);

        assertThrows(SQLException.class,
                () -> repository.save(connection, duplicate),
                "Saving a second deposit with the same primary key should fail");
    }
}
