package dragon.entity;

import java.util.UUID;

public class CheckingAccount implements Account{
    private UUID id;
    private UUID owner;
    private double balance;

    public CheckingAccount(UUID id,  UUID owner, double balance) {
        this.setID(id);
        this.setOwnerID(owner);
        this.setBalance(balance);
    }

    public UUID getID() {
        return id;
    }

    public void setID(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.id = id;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("balance cannot be negative");
        }
        this.balance = balance;
    }

    public UUID getOwnerID() {
        return owner;
    }

    public void setOwnerID(UUID ownerID) {
        if  (ownerID == null) {
            throw new IllegalArgumentException("ownerID cannot be null");
        }
        this.owner = ownerID;
    }

}
