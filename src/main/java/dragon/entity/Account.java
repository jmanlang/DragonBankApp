package dragon.entity;

import java.util.UUID;

public class Account {
    private UUID id;
    private String bankAccountId;
    private UUID ownerId; // References User.id
    private float balance;

    public Account(String accountId, UUID ownerId, float balance) {
        this.id = UUID.randomUUID();
        this.bankAccountId = accountId;
        this.ownerId = ownerId;
        this.balance = balance;
    }

    public Account(String accountId, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.bankAccountId = accountId;
        this.ownerId = ownerId;
        this.balance = 0;
    }

    public UUID getId() {
        return id;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        if (balance >= 0) { // Prevent account from going below $0
            this.balance = balance;
        }
    }
}
