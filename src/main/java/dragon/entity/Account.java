package dragon.entity;

import java.util.UUID;

public class Account {
    private UUID id;
    private String bankAccountId;
    private String ownerId; // References User.accountId
    private float balance;

    public Account(String accountId, String ownerId, float balance) {
        this.id = UUID.randomUUID();
        this.bankAccountId = accountId;
        this.ownerId = ownerId;
        this.balance = balance;
    }

    public Account(String accountId, String ownerId) {
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

    public String getOwnerId() {
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
