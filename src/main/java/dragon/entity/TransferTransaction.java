package dragon.entity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("MMM dd yyyy hh:mma")
                .withZone(ZoneId.systemDefault());
        int userLength = this.userId.toString().length();
        String shortenedUserId = this.userId.toString().substring(userLength - 4);

        int fromAccountLength = this.fromAccount.toString().length();
        String shortenedfromAccount = this.fromAccount.toString().substring(fromAccountLength - 4);

        int toAccountLength = this.toAccount.toString().length();
        String shortenedtoAccount = this.toAccount.toString().substring(toAccountLength - 4);


        return String.format("Transfer:   %s - Amount: $%,-15.2f - User ID: ****%s - from Account ID: ****%s - to Account ID: ****%s", formatter.format(this.date), this.amount, shortenedUserId, shortenedfromAccount, shortenedtoAccount);
    }
}