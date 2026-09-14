package dragon.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    private static final String DEFAULT_DATABASE_URL = "jdbc:sqlite:./data/bank.db";
    public static final String DATABASE_URL_PROPERTY = "dragon.bank.databaseUrl";

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        String databaseUrl = System.getProperty(DATABASE_URL_PROPERTY, DEFAULT_DATABASE_URL);

        if (DEFAULT_DATABASE_URL.equals(databaseUrl)) {
            File dataDirectory = new File("data");
            if (!dataDirectory.exists() && !dataDirectory.mkdirs()) {
                throw new SQLException("Could not create data directory.");
            }
        }

        Connection connection = DriverManager.getConnection(databaseUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }


    public static void initialize() throws SQLException {
        try (Connection connection = getConnection()) {
            createTables(connection);
        }
    }


    public static void resetAndInitialize() throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DROP TABLE IF EXISTS TransferTransaction");
                statement.executeUpdate("DROP TABLE IF EXISTS DepositTransaction");
                statement.executeUpdate("DROP TABLE IF EXISTS WithdrawalTransaction");
                statement.executeUpdate("DROP TABLE IF EXISTS SavingAccount");
                statement.executeUpdate("DROP TABLE IF EXISTS CheckingAccount");
                statement.executeUpdate("DROP TABLE IF EXISTS User");

                createTables(connection);
                connection.commit();
                logger.info("SQLite tables reset and initialized successfully for a fresh application run.");
            } catch (SQLException e) {
                connection.rollback();
                logger.error("SQLite database reset failed.", e);
                throw e;
            }
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        String createUser = """
                CREATE TABLE IF NOT EXISTS User (
                    id TEXT PRIMARY KEY,
                    accountId TEXT NOT NULL UNIQUE,
                    hashedPassword TEXT NOT NULL
                )
                """;

        String createCheckingAccount = """
                CREATE TABLE IF NOT EXISTS CheckingAccount (
                    id TEXT PRIMARY KEY,
                    balance NUMERIC NOT NULL DEFAULT 0 CHECK (balance >= 0),
                    owner TEXT REFERENCES User(id) ON DELETE SET NULL
                )
                """;

        String createSavingAccount = """
                CREATE TABLE IF NOT EXISTS SavingAccount (
                    id TEXT PRIMARY KEY,
                    balance NUMERIC NOT NULL DEFAULT 0 CHECK (balance >= 0),
                    interestRate NUMERIC NOT NULL DEFAULT 0 CHECK (interestRate >= 0),
                    owner TEXT REFERENCES User(id) ON DELETE SET NULL
                )
                """;

        String createDepositTransaction = """
                CREATE TABLE IF NOT EXISTS DepositTransaction (
                    id TEXT PRIMARY KEY,
                    userId TEXT NOT NULL REFERENCES User(id),
                    amount NUMERIC NOT NULL CHECK (amount > 0),
                    date TEXT NOT NULL
                )
                """;

        String createWithdrawalTransaction = """
                CREATE TABLE IF NOT EXISTS WithdrawalTransaction (
                    id TEXT PRIMARY KEY,
                    userId TEXT NOT NULL REFERENCES User(id),
                    amount NUMERIC NOT NULL CHECK (amount > 0),
                    date TEXT NOT NULL
                )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createUser);
            statement.executeUpdate(createCheckingAccount);
            statement.executeUpdate(createSavingAccount);
            statement.executeUpdate(createDepositTransaction);
            statement.executeUpdate(createWithdrawalTransaction);
        }
    }
}
