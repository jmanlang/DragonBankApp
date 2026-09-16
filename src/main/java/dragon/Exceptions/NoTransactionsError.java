package dragon.Exceptions;

public class NoTransactionsError extends Exception {
    public NoTransactionsError(String userId) {
        super("There is no transaction history for userId: " + userId);
    }
}
