package dragon.Exceptions;

public class InvalidAmountException extends Exception {
    public InvalidAmountException() {
        super("InvalidAmountException: Amount not valid");
    }
}
