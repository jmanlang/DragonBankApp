package dragon.entity;

import java.time.Instant;
import java.util.UUID;

public class TransferTransaction implements HasDate {

    private UUID id;
    private UUID userId;
    private UUID fromAccount;
    private UUID toAccount;
    private double amount;
    private Instant date;

    public TransferTransaction(UUID id, UUID userId, UUID fromAccount, UUID toAccount, double amount, Instant date) {
        this.id = id;
        this.userId = userId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.date = date;
    }

    public TransferTransaction(UUID userId, UUID fromAccount, UUID toAccount, double amount) {
        this(UUID.randomUUID(), userId, fromAccount, toAccount, amount, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getFromAccount() {
        return fromAccount;
    }

    public UUID getToAccount() {
        return toAccount;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public Instant getDate() {
        return date;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setFromAccount(UUID fromAccount) {
        this.fromAccount = fromAccount;
    }

    public void setToAccount(UUID toAccount) {
        this.toAccount = toAccount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public String toString() {
        return String.format("Transfer- user ID: %s, from Account ID:%s, to Account ID: %s, amount: %.2f, date:%s",
                this.userId.toString().substring(32),
                this.fromAccount.toString().substring(32),
                this.toAccount.toString().substring(32),
                this.amount,
                this.date.toString().substring(0,16));
    }
}