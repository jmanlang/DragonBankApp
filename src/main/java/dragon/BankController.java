package dragon;

import dragon.entity.HasDate;
import dragon.service.AuthService;
import dragon.service.HistoryService;
import dragon.service.TransactionService;
import dragon.service.BalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import java.sql.SQLException;
import java.util.Scanner;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BankController {
    private final AuthService authService;
    private final TransactionService transactionService;
    private final HistoryService historyService;

    private final BalanceService balanceService;

    private final Scanner sc = new Scanner(System.in);
    private final static Logger logger = LoggerFactory.getLogger(BankController.class);

    public BankController(AuthService authService, TransactionService transactionService, BalanceService balanceService, HistoryService historyService) {
        this.authService = authService;
        this.transactionService = transactionService;
        this.historyService = historyService;
        this.balanceService = balanceService;
    }

    public void init() throws SQLException {
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


    private void handleServicesMenu() throws SQLException {
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
                    System.out.println("Not a valid option. Please choose again.");
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

    private boolean handleBalanceManagement() throws SQLException {
        boolean returnToMainMenu = false;
        boolean quickExit = false;

        while (!returnToMainMenu) {
            printBalanceManagementMenu();
            String input = sc.nextLine().trim();
            switch (input) {
                case "1":
                    handleSavingBalance();
                    break;
                case "2":
                    handleCheckingBalance();
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
                    System.out.println("Not a valid option. Please choose again.");
                    break;
            }
        }
        return quickExit;
    }

    private void handleCheckingBalance() throws SQLException {
        Double checkingBalance = balanceService.getCheckingAccountBalance();
        if (checkingBalance == null) {
            System.out.println("Checking account not found. Please try again.");
        } else {
            System.out.println("Checking balance is " + checkingBalance);
        }
    }

    private void handleSavingBalance() throws SQLException {
            Double savingBalance = balanceService.getSavingAccountBalance();
            if (savingBalance == null) {
                System.out.println("Saving account not found. Please try again.");
            }  else {
                System.out.println("Saving balance is " + savingBalance);
            }
    }

    private void printHistoryManagementMenu(){
        System.out.println("What transactions would you like to print: ");
        System.out.println("1. Print all transactions");
        System.out.println("2. Print all transactions from custom range of dates");
        System.out.println("3. Return to main menu");
        System.out.println("4. Exit");
    }

    private boolean handleHistoryManagementMenu(){
        boolean returnToMainMenu = false;
        boolean quickExit = false;
        while(!returnToMainMenu){
            printHistoryManagementMenu();
            int choice = Integer.parseInt(sc.nextLine());
            if(choice == 1){
                List<HasDate> transactions = historyService.getAllHistory();
                if(transactions != null){
                    printTransactions(transactions);
                }else{
                    System.out.println("There was a problem printing your transaction history or you have no history, try again later.");
                }
            }else if (choice == 2) {
                List<HasDate> transactions = chooseDatesMenu();
                if(transactions != null){
                    printTransactions(transactions);
                } else{
                    System.out.println("There was a problem printing your transaction history or you have no history between those dates, try again later.");
                }
            } else if (choice == 3) {
                System.out.println("Returning to Main Menu");
                returnToMainMenu = true;
            } else if (choice == 4){
                System.out.println("Exiting Bank Application.");
                returnToMainMenu = true;
                quickExit = true;
            }else {
                System.out.println("Invalid Input, try again");
            }
        }
        return quickExit;
    }


    private List<HasDate> chooseDatesMenu() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZoneId zoneId = ZoneId.systemDefault();
        String startDateString = "", endDateString = " ";
        System.out.println("Enter start date(YYYY-MM-DD):");
        LocalDate startDate = null, endDate = null;
        Instant startInstant = null, endInstant = null;
        try {
            startDateString = sc.nextLine();
            startDate = LocalDate.parse(startDateString, dateFormatter);
            startInstant = startDate.atStartOfDay(zoneId).toInstant();
        } catch (DateTimeParseException msg) {
            logger.error("Invalid Input: User did not input start date in correct format.");
            System.out.println("Invalid input: not a date");
            return null;
        }
        System.out.println("Enter end date(YYYY-MM-DD):");
        try {
            endDateString = sc.nextLine();
            endDate = LocalDate.parse(endDateString, dateFormatter);
            endInstant = endDate.atTime(LocalTime.MAX.withNano(0)).atZone(zoneId).toInstant();

        } catch (DateTimeParseException msg) {
            logger.error("Invalid Input: User did not input end date in correct format.");
            System.out.println("Invalid input: not a date");
            return null;
        }
        //print transactions from  start date to  end date
        System.out.println("Printing transactions from " + startDateString + " to " + endDateString);
        return this.historyService.getRangeHistory(startInstant, endInstant);
    }

    private void printTransactions(List<HasDate> transactions){
        for(HasDate transaction: transactions){
            System.out.println(transaction.toString());
        }
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

        int direction = -1;
        while (direction != 1 && direction != 2) {
            String input = sc.nextLine().trim();
            if (input.equals("1") || input.equals("2")) {
                direction = Integer.parseInt(input);
            } else {
                System.out.print("Please enter a valid operation: ");
            }
        }

        try {
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
