package dragon.entity;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {
    private final UUID id;
    private final UUID ownerId;
    private BigDecimal balance;

    public Account(UUID id, UUID ownerId, BigDecimal balance) {
        if (id == null) {
            throw new IllegalArgumentException("Account ID cannot be null.");
        }
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Account balance cannot be negative or null.");
        }
        this.id = id;
        this.ownerId = ownerId;
        this.balance = balance;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Account balance cannot be negative or null.");
        }
        this.balance = balance;
    }
}
