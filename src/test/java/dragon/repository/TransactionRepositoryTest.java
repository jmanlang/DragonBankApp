package dragon.repository;

import dragon.database.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
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
    private Connection connection;
    private UUID userId;
    private UUID fromAccountId;
    private UUID toAccountId;


    @BeforeEach
    void setUp() throws SQLException {
        this.transactionRepository = new TransactionRepository();
        System.setProperty(Database.DATABASE_URL_PROPERTY, TEST_DB_URL);
        connection = DriverManager.getConnection(TEST_DB_URL);
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
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
        System.clearProperty(Database.DATABASE_URL_PROPERTY);
    }

    void setupInsertTransaction() throws SQLException{
        String userIdString = this.userId.toString();
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
    void testQueryAllTransactionsPositive() throws SQLException {
        setupInsertTransaction();
        assertEquals(1, transactionRepository.queryAllTransactions(this.fromAccountId, connection).size());
    }

    @Test
    void testQueryAllTransactionsInvalidUserIDReturnsEmptyList() throws SQLException{
        assertEquals(0, transactionRepository.queryAllTransactions(this.userId, connection).size());
    }

    @Test
    void testQueryRangeTransactionsPositive() throws  SQLException{
        setupInsertTransaction();
        Instant startInstant = convertStartDate("1000-01-01");
        Instant endInstant = convertEndDate("2099-01-01");
        assertEquals(1, transactionRepository.queryRangeTransactions(this.userId, connection, startInstant, endInstant).size());

    }

    @Test
    void testQueryRangeTransactionsRangeHasNoTransactions() throws  SQLException{
        setupInsertTransaction();
        Instant startInstant = convertStartDate("1001-01-01");
        Instant endInstant = convertEndDate("1002-01-01");
        assertEquals(0, transactionRepository.queryRangeTransactions(this.userId, connection, startInstant, endInstant).size());
    }


    @Test
    void testClosedConnectionThrowsSQLException() throws SQLException {
        this.connection.close();
        assertThrows(SQLException.class , () -> transactionRepository.queryAllTransactions(this.userId, this.connection));
    }
}
