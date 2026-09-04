package dragon;

import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    //    public static void main(String[] args){
//        BankController myBank = new BankController();
//        myBank.handleHistoryManagementMenu();
//    }


}
