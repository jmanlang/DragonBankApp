package dragon.entity;

import java.util.UUID;

public class SavingAccount {
    private UUID id;
    private UUID owner;
    private double interestRate;
    private double balance;

    public SavingAccount(UUID id, UUID owner, double balance, double interestRate) {
        this.setID(id);
        this.setOwnerID(owner);
        this.setBalance(balance);
        this.setInterestRate(interestRate);
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

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        if (interestRate < 0) {
            throw new IllegalArgumentException("interestRate cannot be negative");
        }
        this.interestRate = interestRate;
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

    public void setOwnerID(UUID owner) {
        if (owner == null) {
            throw new IllegalArgumentException("ownerID cannot be null");
        }
        this.owner = owner;
    }

}
