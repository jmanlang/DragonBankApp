package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.database.Database;
import dragon.entity.CheckingAccount;
import dragon.entity.SavingAccount;
import dragon.repository.CheckingAccountRepository;
import dragon.repository.DepositTransactionRepository;
import dragon.repository.SavingAccountRepository;
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

    private Connection connection;
    private CheckingAccountRepository checkingAccountRepository;
    private SavingAccountRepository savingAccountRepository;
    private TransactionService transactionService;
    private UUID userId;

    @BeforeEach
    void setUp() throws SQLException {
        System.setProperty(Database.DATABASE_URL_PROPERTY,
                "jdbc:sqlite:file:test_bank?mode=memory&cache=shared");

        connection = Database.getConnection();
        Database.initialize();

        checkingAccountRepository = new CheckingAccountRepository();
        savingAccountRepository = new SavingAccountRepository();
        transactionService = new TransactionService(
                checkingAccountRepository,
                savingAccountRepository,
                new DepositTransactionRepository(),
                new WithdrawalTransactionRepository(),
                new TransferTransactionRepository()
        );

        userId = UUID.randomUUID();
        checkingAccountRepository.createCheckingAccount(connection, new CheckingAccount(UUID.randomUUID(), userId, 500.0));
        savingAccountRepository.createSavingAccount(connection, new SavingAccount(UUID.randomUUID(), userId, 0.0, 0.0));
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);
    }

    @AfterEach
    void tearDown() throws SQLException {
        AuthenticatedAccountContext.setAuthenticatedUserId(null);
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        System.clearProperty(Database.DATABASE_URL_PROPERTY);
    }

    @Test
    void transferPositive() throws SQLException {
        // Valid transfer
        boolean result = transactionService.transfer(100.0, 1);

        assertTrue(result);
        assertEquals(400.0, checkingAccountRepository.findByOwnerID(connection, userId).getBalance());
        assertEquals(100.0, savingAccountRepository.findByOwnerID(connection, userId).getBalance());


    }

    @Test
    void transferNegative() throws SQLException {

        // Transfer negative funds
        boolean result = transactionService.transfer(-50.0, 1);

        assertFalse(result);
        assertEquals(500.0, checkingAccountRepository.findByOwnerID(connection, userId).getBalance());
        assertEquals(0.0, savingAccountRepository.findByOwnerID(connection, userId).getBalance());
    }

    @Test
    void depositPositive() throws SQLException {
        // Valid deposit of $50
        boolean result = transactionService.deposit(50);

        assertTrue(result);
        assertEquals(550.0, checkingAccountRepository.findByOwnerID(connection, userId).getBalance());
        assertEquals(0.0, savingAccountRepository.findByOwnerID(connection, userId).getBalance());
    }

    @Test
    void depositNegative() throws SQLException {
        // Invalid deposit of -$50
        boolean result = transactionService.deposit(-50);

        assertFalse(result);
        assertEquals(500.0, checkingAccountRepository.findByOwnerID(connection, userId).getBalance());
        assertEquals(0.0, savingAccountRepository.findByOwnerID(connection, userId).getBalance());
    }

    @Test
    void withdrawPositive() throws SQLException {
        // Valid withdrawal of $50
        boolean result = transactionService.withdraw(50);

        assertTrue(result);
        assertEquals(450.0, checkingAccountRepository.findByOwnerID(connection, userId).getBalance());
        assertEquals(0.0, savingAccountRepository.findByOwnerID(connection, userId).getBalance());
    }

    @Test
    void withdrawNegative() throws SQLException {
        // Invalid withdrawal of -$50
        boolean result = transactionService.withdraw(-50);

        assertFalse(result);
        assertEquals(500.0, checkingAccountRepository.findByOwnerID(connection, userId).getBalance());
        assertEquals(0.0, savingAccountRepository.findByOwnerID(connection, userId).getBalance());
    }


}
