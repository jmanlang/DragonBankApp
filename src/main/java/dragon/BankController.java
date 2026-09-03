package dragon;

public class BankController {
    private boolean isUserAuthenticated;

    public BankController() {
        this.isUserAuthenticated = false;
    }

    public void init() {
        if (!isUserAuthenticated) {
            printAuthMenu();
        }
        if (isUserAuthenticated) {
            printServicesMenu();
        }
    }

    private void printAuthMenu() {
        System.out.println("Login or Register:");
        System.out.println("1. Login");
        System.out.println("2. Register");
    }

    private void printServicesMenu() {
        System.out.println("Select an option:");
        System.out.println("1. Make a bank transaction");
        System.out.println("2. Display balance");
        System.out.println("3. View transaction history");
    }

    private void printTransactionServicesMenu() {
        System.out.println("What kind of transaction would you like to do?");
        System.out.println("1. Make a deposit");
        System.out.println("2. Make a withdrawal");
        System.out.println("3. Transfer money between accounts");
    }
}
