package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.database.Database;
import dragon.entity.Account;
import dragon.repository.AccountRepository;
import dragon.repository.DepositTransactionRepository;
import dragon.repository.TransferTransactionRepository;
import dragon.repository.WithdrawalTransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionServiceTest {

    private static final String TEST_DATABASE_URL = "jdbc:sqlite:file:transactionServiceTest?mode=memory&cache=shared";

    private Connection holdingConnection;
    private AccountRepository accountRepository;
    private TransactionService transactionService;
    private UUID userId;

    @BeforeEach
    void setUp() throws SQLException {
        System.setProperty(Database.DATABASE_URL_PROPERTY, TEST_DATABASE_URL);
        holdingConnection = DriverManager.getConnection(TEST_DATABASE_URL);
        try (Statement statement = holdingConnection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS CheckingAccount");
            statement.execute("DROP TABLE IF EXISTS SavingAccount");
            statement.execute("DROP TABLE IF EXISTS TransferTransaction");
        }
        Database.initialize();

        accountRepository = new AccountRepository();
        transactionService = new TransactionService(
                accountRepository,
                new DepositTransactionRepository(),
                new WithdrawalTransactionRepository(),
                new TransferTransactionRepository());

        userId = UUID.randomUUID();
        accountRepository.createCheckingAccount(holdingConnection, new Account(UUID.randomUUID(), userId, 500.0));
        accountRepository.createSavingAccount(holdingConnection, new Account(UUID.randomUUID(), userId, 0.0));
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);
    }

    @AfterEach
    void tearDown() throws SQLException {
        AuthenticatedAccountContext.setAuthenticatedUserId(null);
        System.clearProperty(Database.DATABASE_URL_PROPERTY);
        holdingConnection.close();
    }

    @Test
    void transferPositive() throws SQLException {
        // Valid transfer
        boolean result = transactionService.transfer(100.0, 1);

        assertTrue(result);
        assertEquals(400.0, accountRepository.findByOwnerId(holdingConnection, userId).getBalance(), 0.0001);
        assertEquals(100.0, accountRepository.findSavingsByOwnerId(holdingConnection, userId).getBalance(), 0.0001);
    }

    @Test
    void transferNegative() throws SQLException {
        // Transfer when invalid negative value is entered
        boolean result = transactionService.transfer(-50.0, 1);

        assertFalse(result);
        assertEquals(500.0, accountRepository.findByOwnerId(holdingConnection, userId).getBalance(), 0.0001);
        assertEquals(0.0, accountRepository.findSavingsByOwnerId(holdingConnection, userId).getBalance(), 0.0001);
    }
}

