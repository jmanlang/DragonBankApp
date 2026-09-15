package dragon.repository;

import dragon.entity.SavingAccount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavingAccountRepositoryTest {

    private Connection connection;
    private SavingAccountRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE SavingAccounts (" +
                            "id TEXT PRIMARY KEY, " +
                            "userID TEXT NOT NULL, " +
                            "balance REAL NOT NULL DEFAULT 0, " +
                            "interestRate REAL NOT NULL DEFAULT 0)"
            );
        }
        repository = new SavingAccountRepository();
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void findByUserID_returnsNull_whenNoAccountExists() throws SQLException {
        UUID randomUserId = UUID.randomUUID();

        SavingAccount result = repository.findByUserID(connection, randomUserId);

        assertNull(result, "Expected no account to be found for a user with no saving account");
    }

    @Test
    void createThenFind_roundTripsCorrectly_includingInterestRate() throws SQLException {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SavingAccount newAccount = new SavingAccount(accountId, userId, 500.0, 0.025);

        repository.createSavingAccount(connection, newAccount);
        SavingAccount found = repository.findByUserID(connection, userId);

        assertNotNull(found, "Expected to find the account that was just created");
        assertEquals(accountId, found.getID());
        assertEquals(userId, found.getUserID());
        assertEquals(500.0, found.getBalance(), 0.0001);
        assertEquals(0.025, found.getInterestRate(), 0.0001);
    }

    @Test
    void createSavingAccount_rejectsDuplicateAccountId() throws SQLException {
        UUID accountId = UUID.randomUUID();
        SavingAccount first = new SavingAccount(accountId, UUID.randomUUID(), 100.0, 0.01);
        SavingAccount duplicateId = new SavingAccount(accountId, UUID.randomUUID(), 200.0, 0.02);

        repository.createSavingAccount(connection, first);

        assertThrows(SQLException.class,
                () -> repository.createSavingAccount(connection, duplicateId),
                "Inserting a second account with the same primary key id should fail");
    }

    @Test
    void updateSavingAccountBalance_positive_actuallyChangesTheStoredValue() throws SQLException {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SavingAccount account = new SavingAccount(accountId, userId, 100.0, 0.01);
        repository.createSavingAccount(connection, account);

        boolean updated = repository.updateSavingAccountBalance(connection, account, 750.0);
        SavingAccount found = repository.findByUserID(connection, userId);

        assertTrue(updated);
        assertEquals(750.0, found.getBalance(), 0.0001,
                "Balance should reflect the new value passed to updateSavingAccountBalance, not the old one");
    }

    @Test
    void updateSavingAccountBalance_negative_rejectsNegativeAmount() throws SQLException {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SavingAccount account = new SavingAccount(accountId, userId, 100.0, 0.01);
        repository.createSavingAccount(connection, account);

        boolean updated = repository.updateSavingAccountBalance(connection, account, -25.0);
        SavingAccount found = repository.findByUserID(connection, userId);

        assertFalse(updated, "A negative balance should be rejected, not written");
        assertEquals(100.0, found.getBalance(), 0.0001,
                "Balance should be unchanged after a rejected update");
    }

    @Test
    void updateSavingAccountInterestRate_positive_actuallyChangesTheStoredValue() throws SQLException {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SavingAccount account = new SavingAccount(accountId, userId, 100.0, 0.01);
        repository.createSavingAccount(connection, account);

        boolean updated = repository.updateSavingAccountInterestRate(connection, account, 0.05);
        SavingAccount found = repository.findByUserID(connection, userId);

        assertTrue(updated);
        assertEquals(0.05, found.getInterestRate(), 0.0001);
    }

    @Test
    void updateSavingAccountInterestRate_negative_rejectsNullRate() throws SQLException {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SavingAccount account = new SavingAccount(accountId, userId, 100.0, 0.01);
        repository.createSavingAccount(connection, account);

        boolean updated = repository.updateSavingAccountInterestRate(connection, account, null);
        SavingAccount found = repository.findByUserID(connection, userId);

        assertFalse(updated, "A null interest rate should be rejected, not written");
        assertEquals(0.01, found.getInterestRate(), 0.0001,
                "Interest rate should be unchanged after a rejected update");
    }
}