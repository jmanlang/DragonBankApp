package dragon.service;
import java.util.UUID;

public class TransactionService {
    private final UUID userId;

    public TransactionService(UUID autheticatedUserId) {
        this.userId = autheticatedUserId;
    }



}
