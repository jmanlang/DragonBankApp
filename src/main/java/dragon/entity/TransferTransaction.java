package dragon.entity;
import java.time.Instant;
import java.util.UUID;


public class TransferTransaction extends Transaction {
    private UUID accountID2;

    public TransferTransaction(UUID id, UUID userId, UUID fromAccountId, UUID toAccountId, double amount, Instant date) {
        super(id, userId, fromAccountId, amount, date);
        this.accountID2 = toAccountId;
    }

    public UUID getAccountID2() {
        return accountID2;
    }

    public void setAccountID2(UUID accountID2) {
        this.accountID2 = accountID2;
    }

    @Override
    public String toString() {
        return String.format("Transaction Type: Transfer, Transaction id: %s: , from id: %s, to id: %s, date: %s, account: $%.2f.",
                this.getId(), this.getAccountID(), this.getAccountID2(), this.getDate(), this.getAmount());
    }
}
