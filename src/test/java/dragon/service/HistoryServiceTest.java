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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class HistoryServiceTest {
    HistoryService historyService;
    private Connection connection;


    @BeforeEach
    void setUp() throws SQLException {
        TransactionRepository transactionRepository = new TransactionRepository();
        connection = Database.getConnection();
        historyService = new HistoryService(transactionRepository, connection);
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS TransferTransaction (
                        id TEXT PRIMARY KEY,
                        userId TEXT NOT NULL REFERENCES User(id),
                        fromAccount TEXT NOT NULL,
                        toAccount TEXT NOT NULL,
                        amount REAL NOT NULL CHECK (amount > 0),
                        date TEXT NOT NULL
                    )
                    """);
        }
        UUID userId = UUID.randomUUID();
        AuthenticatedAccountContext.setAuthenticatedUserId(userId);
    }

    void setupPositiveTests() throws SQLException{
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
    @Test
    void testGetAllHistoryPositive() throws SQLException{
        setupPositiveTests();
        assertNotNull(historyService.getAllHistory());
    }


    @Test
    void testGetAllHistoryNoTransactions(){
        assertNull(historyService.getAllHistory());
    }

    @Test
    void testGetRangeHistoryPositive() throws SQLException{

        setupPositiveTests();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDate startDate = LocalDate.parse("2006-09-01", dateFormatter);
        Instant startInstant = startDate.atStartOfDay(zoneId).toInstant();

        LocalDate endDate = LocalDate.parse("2030-09-01", dateFormatter);
        Instant endInstant = endDate.atTime(LocalTime.MAX.withNano(0)).atZone(zoneId).toInstant();

        assertNotNull(historyService.getRangeHistory(startInstant, endInstant));
    }

    @Test
    void testGetRangeHistoryInvalidDateOrder() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDate startDate = LocalDate.parse("2026-09-01", dateFormatter);
        Instant startInstant = startDate.atStartOfDay(zoneId).toInstant();

        LocalDate endDate = LocalDate.parse("2016-09-01", dateFormatter);
        Instant endInstant = endDate.atTime(LocalTime.MAX.withNano(0)).atZone(zoneId).toInstant();


        assertNull(historyService.getRangeHistory(startInstant, endInstant));
    }

}
