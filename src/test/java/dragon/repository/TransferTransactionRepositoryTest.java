package dragon.repository;

import dragon.database.Database;
import dragon.entity.TransferTransaction;
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

class TransferTransactionRepositoryTest {

    private Connection connection;
    private TransferTransactionRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    Database.CREATE_TRANSFER_TRANSACTION
            );
        }
        repository = new TransferTransactionRepository();
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void saveTransferPositive() throws SQLException {
        // Insert valid transfer transaction
        UUID userId = UUID.randomUUID();
        UUID fromAccount = UUID.randomUUID();
        UUID toAccount = UUID.randomUUID();
        TransferTransaction transaction = new TransferTransaction(userId, fromAccount, toAccount, 150.0);

        repository.save(connection, transaction);

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT COUNT(*) FROM TransferTransaction WHERE userId = ? AND fromAccount = ? AND toAccount = ? AND amount = ?")) {
            preparedStatement.setString(1, userId.toString());
            preparedStatement.setString(2, fromAccount.toString());
            preparedStatement.setString(3, toAccount.toString());
            preparedStatement.setDouble(4, 150.0);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                assertEquals(1, resultSet.getInt(1), "Expected one saved transfer record");
            }
        }
    }

    @Test
    void saveTransferNegative() throws SQLException {
        // Transfer transaction w/ duplicate id
        UUID userId = UUID.randomUUID();
        UUID fromAccount = UUID.randomUUID();
        UUID toAccount = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        TransferTransaction first = new TransferTransaction(transactionId, userId, fromAccount, toAccount, 40.00, java.time.Instant.now());
        TransferTransaction duplicate = new TransferTransaction(transactionId, userId, fromAccount, toAccount, 60.00, java.time.Instant.now());

        repository.save(connection, first);

        assertThrows(SQLException.class,
                () -> repository.save(connection, duplicate),
                "Saving a second transfer with the same primary key should fail");
    }
}
