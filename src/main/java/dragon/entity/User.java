package dragon.entity;

import java.util.UUID;

// Represents a row in the User SQL table.
public class User {
    private UUID id;
    private String accountId;
    private String password;

    public User(UUID id, String accountId, String password) {
        this.id = id;
        this.accountId = accountId;
        this.password = password;
    }

    public User(String accountId, String password) {
        this(UUID.randomUUID(), accountId, password);
    }

    public UUID getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getPassword() {
        return password;
    }
}
