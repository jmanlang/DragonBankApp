package dragon.repository;

import dragon.database.Database;
import dragon.entity.WithdrawalTransaction;
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

class WithdrawalTransactionRepositoryTest {

    private Connection connection;
    private WithdrawalTransactionRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    Database.CREATE_WITHDRAWAL_TRANSACTION
            );
        }
        repository = new WithdrawalTransactionRepository();
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void saveWithdrawalPositive() throws SQLException {
        // Valid withdrawal transaction
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        WithdrawalTransaction transaction = new WithdrawalTransaction(userId, 90.25, accountId);

        repository.save(connection, transaction);

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT COUNT(*) FROM WithdrawalTransaction WHERE userId = ? AND amount = ? AND accountId = ?")) {
            preparedStatement.setString(1, userId.toString());
            preparedStatement.setDouble(2, 90.25);
            preparedStatement.setString(3, accountId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                assertEquals(1, resultSet.getInt(1), "Expected one saved withdrawal record");
            }
        }
    }

    @Test
    void saveWithdrawalNegative() throws SQLException {
        // Withdrawal transaction w/ duplicate id
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        WithdrawalTransaction first = new WithdrawalTransaction(transactionId, userId, 60.00, java.time.Instant.now(), accountId);
        WithdrawalTransaction duplicate = new WithdrawalTransaction(transactionId, userId, 80.00, java.time.Instant.now(), accountId);

        repository.save(connection, first);

        assertThrows(SQLException.class,
                () -> repository.save(connection, duplicate),
                "Saving a second withdrawal with the same primary key should fail");
    }
}
