package dragon.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class DepositTransaction {
    private final UUID id;
    private final UUID userId;
    private final BigDecimal amount;
    private final Instant date;

    public DepositTransaction(UUID id, UUID userId, BigDecimal amount, Instant date) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
    }

    public DepositTransaction(UUID userId, BigDecimal amount) {
        this(UUID.randomUUID(), userId, amount, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getDate() {
        return date;
    }
}
