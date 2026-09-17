package dragon.service;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import dragon.AuthenticatedAccountContext;
import dragon.database.Database;
import dragon.repository.CheckingAccountRepository;
import dragon.repository.SavingAccountRepository;
import dragon.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class HistoryServiceTest {
    HistoryService historyService;
    private static final String TEST_DB_URL = "jdbc:sqlite:file::memory:?cache=shared";
    private Connection connection;
    private UUID userId;
    private UUID fromAccountId;
    private UUID toAccountId;

    @BeforeEach
    void setUp() throws SQLException {
        TransactionRepository transactionRepository = new TransactionRepository();
        CheckingAccountRepository checkingAccountRepository = new CheckingAccountRepository();
        SavingAccountRepository savingAccountRepository = new SavingAccountRepository();
        System.setProperty(Database.DATABASE_URL_PROPERTY, TEST_DB_URL);
        connection = DriverManager.getConnection(TEST_DB_URL);
        historyService = new HistoryService(transactionRepository, checkingAccountRepository, savingAccountRepository);
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(Database.CREATE_USER);
            statement.executeUpdate(Database.CREATE_CHECKING_ACCOUNT);
            statement.executeUpdate(Database.CREATE_SAVING_ACCOUNT);
            statement.executeUpdate(Database.CREATE_TRANSFER_TRANSACTION);
            statement.executeUpdate(Database.CREATE_DEPOSIT_TRANSACTION);
            statement.executeUpdate(Database.CREATE_WITHDRAWAL_TRANSACTION);
        }
        this.userId = UUID.randomUUID();
        this.fromAccountId = UUID.randomUUID();
        this.toAccountId = UUID.randomUUID();
        AuthenticatedAccountContext.setAuthenticatedUserId(this.userId);
    }

    @AfterEach
    void tearDown() throws SQLException {
        AuthenticatedAccountContext.setAuthenticatedUserId(null);
        connection.close();
        System.clearProperty(Database.DATABASE_URL_PROPERTY);
    }

    void setupInsertTransaction() throws SQLException{
        String userIdString = this.userId.toString();
        System.out.println(userIdString);
        String fromAccountIdString = this.fromAccountId.toString();
        String toAccountIdString = this.toAccountId.toString();
        String transactionAccountId = String.valueOf(UUID.randomUUID());
        String date = String.valueOf(Instant.now());
        String sql = """
                INSERT INTO CheckingAccount (id, balance, owner)
                VALUES (?, ?, ?)""";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fromAccountIdString);
            statement.setDouble(2, 0);
            statement.setString(3, userIdString);
            statement.executeUpdate();
        }

        String sql2 = """
                INSERT INTO SavingAccount (id, balance, owner)
                VALUES (?, ?, ?)""";

        try (PreparedStatement statement = connection.prepareStatement(sql2)) {
            statement.setString(1, toAccountIdString);
            statement.setDouble(2, 0);
            statement.setString(3, userIdString);
            statement.executeUpdate();
        }

        String sql3 = """
                INSERT INTO TransferTransaction (id, userId, fromAccount, toAccount, amount, date)
                VALUES (?, ?, ?, ?, ?, ?)""";
        try (PreparedStatement statement = connection.prepareStatement(sql3)) {
            statement.setString(1, transactionAccountId);
            statement.setString(2, userIdString);
            statement.setString(3, fromAccountIdString);
            statement.setString(4, toAccountIdString);
            statement.setDouble(5, 1000);
            statement.setString(6, date);
            statement.executeUpdate();
        }
    }

    Instant convertStartDate(String startDate){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate startLocalDate = LocalDate.parse(startDate, dateFormatter);
        return startLocalDate.atStartOfDay(zoneId).toInstant();
    }

    Instant convertEndDate(String endDate){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate endLocalDate = LocalDate.parse(endDate, dateFormatter);
        return endLocalDate.atTime(LocalTime.MAX.withNano(0)).atZone(zoneId).toInstant();
    }


    @Test
    void testGetAllHistoryPositive() throws SQLException{
        setupInsertTransaction();
        assertNotNull(historyService.getAllHistory("1"));
    }

    @Test
    void testGetAllHistoryNoTransactions(){
        assertNull(historyService.getAllHistory("1"));
    }

    @Test
    void testGetRangeHistoryPositive() throws SQLException{
        setupInsertTransaction();
        Instant startInstant = convertStartDate("2006-09-01");
        Instant endInstant = convertEndDate("2030-09-01");

        assertNotNull(historyService.getRangeHistory(startInstant, endInstant));
    }

    @Test
    void testGetRangeHistoryInvalidDateOrder() throws SQLException {
        setupInsertTransaction();
        Instant startInstant = convertStartDate("2026-09-01");
        Instant endInstant = convertEndDate("2016-09-01");

        assertNull(historyService.getRangeHistory(startInstant, endInstant));
    }

}
