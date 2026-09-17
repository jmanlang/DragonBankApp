package dragon.entity;

import java.time.Instant;
import java.util.UUID;

public class DepositTransaction implements HasDate {
    private UUID id;
    private UUID userId;
    private UUID accountId;
    private double amount;
    private Instant date;

    public DepositTransaction(UUID id, UUID userId, double amount, Instant date) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
    }

    public DepositTransaction(UUID id, UUID userId, double amount, Instant date, UUID accountId) {
        this.id = id;
        this.userId = userId;
        this.accountId = accountId;
        this.amount = amount;
        this.date = date;
    }



    public DepositTransaction(UUID userId, double amount, UUID accountId) {
        this(UUID.randomUUID(), userId, amount, Instant.now(), accountId);
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public String toString() {
        return String.format("Deposit- user Id: %s, account Id: %s, amount: %.2f, date:%s",
                this.userId.toString().substring(32),
                this.accountId.toString().substring(32),
                this.amount,
                this.date.toString().substring(0,16));
    }
}