package dragon.repository;
import dragon.database.Database;

import dragon.entity.CheckingAccount;
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

class CheckingAccountRepositoryTest {

    private Connection connection;
    private CheckingAccountRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute(Database.CREATE_CHECKING_ACCOUNT);
        }
        repository = new CheckingAccountRepository();
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void findByOwnerID_returnsNull_whenNoAccountExists() throws SQLException {
        UUID randomUserId = UUID.randomUUID();

        CheckingAccount result = repository.findByOwnerID(connection, randomUserId);

        assertNull(result, "Expected no account to be found for a user with no checking account");
    }

    @Test
    void createThenFind_roundTripsCorrectly() throws SQLException {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CheckingAccount newAccount = new CheckingAccount(accountId, userId, 250.75);

        repository.createCheckingAccount(connection, newAccount);
        CheckingAccount found = repository.findByOwnerID(connection, userId);

        assertNotNull(found, "Expected to find the account that was just created");
        assertEquals(accountId, found.getID());
        assertEquals(userId, found.getOwnerID());
        assertEquals(250.75, found.getBalance(), 0.0001);
    }

    @Test
    void findByOwnerID_doesNotMatchDifferentUser() throws SQLException {
        UUID accountId = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        repository.createCheckingAccount(connection, new CheckingAccount(accountId, ownerUserId, 100.0));

        CheckingAccount result = repository.findByOwnerID(connection, otherUserId);

        assertNull(result, "An account belonging to a different user should not be returned");
    }

    @Test
    void createCheckingAccount_withZeroBalance_isAllowed() throws SQLException {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CheckingAccount freshAccount = new CheckingAccount(accountId, userId, 0.0);

        repository.createCheckingAccount(connection, freshAccount);
        CheckingAccount found = repository.findByOwnerID(connection, userId);

        assertNotNull(found);
        assertEquals(0.0, found.getBalance(), 0.0001);
    }

    @Test
    void createCheckingAccount_rejectsDuplicateAccountId() throws SQLException {
        UUID accountId = UUID.randomUUID();
        CheckingAccount first = new CheckingAccount(accountId, UUID.randomUUID(), 100.0);
        CheckingAccount duplicateId = new CheckingAccount(accountId, UUID.randomUUID(), 200.0);

        repository.createCheckingAccount(connection, first);

        assertThrows(SQLException.class,
                () -> repository.createCheckingAccount(connection, duplicateId),
                "Inserting a second account with the same primary key id should fail");
    }

    @Test
    void updateCheckingBalance_positive_actuallyChangesTheStoredValue() throws SQLException {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CheckingAccount account = new CheckingAccount(accountId, userId, 100.0);
        repository.createCheckingAccount(connection, account);

        boolean updated = repository.updateCheckingBalance(connection, account, 450.0);
        CheckingAccount found = repository.findByOwnerID(connection, userId);

        assertTrue(updated, "updateCheckingBalance should report success for a valid update");
        assertEquals(450.0, found.getBalance(), 0.0001,
                "Balance should reflect the new value, not the account's stale in-memory balance");
    }

    @Test
    void updateCheckingBalance_negative_rejectsNegativeAmount() throws SQLException {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CheckingAccount account = new CheckingAccount(accountId, userId, 100.0);
        repository.createCheckingAccount(connection, account);

        boolean updated = repository.updateCheckingBalance(connection, account, -50.0);
        CheckingAccount found = repository.findByOwnerID(connection, userId);

        assertFalse(updated, "A negative balance should be rejected, not written");
        assertEquals(100.0, found.getBalance(), 0.0001,
                "Balance should be unchanged after a rejected update");
    }
}