package dragon.entity;

import java.time.Instant;
import java.util.UUID;

public class DepositTransaction extends Transaction{
    public DepositTransaction(UUID id, UUID userId, UUID accountId, double amount, Instant date) {
        super(id, userId, accountId, amount, date);
    }


    public String toString() {
        return "Transaction Type: Deposit, " + super.toString();
    }
}
