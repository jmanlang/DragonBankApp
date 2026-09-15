package dragon;

import dragon.service.AuthService;
import dragon.service.TransactionService;

import java.util.Scanner;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BankController {
    private final AuthService authService;
    private final TransactionService transactionService;

    private final Scanner sc = new Scanner(System.in);

    public BankController(AuthService authService, TransactionService transactionService) {
        this.authService = authService;
        this.transactionService = transactionService;
    }

    public void init() {
        if (!isUserAuthenticated()) {
            userAuthenticationHandler();
        }
        if (isUserAuthenticated()) {
            handleServicesMenu();
        }
    }
    private void userAuthenticationHandler() {
        while (!isUserAuthenticated()) {
            printAuthMenu();
            String input = sc.nextLine().trim();

            switch (input) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegistration();
                    handleLogin();
                    break;
                default:
                    System.out.println("Invalid input. Please choose again.");
                    break;
            }
        }
    }
    private void printAuthMenu() {
        System.out.println("Welcome to the Bank of CLI\n");
        System.out.println("Would you like to login or register?:");
        System.out.println("1. Login");
        System.out.println("2. Register");
    }

    private void handleRegistration() {
        // Will add logging at a later date
        boolean registerSuccess = false;
        while (!registerSuccess) {
            System.out.println("Account Registration");
            System.out.print("Enter an account ID: ");
            String accountId = sc.nextLine();
            System.out.print("Enter a PIN: ");
            String password = sc.nextLine();
            if (authService.register(accountId, password)) {
                System.out.println("Registration complete. Please log in.");
                registerSuccess = true;
            } else {
                System.out.println("Registration failed. Try again.");
            }
        }
    }

    private void handleLogin() {
        // Will add logging at a later date
        System.out.println("Account Login");
        System.out.print("Enter an account ID: ");
        String accountId = sc.nextLine();
        System.out.print("Enter a PIN: ");
        String password = sc.nextLine();

        if (authService.login(accountId, password)) {
            System.out.println("Login successful.");
        } else {
            System.out.println("Login failed. Try again.");
        }
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
                    handleTransactionServices();
                    break;
                case "2":
                    boolean userWantsExit = handleBalanceManagement();
                    if (userWantsExit) {
                        exit = true;
                    }
                    break;
                case "3":
                    // transaction history
                    exit = handleHistoryManagementMenu();
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

    private void printHistoryManagementMenu(){
        System.out.println("What transactions would you like to print: ");
        System.out.println("1. Print all checking account transactions");
        System.out.println("2. Print all savings account transactions");
        System.out.println("3. Print all transactions from custom range of dates");
        System.out.println("4. Return to main menu");
        System.out.println("5. Exit");
    }

    private boolean handleHistoryManagementMenu(){
        boolean returnToMainMenu = false;
        boolean quickExit = false;
        while(!returnToMainMenu){
            printHistoryManagementMenu();
            int choice = Integer.parseInt(sc.nextLine());
            if (choice == 4) {
                System.out.println("Returning to Main Menu");
                returnToMainMenu = true;
            } else if (choice == 1) {
                //middle layer finds and prints all checking transactions
                System.out.println("Printing checking account transactions");
            } else if (choice == 2) {
                //middle layer finds and prints all saving transactions
                System.out.println("Printing savings account transactions");
            } else if (choice == 3) {
                chooseDatesMenu();
            } else if (choice == 5){
                System.out.println("Exiting Bank Application.");
                returnToMainMenu = true;
                quickExit = true;
            }else {
                System.out.println("Invalid Input, try again");
            }
        }
        return quickExit;
    }


    private void chooseDatesMenu() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        String startDateString, endDateString;
        System.out.println("Enter start date(MM-DD-YYYY):");
        try {
            startDateString = sc.nextLine();
            LocalDate startDate = LocalDate.parse(startDateString, dateFormatter);
        } catch (DateTimeParseException msg) {
            System.out.println("Invalid input: not a date");
            return;
        }
        System.out.println("Enter end date(MM-DD-YYYY):");
        try {
            endDateString = sc.nextLine();
            LocalDate endDate = LocalDate.parse(endDateString, dateFormatter);
        } catch (DateTimeParseException msg) {
            System.out.println("Invalid input: not a date");
            return;
        }

        //print transactions from  start date to  end date
        System.out.println("Printing transactions from " + startDateString + " to " + endDateString);
    }
  
    private void printTransactionServicesMenu() {
        System.out.println("What kind of transaction would you like to do?");
        System.out.println("1. Make a deposit");
        System.out.println("2. Make a withdrawal");
        System.out.println("3. Transfer money between accounts");
        System.out.println("4. Return to main menu");
    }

    private void handleTransactionServices() {
        boolean returnToMainMenu = false;


        while (!returnToMainMenu) {
            printTransactionServicesMenu();
            String input = sc.nextLine().trim();
            switch (input) {
                case "1":
                    handleDeposit();
                    break;
                case "2":
                    handleWithdrawal();
                    break;
                case "3":
                    handleTransfer();
                    break;
                case "4":
                    System.out.println("Returning to main menu...");
                    returnToMainMenu = true;
                    break;
                default:
                    System.out.println("Invalid input. please try again.");
                    break;
            }
        }
    }

    private void handleWithdrawal() {
        System.out.print("Enter withdrawal amount: $");
        try {
            double withdrawalAmount = Double.parseDouble(sc.nextLine().trim());
            if (transactionService.withdraw(withdrawalAmount)) {
                System.out.println("Withdrawal successful.");
            } else {
                System.out.println("Withdrawal failed.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid dollar amount.");
        }
    }

    private void handleDeposit() {
        System.out.print("Enter deposit amount: $");
        try {
            double depositAmount = Double.parseDouble(sc.nextLine().trim());
            if (transactionService.deposit(depositAmount)) {
                System.out.println("Deposit successful.");
            } else {
                System.out.println("Deposit failed.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid dollar amount.");
        }
    }

    private void handleTransfer() {
        System.out.println("How would you like to perform the transfer?");
        System.out.println("1. Checking Account -> Savings Account");
        System.out.println("2. Savings Account -> Checkings Account");

        try {
            int direction = sc.nextInt();
            sc.nextLine();
            System.out.print("Enter transfer amount: $");
            double transferAmount = Double.parseDouble(sc.nextLine().trim());
            if (transactionService.transfer(transferAmount, direction)) {
                System.out.println("Transfer successful");
            } else {
                System.out.println("Transfer failed");
            }

        } catch (NumberFormatException e) {
        System.out.println("Invalid dollar amount.");
        }
    }

    private boolean isUserAuthenticated() {
        return AuthenticatedAccountContext.getAuthenticatedUserId() != null;
    }
}
