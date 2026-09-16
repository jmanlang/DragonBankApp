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
import dragon.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class HistoryServiceTest {
    HistoryService historyService;
    private static final String TEST_DB_URL = "jdbc:sqlite:file::memory:?cache=shared";
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        TransactionRepository transactionRepository = new TransactionRepository();
        System.setProperty(Database.DATABASE_URL_PROPERTY, TEST_DB_URL);
        connection = DriverManager.getConnection(TEST_DB_URL);
        historyService = new HistoryService(transactionRepository);
        try (PreparedStatement statement = connection.prepareStatement(Database.CREATE_TRANSFER_TRANSACTION)) {
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(Database.CREATE_DEPOSIT_TRANSACTION)) {
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(Database.CREATE_WITHDRAWAL_TRANSACTION)) {
            statement.executeUpdate();
        }
        UUID userId = UUID.randomUUID();
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);
    }

    @AfterEach
    void tearDown() throws SQLException {
        AuthenticatedAccountContext.setAuthenticatedUserId(null);
        connection.close();
        System.clearProperty(Database.DATABASE_URL_PROPERTY);
    }

    void setupInsertTransaction() throws SQLException{
        UUID userId = UUID.randomUUID();
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);
        String userIdString = userId.toString();
        String toAccountId = String.valueOf(UUID.randomUUID());
        String fromAccountId = String.valueOf(UUID.randomUUID());
        String transactionAccountId = String.valueOf(UUID.randomUUID());
        String date = String.valueOf(Instant.now());
        String sql = """
                INSERT INTO TransferTransaction (id, userId, fromAccount, toAccount, amount, date)
                VALUES (?, ?, ?, ?, ?, ?)""";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transactionAccountId);
            statement.setString(2, userIdString);
            statement.setString(3, fromAccountId);
            statement.setString(4, toAccountId);
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
        assertNotNull(historyService.getAllHistory());
    }

    @Test
    void testGetAllHistoryNoTransactions(){
        assertNull(historyService.getAllHistory());
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
