package dragon.repository;

import dragon.database.Database;
import dragon.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest {
    private Connection connection;
    private String previousDatabaseUrl;
    private UserRepository userRepository;
    private User existingUser;

    @BeforeEach
    void setUp() throws SQLException {
        previousDatabaseUrl = System.getProperty(Database.DATABASE_URL_PROPERTY);
        String databaseUrl = "jdbc:sqlite:file:user-repository-test-" + UUID.randomUUID()
                + "?mode=memory&cache=shared";
        System.setProperty(Database.DATABASE_URL_PROPERTY, databaseUrl);
        connection = DriverManager.getConnection(databaseUrl);
        Database.initialize();

        userRepository = new UserRepository();
        existingUser = new User(UUID.randomUUID(), "account-1", "stored-pin-hash");
        insertTestUser(existingUser);
    }

    @AfterEach
    void tearDown() throws SQLException {
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
    void findByAccountIdReturnsMatchingUser() throws SQLException {
        User otherUser = new User(UUID.randomUUID(), "account-2", "another-pin-hash");
        insertTestUser(otherUser);

        User result = userRepository.findByAccountId(otherUser.getAccountId());

        assertUserMatches(otherUser, result);
    }

    @Test
    void findByAccountIdReturnsNullWhenUserDoesNotExist() {
        assertNull(userRepository.findByAccountId("missing-account"));
    }

    @Test
    void findByAccountIdWithConnectionReturnsMatchingUser() throws SQLException {
        User otherUser = new User(UUID.randomUUID(), "account-2", "another-pin-hash");
        insertTestUser(otherUser);

        try (Connection connection = Database.getConnection()) {
            User result = userRepository.findByAccountId(connection, otherUser.getAccountId());

            assertUserMatches(otherUser, result);
            assertFalse(connection.isClosed());
        }
    }

    @Test
    void findByAccountIdWithConnectionReturnsNullWhenUserDoesNotExist() throws SQLException {
        try (Connection connection = Database.getConnection()) {
            assertNull(userRepository.findByAccountId(connection, "missing-account"));
        }
    }

    @Test
    void existsByAccountIdReturnsTrueWhenUserExists() {
        assertTrue(userRepository.existsByAccountId(existingUser.getAccountId()));
    }

    @Test
    void existsByAccountIdReturnsFalseWhenUserDoesNotExist() {
        assertFalse(userRepository.existsByAccountId("missing-account"));
    }

    @Test
    void authenticateReturnsTrueWhenPasswordMatches() {
        assertTrue(userRepository.authenticate(existingUser.getAccountId(), existingUser.getPassword()));
    }

    @Test
    void authenticateReturnsFalseWhenPasswordIsIncorrect() {
        assertFalse(userRepository.authenticate(existingUser.getAccountId(), "wrong-pin-hash"));
    }

    @Test
    void authenticateReturnsFalseWhenUserDoesNotExist() {
        assertFalse(userRepository.authenticate("missing-account", existingUser.getPassword()));
    }

    @Test
    void saveStoresUser() throws SQLException {
        User newUser = new User(UUID.randomUUID(), "new-account", "new-pin-hash");

        userRepository.save(newUser);

        assertStoredUserMatches(newUser);
        assertEquals(2, countUsers());
    }

    @Test
    void saveRejectsDuplicateAccountId() throws SQLException {
        User duplicateUser = new User(UUID.randomUUID(), existingUser.getAccountId(), "different-pin-hash");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userRepository.save(duplicateUser)
        );

        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals(1, countUsers());
        assertStoredUserMatches(existingUser);
    }

    @Test
    void saveWithConnectionStoresUser() throws SQLException {
        User newUser = new User(UUID.randomUUID(), "new-account", "new-pin-hash");

        try (Connection connection = Database.getConnection()) {
            userRepository.save(connection, newUser);
            assertFalse(connection.isClosed());
        }

        assertStoredUserMatches(newUser);
        assertEquals(2, countUsers());
    }

    @Test
    void saveWithConnectionRejectsDuplicateAccountId() throws SQLException {
        User duplicateUser = new User(UUID.randomUUID(), existingUser.getAccountId(), "different-pin-hash");

        try (Connection connection = Database.getConnection()) {
            assertThrows(SQLException.class, () -> userRepository.save(connection, duplicateUser));
        }

        assertEquals(1, countUsers());
        assertStoredUserMatches(existingUser);
    }

    private void insertTestUser(User user) throws SQLException {
        String sql = "INSERT INTO User (id, accountId, password) VALUES (?, ?, ?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getId().toString());
            statement.setString(2, user.getAccountId());
            statement.setString(3, user.getPassword());
            statement.executeUpdate();
        }
    }

    private void assertUserMatches(User expected, User actual) {
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAccountId(), actual.getAccountId());
        assertEquals(expected.getPassword(), actual.getPassword());
    }

    private void assertStoredUserMatches(User expected) throws SQLException {
        String sql = "SELECT id, accountId, password FROM User WHERE accountId = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, expected.getAccountId());

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals(expected.getId().toString(), resultSet.getString("id"));
                assertEquals(expected.getAccountId(), resultSet.getString("accountId"));
                assertEquals(expected.getPassword(), resultSet.getString("password"));
                assertFalse(resultSet.next());
            }
        }
    }

    private int countUsers() throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM User");
             ResultSet resultSet = statement.executeQuery()) {
            assertTrue(resultSet.next());
            return resultSet.getInt(1);
        }
    }
}
