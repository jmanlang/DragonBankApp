package dragon;


import dragon.database.Database;
import dragon.repository.*;
import dragon.service.AuthService;
import dragon.service.BalanceService;
import dragon.service.TransactionService;

import java.sql.SQLException;
import java.sql.Connection;

public class App {
    public static void main(String[] args) {
        try {
            Database.initialize();
            Connection connection = Database.getConnection();

            UserRepository userRepository = new UserRepository();
            AccountRepository accountRepository = new AccountRepository();
            CheckingAccountRepository checkingAccountRepository = new CheckingAccountRepository();
            SavingAccountRepository savingAccountRepository = new SavingAccountRepository();
            BalanceService balanceService = new BalanceService(connection, checkingAccountRepository, savingAccountRepository);
            AuthService authService = new AuthService(userRepository, accountRepository);
            DepositTransactionRepository depositTransactionRepository = new DepositTransactionRepository();
            WithdrawalTransactionRepository withdrawalTransactionRepository = new WithdrawalTransactionRepository();
            TransferTransactionRepository transferTransactionRepository = new TransferTransactionRepository();
            TransactionService transactionService = new TransactionService(
                    accountRepository,
                    depositTransactionRepository,
                    withdrawalTransactionRepository,
                    transferTransactionRepository
            );
            BankController app = new BankController(authService, transactionService, balanceService);
            app.init();
        } catch (SQLException e) {
            System.out.println("The bank could not start because the database is unavailable.");
        }
    }
}
