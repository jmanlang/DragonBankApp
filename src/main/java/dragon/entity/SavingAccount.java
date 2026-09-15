package dragon.entity;

import java.util.UUID;

public class SavingAccount {
    private UUID id;
    private UUID ownerID;
    private double interestRate;
    private double balance;

    public SavingAccount(UUID id, UUID userID, double balance, double interestRate) {
        this.id = id;
        this.ownerID = userID;
        this.setBalance(balance);
        this.setInterestRate(interestRate);
    }

    public UUID getID() {
        return id;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("balance cannot be null or negative");
        }
        this.balance = balance;
    }

    public UUID getOwnerID() {
        return ownerID;
    }

}
