package dragon.repository;

import dragon.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserRepository {
    public User findByAccountId(Connection connection, String accountId) throws SQLException {
        String sql = "SELECT id, accountId, hashedPassword FROM User WHERE accountId = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return new User(
                        UUID.fromString(resultSet.getString("id")),
                        resultSet.getString("accountId"),
                        resultSet.getString("hashedPassword")
                );
            }
        }
    }

    public void save(Connection connection, User user) throws SQLException {
        String sql = "INSERT INTO User (id, accountId, hashedPassword) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getId().toString());
            statement.setString(2, user.getAccountId());
            statement.setString(3, user.getHashedPassword());
            statement.executeUpdate();
        }
    }
}
