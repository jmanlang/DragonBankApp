package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.database.Database;
import dragon.repository.CheckingAccountRepository;
import dragon.repository.SavingAccountRepository;
import dragon.repository.UserRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {
    private static final String ACCOUNT_ID = "account-1";
    private static final String PIN = "1234";
    private static final String PIN_HASH = "81dc9bdb52d04dc20036dbd8313ed055";

    private Connection connection;
    private String databaseUrl;
    private String previousDatabaseUrl;
    private UUID previousAuthenticatedUserId;
    private AuthService authService;

    @BeforeEach
    void setUp() throws SQLException {
        previousDatabaseUrl = System.getProperty(Database.DATABASE_URL_PROPERTY);
        previousAuthenticatedUserId = AuthenticatedAccountContext.getAuthenticatedUserId();
        databaseUrl = "jdbc:sqlite:file:auth-service-test-" + UUID.randomUUID()
                + "?mode=memory&cache=shared";
        System.setProperty(Database.DATABASE_URL_PROPERTY, databaseUrl);
        connection = DriverManager.getConnection(databaseUrl);
        Database.initialize();
        AuthenticatedAccountContext.setAuthenticatedUserId(null);

        authService = new AuthService(
                new UserRepository(),
                new CheckingAccountRepository(),
                new SavingAccountRepository()
        );
    }

    @AfterEach
    void tearDown() throws SQLException {
        AuthenticatedAccountContext.setAuthenticatedUserId(previousAuthenticatedUserId);
        if (previousDatabaseUrl == null) {
            System.clearProperty(Database.DATABASE_URL_PROPERTY);
        } else {
            System.setProperty(Database.DATABASE_URL_PROPERTY, previousDatabaseUrl);
        }

        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void registerStoresHashedPinAndCreatesBothAccounts() throws SQLException {
        assertTrue(authService.register(ACCOUNT_ID, PIN));

        UUID userId = assertStoredUser(ACCOUNT_ID);
        assertAccountsBelongTo(userId);
        assertRowCounts(1);
    }

    @Test
    void registerTrimsAccountId() throws SQLException {
        assertTrue(authService.register("  " + ACCOUNT_ID + "  ", PIN));

        UUID userId = assertStoredUser(ACCOUNT_ID);
        assertAccountsBelongTo(userId);
        assertRowCounts(1);
    }

    @Test
    void registerRejectsDuplicateAccountIdWithoutChangingOriginalUser() throws SQLException {
        assertTrue(authService.register(ACCOUNT_ID, PIN));
        UUID originalUserId = assertStoredUser(ACCOUNT_ID);

        assertFalse(authService.register("  " + ACCOUNT_ID + "  ", "5678"));

        assertEquals(originalUserId, assertStoredUser(ACCOUNT_ID));
        assertAccountsBelongTo(originalUserId);
        assertRowCounts(1);
    }

    @Test
    void registerRejectsMissingAccountId() throws SQLException {
        assertFalse(authService.register(null, PIN));
        assertFalse(authService.register("", PIN));
        assertFalse(authService.register("   ", PIN));
        assertRowCounts(0);
    }

    @Test
    void registerRejectsMissingPassword() throws SQLException {
        assertFalse(authService.register(ACCOUNT_ID, null));
        assertFalse(authService.register(ACCOUNT_ID, ""));
        assertFalse(authService.register(ACCOUNT_ID, "   "));
        assertRowCounts(0);
    }

    @Test
    void registerRollsBackWhenCheckingAccountInsertFails() throws SQLException {
        rejectAccountInsert("CheckingAccount");

        assertFalse(authService.register(ACCOUNT_ID, PIN));

        assertRowCounts(0);
    }

    @Test
    void registerRollsBackWhenSavingAccountInsertFails() throws SQLException {
        rejectAccountInsert("SavingAccount");

        assertFalse(authService.register(ACCOUNT_ID, PIN));

        assertRowCounts(0);
    }

    @Test
    void registerReturnsFalseWhenDatabaseIsUnavailable() throws SQLException {
        // An unsupported JDBC URL forces a connection failure.
        System.setProperty(Database.DATABASE_URL_PROPERTY, "jdbc:unavailable:auth-test");
        try {
            assertFalse(authService.register(ACCOUNT_ID, PIN));
        } finally {
            System.setProperty(Database.DATABASE_URL_PROPERTY, databaseUrl);
        }

        assertRowCounts(0);
    }

    @Test
    void loginAuthenticatesUserWithCorrectPin() throws SQLException {
        UUID userId = insertLoginUser();
        AuthenticatedAccountContext.setAuthenticatedUserId(UUID.randomUUID());

        assertTrue(authService.login(ACCOUNT_ID, PIN));

        assertEquals(userId, AuthenticatedAccountContext.getAuthenticatedUserId());
    }

    @Test
    void loginTrimsAccountId() throws SQLException {
        UUID userId = insertLoginUser();

        assertTrue(authService.login("  " + ACCOUNT_ID + "  ", PIN));

        assertEquals(userId, AuthenticatedAccountContext.getAuthenticatedUserId());
    }

    @Test
    void loginRejectsIncorrectPinAndClearsAuthenticatedUser() throws SQLException {
        UUID userId = insertLoginUser();
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);

        assertFalse(authService.login(ACCOUNT_ID, "5678"));

        assertNull(AuthenticatedAccountContext.getAuthenticatedUserId());
    }

    @Test
    void loginRejectsUnknownAccountAndClearsAuthenticatedUser() throws SQLException {
        UUID userId = insertLoginUser();
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);

        assertFalse(authService.login("missing-account", PIN));

        assertNull(AuthenticatedAccountContext.getAuthenticatedUserId());
    }

    @Test
    void loginRejectsMissingAccountIdAndClearsAuthenticatedUser() throws SQLException {
        UUID userId = insertLoginUser();

        for (String accountId : new String[]{null, "", "   "}) {
            AuthenticatedAccountContext.setAuthenticatedUserId(userId);
            assertFalse(authService.login(accountId, PIN));
            assertNull(AuthenticatedAccountContext.getAuthenticatedUserId());
        }
    }

    @Test
    void loginRejectsMissingPasswordAndClearsAuthenticatedUser() throws SQLException {
        UUID userId = insertLoginUser();

        for (String password : new String[]{null, "", "   "}) {
            AuthenticatedAccountContext.setAuthenticatedUserId(userId);
            assertFalse(authService.login(ACCOUNT_ID, password));
            assertNull(AuthenticatedAccountContext.getAuthenticatedUserId());
        }
    }

    @Test
    void loginReturnsFalseAndClearsAuthenticatedUserWhenDatabaseIsUnavailable() throws SQLException {
        UUID userId = insertLoginUser();
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);
        System.setProperty(Database.DATABASE_URL_PROPERTY, "jdbc:unavailable:auth-test");

        assertFalse(authService.login(ACCOUNT_ID, PIN));

        assertNull(AuthenticatedAccountContext.getAuthenticatedUserId());
    }

    private UUID insertLoginUser() throws SQLException {
        UUID userId = UUID.randomUUID();
        String sql = "INSERT INTO User (id, accountId, password) VALUES (?, ?, ?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId.toString());
            statement.setString(2, ACCOUNT_ID);
            statement.setString(3, PIN_HASH);
            statement.executeUpdate();
        }

        return userId;
    }

    private UUID assertStoredUser(String accountId) throws SQLException {
        String sql = "SELECT id, accountId, password FROM User WHERE accountId = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                UUID userId = UUID.fromString(resultSet.getString("id"));
                assertEquals(accountId, resultSet.getString("accountId"));
                assertEquals(PIN_HASH, resultSet.getString("password"));
                assertFalse(resultSet.next());
                return userId;
            }
        }
    }

    private void assertAccountsBelongTo(UUID userId) throws SQLException {
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT id, owner, balance FROM CheckingAccount")) {
                assertTrue(resultSet.next());
                assertNotNull(resultSet.getString("id"));
                assertEquals(userId.toString(), resultSet.getString("owner"));
                assertEquals(0.0, resultSet.getDouble("balance"));
                assertFalse(resultSet.next());
            }

            try (ResultSet resultSet = statement.executeQuery("SELECT id, owner, balance, interestRate FROM SavingAccount")) {
                assertTrue(resultSet.next());
                assertNotNull(resultSet.getString("id"));
                assertEquals(userId.toString(), resultSet.getString("owner"));
                assertEquals(0.0, resultSet.getDouble("balance"));
                assertEquals(0.0, resultSet.getDouble("interestRate"));
                assertFalse(resultSet.next());
            }
        }
    }

    private void assertRowCounts(int expected) throws SQLException {
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            for (String table : new String[]{"User", "CheckingAccount", "SavingAccount"}) {
                try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    assertTrue(resultSet.next());
                    assertEquals(expected, resultSet.getInt(1), "Unexpected row count in " + table);
                }
            }
        }
    }


    private void rejectAccountInsert(String table) throws SQLException {
        String sql = "CREATE TRIGGER reject_account_insert BEFORE INSERT ON " + table
                + " BEGIN SELECT RAISE(ABORT, 'Account insert failed for test'); END";

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
