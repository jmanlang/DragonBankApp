package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.entity.CheckingAccount;
import dragon.entity.SavingAccount;
import dragon.repository.CheckingAccountRepository;
import dragon.repository.SavingAccountRepository;
import dragon.database.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BalanceServiceTest {

    private Connection connection;
    private CheckingAccountRepository checkingAccountRepository;
    private SavingAccountRepository savingAccountRepository;
    private BalanceService balanceService;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute(Database.CREATE_CHECKING_ACCOUNT);
            statement.execute(Database.CREATE_SAVING_ACCOUNT);
        }
        checkingAccountRepository = new CheckingAccountRepository();
        savingAccountRepository = new SavingAccountRepository();
        balanceService = new BalanceService(connection, checkingAccountRepository, savingAccountRepository);
    }

    @AfterEach
    void tearDown() throws SQLException {
        AuthenticatedAccountContext.setAuthenticatedUserId(null);
        connection.close();
    }

    @Test
    void getCheckingAccountBalance_returnsNull_whenUserHasNoAccount() throws SQLException {
        UUID userId = UUID.randomUUID();
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);

        assertNull(balanceService.getCheckingAccountBalance());
    }

    @Test
    void getSavingAccountBalance_returnsNull_whenUserHasNoAccount() throws SQLException {
        UUID userId = UUID.randomUUID();
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);

        assertNull(balanceService.getSavingAccountBalance());
    }

    @Test
    void getCheckingAccountBalance_returnsRealBalance_whenAccountExists() throws SQLException {
        UUID userId = UUID.randomUUID();
        checkingAccountRepository.createCheckingAccount(connection, new CheckingAccount(UUID.randomUUID(), userId, 1200.50));
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);

        assertEquals(1200.50, balanceService.getCheckingAccountBalance(), 0.0001);
    }

    @Test
    void getSavingAccountBalance_returnsRealBalance_whenAccountExists() throws SQLException {
        UUID userId = UUID.randomUUID();
        savingAccountRepository.createSavingAccount(connection, new SavingAccount(UUID.randomUUID(), userId, 5000.00, 0.02));
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);

        assertEquals(5000.00, balanceService.getSavingAccountBalance(), 0.0001);
    }

    @Test
    void checkingAndSavingBalances_areNotMixedUp() throws SQLException {
        UUID userId = UUID.randomUUID();
        checkingAccountRepository.createCheckingAccount(connection, new CheckingAccount(UUID.randomUUID(), userId, 100.0));
        savingAccountRepository.createSavingAccount(connection, new SavingAccount(UUID.randomUUID(), userId, 999.0, 0.01));AuthenticatedAccountContext.setAuthenticatedUserId(userId);

        assertEquals(100.0, balanceService.getCheckingAccountBalance(), 0.0001, "Checking balance should be 100, not the saving balance");
        assertEquals(999.0, balanceService.getSavingAccountBalance(), 0.0001, "Saving balance should be 999, not the checking balance");
    }

    @Test
    void getCheckingAccountBalance_negative_propagatesExceptionOnDatabaseFailure() throws SQLException {
        UUID userId = UUID.randomUUID();
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);
        connection.close();

        assertThrows(SQLException.class, () -> balanceService.getCheckingAccountBalance(), "A database failure should propagate to the caller, not be silently swallowed");
    }

    @Test
    void getSavingAccountBalance_negative_propagatesExceptionOnDatabaseFailure() throws SQLException {
        UUID userId = UUID.randomUUID();
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);
        connection.close();

        assertThrows(SQLException.class, () -> balanceService.getSavingAccountBalance(), "A database failure should propagate to the caller, not be silently swallowed");
    }
}