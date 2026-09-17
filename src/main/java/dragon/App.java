package dragon;

import dragon.database.Database;
import dragon.repository.*;
import dragon.service.*;
import java.sql.SQLException;

import java.sql.Connection;

public class App {
    public static void main(String[] args) {
        try {
            Database.initialize();
            Connection connection = Database.getConnection();

            TransactionRepository transactionRepository = new TransactionRepository();
            UserRepository userRepository = new UserRepository();
            CheckingAccountRepository checkingAccountRepository = new CheckingAccountRepository();
            SavingAccountRepository savingAccountRepository = new SavingAccountRepository();
            HistoryService historyService = new HistoryService(transactionRepository, checkingAccountRepository, savingAccountRepository);
            BalanceService balanceService = new BalanceService(checkingAccountRepository, savingAccountRepository);
            AuthService authService = new AuthService(userRepository, checkingAccountRepository, savingAccountRepository);
            DepositTransactionRepository depositTransactionRepository = new DepositTransactionRepository();
            WithdrawalTransactionRepository withdrawalTransactionRepository = new WithdrawalTransactionRepository();
            TransferTransactionRepository transferTransactionRepository = new TransferTransactionRepository();
            TransactionService transactionService = new TransactionService(
                    checkingAccountRepository,
                    savingAccountRepository,
                    depositTransactionRepository,
                    withdrawalTransactionRepository,
                    transferTransactionRepository
            );
            BankController app = new BankController(authService, transactionService, balanceService, historyService);

            app.init();
        } catch (SQLException e) {
            System.out.println("The bank could not start because the database is unavailable.");
        }
    }
}