package dragon.entity;
import java.time.Instant;
import java.util.UUID;

public class Transaction {
    private UUID id;
    private UUID userId;
    private UUID accountID;
    private double amount;
    private Instant date;

    public Transaction(UUID id, UUID userId, UUID accountId, double amount, Instant date) {
        this.id = id;
        this.amount = amount;
        this.accountID = accountId;
        this.userId = userId;
        this.date = date;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }

    public Instant getDate() {
        return date;
    }

    public UUID getAccountID() {
        return accountID;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setAccountID(UUID accountID) {
        this.accountID = accountID;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format("Transaction id: %s: , userId: %s, accountId: %s,  date: %s, amount: $%.2f.",
                this.getId(), this.getUserId(), this.getAccountID(), this.getDate(), this.getAmount());
    }


}
