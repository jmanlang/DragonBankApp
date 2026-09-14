package dragon;

import dragon.database.Database;
import dragon.repository.AccountRepository;
import dragon.repository.DepositTransactionRepository;
import dragon.repository.UserRepository;
import dragon.repository.WithdrawalTransactionRepository;
import dragon.service.AuthService;
import dragon.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            // Made it reset the database for each run so we dont have 100 test accounts
            Database.resetAndInitialize();
            AuthenticatedAccountContext.setAuthenticatedUserId(null);

            UserRepository userRepository = new UserRepository();
            AccountRepository accountRepository = new AccountRepository();
            DepositTransactionRepository depositRepository = new DepositTransactionRepository();
            WithdrawalTransactionRepository withdrawalRepository = new WithdrawalTransactionRepository();

            AuthService authService = new AuthService(userRepository, accountRepository);
            TransactionService transactionService = new TransactionService(
                    accountRepository,
                    depositRepository,
                    withdrawalRepository
            );

            logger.info("Bank of CLI application started successfully with fresh tables.");
            new BankController(authService, transactionService).init();
        } catch (SQLException e) {
            logger.error("Bank of CLI could not start because the database is unavailable.", e);
            System.out.println("Banking service is currently unavailable. Please try again later.");
        }
    }
}
