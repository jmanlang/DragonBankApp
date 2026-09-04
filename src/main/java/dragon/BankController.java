package dragon;


import java.util.Scanner;

public class BankController {
    private boolean isUserAuthenticated;
    private final Scanner sc = new Scanner(System.in);

    public BankController() {
        this.isUserAuthenticated = false;
    }

    public void init() {
        if (!isUserAuthenticated) {
            printAuthMenu();
        }
        if (isUserAuthenticated) {
            handleServicesMenu();
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
        System.out.println("4. Exit");
    }

    private void handleServicesMenu() {
        boolean exit = false;

        while (!exit) {
            printServicesMenu();
            String input = sc.nextLine().trim();
            switch (input) {
                case "1":
                    // bank transaction
                    break;
                case "2":
                    boolean userWantsExit = handleBalanceManagement();
                    if (userWantsExit) {
                        exit = true;
                    }
                    break;
                case "3":
                    // transaction history
                    break;
                case "4":
                    System.out.println("Exiting...");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid input. Please choose again.");
                    break;
            }
        }
    }

    private void printBalanceManagementMenu() {
        // included both checking and savings, but can rewrite for a singular balance
        System.out.println("Select an option:");
        System.out.println("1. Check the balance in your savings account.");
        System.out.println("2. Check the balance in your checking account.");
        System.out.println("3. Return to main menu.");
        System.out.println("4. Exit.");
    }

    private boolean handleBalanceManagement() {
        boolean returnToMainMenu = false;
        boolean quickExit = false;

        while (!returnToMainMenu) {
            printBalanceManagementMenu();
            String input = sc.nextLine().trim();
            switch (input) {
                case "1":
                    System.out.println("Savings account: $500");
                    break;
                case "2":
                    System.out.println("Checking account: $1000");
                    break;
                case "3":
                    System.out.println("Returning to main menu...");
                    returnToMainMenu = true;
                    break;
                case "4":
                    System.out.println("Exiting...");
                    quickExit = true;
                    returnToMainMenu = true;
                    break;
                default:
                    System.out.println("Invalid input. please try again.");
                    break;
            }
        }
        return quickExit;
    }

    private void printTransactionServicesMenu() {
        System.out.println("What kind of transaction would you like to do?");
        System.out.println("1. Make a deposit");
        System.out.println("2. Make a withdrawal");
        System.out.println("3. Transfer money between accounts");
    }
}
