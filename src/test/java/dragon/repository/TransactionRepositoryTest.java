package dragon.repository;

import dragon.database.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransactionRepositoryTest{
    TransactionRepository transactionRepository;
    private static final String TEST_DB_URL = "jdbc:sqlite:file::memory:?cache=shared";
    Connection connection;
    UUID userID;


    @BeforeEach
    void setUp() throws SQLException {
        this.userID = UUID.randomUUID();
        this.transactionRepository = new TransactionRepository();
        System.setProperty(Database.DATABASE_URL_PROPERTY, TEST_DB_URL);
        connection = DriverManager.getConnection(TEST_DB_URL);
        try (PreparedStatement statement = connection.prepareStatement(Database.CREATE_TRANSFER_TRANSACTION)) {
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(Database.CREATE_DEPOSIT_TRANSACTION)) {
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(Database.CREATE_WITHDRAWAL_TRANSACTION)) {
            statement.executeUpdate();
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
        System.clearProperty(Database.DATABASE_URL_PROPERTY);
    }

    void setupInsertTransaction() throws SQLException{
        String userIdString = this.userID.toString();
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
    void testQueryAllTransactionsPositive() throws SQLException {
        setupInsertTransaction();
        assertEquals(1, transactionRepository.queryAllTransactions(this.userID, connection).size());
    }

    @Test
    void testQueryAllTransactionsInvalidUserIDReturnsEmptyList() throws SQLException{
        assertEquals(0, transactionRepository.queryAllTransactions(this.userID, connection).size());
    }

    @Test
    void testQueryRangeTransactionsPositive() throws  SQLException{
        setupInsertTransaction();
        Instant startInstant = convertStartDate("1000-01-01");
        Instant endInstant = convertEndDate("2099-01-01");
        assertEquals(1, transactionRepository.queryRangeTransactions(this.userID, connection, startInstant, endInstant).size());

    }

    @Test
    void testQueryRangeTransactionsRangeHasNoTransactions() throws  SQLException{
        setupInsertTransaction();
        Instant startInstant = convertStartDate("1001-01-01");
        Instant endInstant = convertEndDate("1002-01-01");
        assertEquals(0, transactionRepository.queryRangeTransactions(this.userID, connection, startInstant, endInstant).size());
    }


    @Test
    void testClosedConnectionThrowsSQLException() throws SQLException {
        this.connection.close();
        assertThrows(SQLException.class , () -> transactionRepository.queryAllTransactions(this.userID, this.connection));
    }
}
