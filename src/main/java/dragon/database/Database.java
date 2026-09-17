package dragon.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DEFAULT_DATABASE_URL = "jdbc:sqlite:./data/bank.db";
    public static final String DATABASE_URL_PROPERTY = "dragon.bank.databaseUrl";

    public static final String CREATE_USER = """
                    CREATE TABLE IF NOT EXISTS User (
                        id TEXT PRIMARY KEY,
                        accountId TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL
                    )
                    """;
    public static final String CREATE_CHECKING_ACCOUNT = """
                    CREATE TABLE IF NOT EXISTS CheckingAccount (
                        id TEXT PRIMARY KEY,
                        balance REAL NOT NULL DEFAULT 0 CHECK (balance >= 0),
                        owner TEXT NOT NULL REFERENCES User(id)
                    )
                    """;

    public static final String CREATE_SAVING_ACCOUNT = """
                    CREATE TABLE IF NOT EXISTS SavingAccount (
                        id TEXT PRIMARY KEY,
                        balance REAL NOT NULL DEFAULT 0 CHECK (balance >= 0),
                        interestRate REAL NOT NULL DEFAULT 0 CHECK (interestRate >= 0),
                        owner TEXT NOT NULL REFERENCES User(id)
                    )
                    """;

    public static final String CREATE_DEPOSIT_TRANSACTION = """
                    CREATE TABLE IF NOT EXISTS DepositTransaction (
                        id TEXT PRIMARY KEY,
                        userId TEXT NOT NULL REFERENCES User(id),
                        amount REAL NOT NULL CHECK (amount > 0),
                        date TEXT NOT NULL,
                        accountId TEXT NOT NULL
                    )
                    """;

    public static final String CREATE_WITHDRAWAL_TRANSACTION = """
                    CREATE TABLE IF NOT EXISTS WithdrawalTransaction (
                        id TEXT PRIMARY KEY,
                        userId TEXT NOT NULL REFERENCES User(id),
                        amount REAL NOT NULL CHECK (amount > 0),
                        date TEXT NOT NULL,
                        accountId TEXT NOT NULL
                    )
                    """;

    public static final String CREATE_TRANSFER_TRANSACTION = """
                    CREATE TABLE IF NOT EXISTS TransferTransaction (
                        id TEXT PRIMARY KEY,
                        userId TEXT NOT NULL REFERENCES User(id),
                        fromAccount TEXT NOT NULL,
                        toAccount TEXT NOT NULL,
                        amount REAL NOT NULL CHECK (amount > 0),
                        date TEXT NOT NULL
                    )
                    """;

    public static final String DELETE_USER = """
                    DROP TABLE IF EXISTS User
                    """;
    public static final String DELETE_CHECKING_ACCOUNT = """
                    DROP TABLE IF EXISTS Checking_Account
                    """;
    public static final String DELETE_SAVING_ACCOUNT = """
                    DROP TABLE IF EXISTS SavingAccount
                    """;
    public static final String DELETE_DEPOSIT_TRANSACTION = """
                    DROP TABLE IF EXISTS DepositTransaction
                    """;
    public static final String DELETE_WITHDRAWAL_TRANSACTION = """
                    DROP TABLE IF EXISTS WithdrawalTransaction
                    """;
    public static final String DELETE_TRANSFER_TRANSACTION = """
                    DROP TABLE IF EXISTS TransferTransaction
                    """;

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

        return DriverManager.getConnection(databaseUrl);
    }

    public static void initialize() throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
//            statement.executeUpdate(DELETE_TRANSFER_TRANSACTION);
//            statement.executeUpdate(DELETE_DEPOSIT_TRANSACTION);
//            statement.executeUpdate(DELETE_WITHDRAWAL_TRANSACTION);
//            statement.executeUpdate(DELETE_CHECKING_ACCOUNT);
//            statement.executeUpdate(DELETE_SAVING_ACCOUNT);
//            statement.executeUpdate(DELETE_USER);

            statement.executeUpdate(CREATE_USER);
            statement.executeUpdate(CREATE_CHECKING_ACCOUNT);
            statement.executeUpdate(CREATE_SAVING_ACCOUNT);
            statement.executeUpdate(CREATE_DEPOSIT_TRANSACTION);
            statement.executeUpdate(CREATE_WITHDRAWAL_TRANSACTION);
            statement.executeUpdate(CREATE_TRANSFER_TRANSACTION);
        }
    }
}