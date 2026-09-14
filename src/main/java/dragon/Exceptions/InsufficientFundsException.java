package dragon.Exceptions;

public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(float availableBalance, float withdrawAmount) {
        super("InsufficientFundsException: User tried to withdraw $ " + withdrawAmount + " from account with balance $ " + availableBalance);
    }
}
