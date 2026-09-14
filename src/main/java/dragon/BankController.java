package dragon;

import dragon.exception.AccountAlreadyExistsException;
import dragon.exception.BankingException;
import dragon.exception.InvalidCredentialsException;
import dragon.exception.ServiceUnavailableException;
import dragon.exception.ValidationException;
import dragon.service.AuthService;
import dragon.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

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
                    if (handleRegistration()) {
                        handleLogin();
                    }
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

    private boolean handleRegistration() {
        System.out.println("Account Registration");
        System.out.print("Enter an account ID: ");
        String accountId = sc.nextLine();
        System.out.print("Enter a PIN: ");
        String pin = sc.nextLine();

        try {
            authService.register(accountId, pin);
            System.out.println("Registration complete. Please log in.");
            return true;
        } catch (ValidationException | AccountAlreadyExistsException e) {
            System.out.println(e.getMessage());
        } catch (ServiceUnavailableException e) {
            // The full technical exception is already logged by the service.
            System.out.println(e.getMessage());
        }

        return false;
    }

    private void handleLogin() {
        System.out.println("Account Login");
        System.out.print("Enter an account ID: ");
        String accountId = sc.nextLine();
        System.out.print("Enter a PIN: ");
        String pin = sc.nextLine();

        try {
            authService.login(accountId, pin);
            System.out.println("Login successful.");
        } catch (InvalidCredentialsException e) {
            System.out.println(e.getMessage());
        } catch (ServiceUnavailableException e) {
            // Do not print the technical stack trace to the CLI user.
            System.out.println(e.getMessage());
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
                    handleTransactionServices();
                    break;
                case "2":
                    boolean userWantsExit = handleBalanceManagement();
                    if (userWantsExit) {
                        exit = true;
                    }
                    break;
                case "3":
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
                case "2":
                    System.out.println("Balance display is not implemented in this branch.");
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

    private void printHistoryManagementMenu() {
        System.out.println("What transactions would you like to print: ");
        System.out.println("1. Print all checking account transactions");
        System.out.println("2. Print all savings account transactions");
        System.out.println("3. Print all transactions from custom range of dates");
        System.out.println("4. Return to main menu");
        System.out.println("5. Exit");
    }

    private boolean handleHistoryManagementMenu() {
        boolean returnToMainMenu = false;
        boolean quickExit = false;

        while (!returnToMainMenu) {
            printHistoryManagementMenu();
            String input = sc.nextLine().trim();

            switch (input) {
                case "1":
                case "2":
                case "3":
                    System.out.println("Transaction-history display is not implemented in this branch.");
                    break;
                case "4":
                    System.out.println("Returning to Main Menu");
                    returnToMainMenu = true;
                    break;
                case "5":
                    System.out.println("Exiting Bank Application.");
                    returnToMainMenu = true;
                    quickExit = true;
                    break;
                default:
                    System.out.println("Invalid Input, try again");
                    break;
            }
        }

        return quickExit;
    }

    private void chooseDatesMenu() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        String startDateString;
        String endDateString;

        System.out.println("Enter start date(MM-DD-YYYY):");
        try {
            startDateString = sc.nextLine();
            LocalDate.parse(startDateString, dateFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid input: not a date");
            return;
        }

        System.out.println("Enter end date(MM-DD-YYYY):");
        try {
            endDateString = sc.nextLine();
            LocalDate.parse(endDateString, dateFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid input: not a date");
            return;
        }

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
            BigDecimal amount = new BigDecimal(sc.nextLine().trim());
            BigDecimal newBalance = transactionService.withdraw(amount);
            System.out.println("Withdrawal successful. New balance: $" + newBalance);
        } catch (NumberFormatException e) {
            System.out.println("Invalid dollar amount. Example: 50 or 50.00");
        } catch (BankingException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleDeposit() {
        System.out.print("Enter deposit amount: $");

        try {
            BigDecimal amount = new BigDecimal(sc.nextLine().trim());
            BigDecimal newBalance = transactionService.deposit(amount);
            System.out.println("Deposit successful. New balance: $" + newBalance);
        } catch (NumberFormatException e) {
            System.out.println("Invalid dollar amount. Example: 50 or 50.00");
        } catch (BankingException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleTransfer() {
        System.out.println("Transfers are not implemented yet.");
        System.out.println("Transfer must be implemented as its own single database transaction.");
    }

    private boolean isUserAuthenticated() {
        return AuthenticatedAccountContext.getAuthenticatedUserId() != null;
    }
}
