package dragon.exception;


public class ServiceUnavailableException extends BankingException {
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
