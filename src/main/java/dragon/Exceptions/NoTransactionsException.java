package dragon.Exceptions;

public class NoTransactionsException extends Exception {
    public NoTransactionsException(String userId) {
        super("There is no transaction history for userId: " + userId);
    }
}
