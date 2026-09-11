package dragon.repository;

import dragon.entity.User;

import java.util.UUID;

// We should the bodies of each of these methods by JDBC calls.
// For findByAccountId for example, we'd actually query the sqlite db to check
// if there's a user with the same accountId.

// Basically, we have a repository for each table/entity in our database.
// These help you do operations related to a table.
public class UserRepository {
    // Does the user by accountId exist? If so, return the User object. Else null.
    public User findByAccountId(String accountId) {
        return new User("123", "abc");
    }

    // Is there a user with the account id?
    public boolean existsByAccountId(String accountId) {
        return false;
    }

    // Return true if there exists accountId User and the password is correct.
    public boolean authenticate(String accountId, String password) {
        return true;
    }

    // Add a new User to the db.
    public void save(User user) {};
}
