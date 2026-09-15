package dragon;

import dragon.database.Database;
import dragon.repository.AccountRepository;
import dragon.repository.DepositTransactionRepository;
import dragon.repository.UserRepository;
import dragon.repository.WithdrawalTransactionRepository;
import dragon.service.AuthService;
import dragon.service.TransactionService;

import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        try {
            Database.initialize();

            UserRepository userRepository = new UserRepository();
            AuthService authService = new AuthService(userRepository);
            AccountRepository accountRepository = new AccountRepository();
            DepositTransactionRepository depositTransactionRepository = new DepositTransactionRepository();
            WithdrawalTransactionRepository withdrawalTransactionRepository = new WithdrawalTransactionRepository();
            TransactionService transactionService = new TransactionService(
                    accountRepository,
                    depositTransactionRepository,
                    withdrawalTransactionRepository
            );
            BankController app = new BankController(authService, transactionService);
            app.init();
        } catch (SQLException e) {
            System.out.println("The bank could not start because the database is unavailable.");
        }
    }
}
