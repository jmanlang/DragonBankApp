package dragon.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dragon.AuthenticatedAccountContext;
import dragon.entity.CheckingAccount;
import dragon.repository.CheckingAccountRepository;
import dragon.repository.SavingAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BalanceServiceLoggingTest {

    private Connection connection;
    private CheckingAccountRepository checkingAccountRepository;
    private SavingAccountRepository savingAccountRepository;
    private BalanceService balanceService;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE CheckingAccount (" +
                            "id TEXT PRIMARY KEY, owner TEXT NOT NULL, balance REAL NOT NULL DEFAULT 0)"
            );
            statement.execute(
                    "CREATE TABLE SavingAccount (" +
                            "id TEXT PRIMARY KEY, owner TEXT NOT NULL, balance REAL NOT NULL DEFAULT 0, " +
                            "interestRate REAL NOT NULL DEFAULT 0)"
            );
        }
        checkingAccountRepository = new CheckingAccountRepository();
        savingAccountRepository = new SavingAccountRepository();
        balanceService = new BalanceService(connection, checkingAccountRepository, savingAccountRepository);

        Logger balanceServiceLogger = (Logger) LoggerFactory.getLogger(BalanceService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        balanceServiceLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() throws SQLException {
        AuthenticatedAccountContext.setAuthenticatedUserId(null);
        connection.close();
    }

    @Test
    void logsInfo_whenCheckingBalanceRetrievedSuccessfully() throws SQLException {
        UUID userId = UUID.randomUUID();
        checkingAccountRepository.createCheckingAccount(
                connection, new CheckingAccount(UUID.randomUUID(), userId, 300.0));
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);

        balanceService.getCheckingAccountBalance();

        List<ILoggingEvent> events = logAppender.list;
        assertTrue(events.stream().anyMatch(event -> event.getLevel() == Level.INFO), "Expected an INFO log entry when a balance is retrieved successfully");
        assertTrue(events.stream().noneMatch(event -> event.getLevel() == Level.ERROR), "Should not log ERROR on a successful lookup");
    }

    @Test
    void logsInfo_whenNoAccountFound() throws SQLException {
        UUID userId = UUID.randomUUID();
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);

        balanceService.getCheckingAccountBalance();

        assertTrue(logAppender.list.stream().anyMatch(event -> event.getLevel() == Level.INFO), "Expected an INFO log entry when no account is found (not an error case)");
    }

    @Test
    void logsError_whenDatabaseCallFails() throws SQLException {
        UUID userId = UUID.randomUUID();
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);

        connection.close();

        try {
            balanceService.getCheckingAccountBalance();
        } catch (SQLException expected) {
        }

        List<ILoggingEvent> events = logAppender.list;
        long errorCount = events.stream().filter(event -> event.getLevel() == Level.ERROR).count();
        assertEquals(1, errorCount, "Expected exactly one ERROR log entry when the database call fails");
    }
}