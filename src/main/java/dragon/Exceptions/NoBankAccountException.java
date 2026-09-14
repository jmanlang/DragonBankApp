package dragon.Exceptions;

public class NoBankAccountException extends Exception {
    public NoBankAccountException(String accountId) {
        super("Owner does not own account with id " + accountId);
    }
}
