package dragon.entity;

import java.util.UUID;

public class CheckingAccount {
    private UUID id;
    private UUID ownerID;
    private double balance;

    public CheckingAccount(UUID id,  UUID ownerID, double balance) {
        this.id = id;
        this.ownerID = ownerID;
        this.setBalance(balance);
    }

    public UUID getID() {
        return id;
    }

    public void setID(UUID id) {
        this.id = id;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("balance cannot be null or negative");
        }
        this.balance = balance;
    }

    public UUID getUserID() {
        return ownerID;
    }

    public void setUserID(UUID ownerID) {
        this.ownerID = ownerID;
    }

}
