package dragon.Exceptions;

public class NoAccountException extends RuntimeException {
    public NoAccountException() {
        super("NoAccountException: No user logged in");
    }
}
