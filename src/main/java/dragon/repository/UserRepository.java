package dragon.repository;

import dragon.entity.User;
import dragon.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

// We should the bodies of each of these methods by JDBC calls.
// For findByAccountId for example, we'd actually query the sqlite db to check
// if there's a user with the same accountId.

// Basically, we have a repository for each table/entity in our database.
// These help you do operations related to a table.
public class UserRepository {
    // Does the user by accountId exist? If so, return the User object. Else null.
    public User findByAccountId(String accountId) {
        try (Connection connection = Database.getConnection()) {
            return findByAccountId(connection, accountId);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not find user.", e);
        }
    }

    public User findByAccountId(Connection connection, String accountId) throws SQLException {
        String sql = "SELECT id, accountId, password FROM User WHERE accountId = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return new User(
                        UUID.fromString(resultSet.getString("id")),
                        resultSet.getString("accountId"),
                        resultSet.getString("password")
                );
            }
        }
    }

    // Is there a user with the account id?
    public boolean existsByAccountId(String accountId) {
        return findByAccountId(accountId) != null;
    }

    // Return true if there exists accountId User and the password is correct.
    public boolean authenticate(String accountId, String password) {
        User user = findByAccountId(accountId);
        return user != null && user.getPassword().equals(password);
    }

    // Add a new User to the db.
    public void save(User user) {
        try (Connection connection = Database.getConnection()) {
            save(connection, user);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save user.", e);
        }
    }

    public void save(Connection connection, User user) throws SQLException {
        String sql = "INSERT INTO User (id, accountId, password) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getId().toString());
            statement.setString(2, user.getAccountId());
            statement.setString(3, user.getPassword());
            statement.executeUpdate();
        }
    }
}
