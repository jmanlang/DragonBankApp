package dragon.entity;

import java.util.UUID;

public class CheckingAccount {
    private UUID id;
    private Double balance;
    private UUID userID;

    public CheckingAccount(UUID id,  UUID userID, Double balance) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.id = id;
        this.userID = userID;
        this.setBalance(balance);
    }

    public UUID getID() {
        return id;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        if (balance == null || balance < 0) {
            throw new IllegalArgumentException("balance cannot be null or negative");
        }
        this.balance = balance;
    }

    public UUID getUserID() {
        return userID;
    }

}
