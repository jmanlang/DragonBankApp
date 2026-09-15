package dragon.entity;

import java.time.Instant;
import java.util.UUID;

public class TransferTransaction {
    private UUID id;
    private UUID userId;
    private double amount;
    private Instant date;
    private String fromAccount;
    private String toAccount;

    public TransferTransaction(UUID id, UUID userId, double amount, Instant date, String fromAccount, String toAccount) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }

    public TransferTransaction(UUID userId, double amount, String fromAccount, String toAccount) {
        this(UUID.randomUUID(), userId, amount, Instant.now(), fromAccount, toAccount);
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

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }
}
