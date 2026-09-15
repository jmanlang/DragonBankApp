package dragon.entity;

import java.time.Instant;
import java.util.UUID;

public class WithdrawalTransaction extends Transaction {
    public WithdrawalTransaction(UUID id, UUID userId, UUID accountId, double amount, Instant date) {
        super(id, userId, accountId, amount, date);
    }


    public String toString() {
        return "Transaction Type: Withdrawal, " + super.toString();
    }
}
