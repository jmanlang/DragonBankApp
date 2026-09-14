package dragon.exception;

public class AuthenticationRequiredException extends BankingException {
    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
