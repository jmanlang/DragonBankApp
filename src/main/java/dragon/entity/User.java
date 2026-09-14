package dragon.entity;

import java.util.UUID;

public class User {
    private final UUID id;
    private final String accountId;
    private final String hashedPassword;

    public User(UUID id, String accountId, String hashedPassword) {
        this.id = id;
        this.accountId = accountId;
        this.hashedPassword = hashedPassword;
    }

    public User(String accountId, String hashedPassword) {
        this(UUID.randomUUID(), accountId, hashedPassword);
    }

    public UUID getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }
}
