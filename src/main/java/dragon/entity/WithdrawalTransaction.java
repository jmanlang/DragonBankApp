package dragon.entity;

import java.time.Instant;
import java.util.UUID;

public class WithdrawalTransaction implements HasDate{
    private UUID id;
    private UUID userId;
    private double amount;
    private Instant date;

    public WithdrawalTransaction(UUID id, UUID userId, double amount, Instant date) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
    }

    public WithdrawalTransaction(UUID userId, double amount) {
        this(UUID.randomUUID(), userId, amount, Instant.now());
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

    @Override
    public String toString() {
        return String.format("Withdrawal- user ID: %s, amount: %.2f, date:%s",
                this.userId.toString().substring(32),
                this.amount,
                this.date.toString().substring(0,16));
    }
}