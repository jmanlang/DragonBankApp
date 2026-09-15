package dragon.entity;

import java.util.UUID;

public class SavingAccount {
    private UUID id;
    private Double interestRate;
    private Double balance;
    private UUID userID;

    public SavingAccount(UUID id, UUID userID, Double balance, Double interestRate) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.id = id;
        this.userID = userID;
        this.setBalance(balance);
        this.setInterestRate(interestRate);
    }

    public UUID getID() {
        return id;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        if (interestRate == null || interestRate < 0) {
            throw new IllegalArgumentException("balance cannot be null or negative");
        }
        this.interestRate = interestRate;
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
