package dragon.entity;
import java.util.UUID;


public class BankAccount {
    private UUID id;
    private UUID userId;
    private double balance;

    public BankAccount(UUID id, UUID userId, double balance) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return  String.format("Bank Account ID: %s, userId: %s, Balance: $%.2f", this.getId(), this.getUserId(), this.getBalance());
    }


}
