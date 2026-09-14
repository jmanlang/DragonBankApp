package dragon.exception;

public class AccountAlreadyExistsException extends BankingException {
    public AccountAlreadyExistsException(String message) {
        super(message);
    }
}
