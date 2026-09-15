package dragon.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
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

        return DriverManager.getConnection(databaseUrl);
    }

    public static void initialize() throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS User (
                        id TEXT PRIMARY KEY,
                        accountId TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS CheckingAccount (
                        id TEXT PRIMARY KEY,
                        balance REAL NOT NULL DEFAULT 0 CHECK (balance >= 0),
                        owner TEXT NOT NULL REFERENCES User(id)
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS SavingAccount (
                        id TEXT PRIMARY KEY,
                        balance REAL NOT NULL DEFAULT 0 CHECK (balance >= 0),
                        interestRate REAL NOT NULL DEFAULT 0 CHECK (interestRate >= 0),
                        owner TEXT NOT NULL REFERENCES User(id)
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS DepositTransaction (
                        id TEXT PRIMARY KEY,
                        userId TEXT NOT NULL REFERENCES User(id),
                        amount REAL NOT NULL CHECK (amount > 0),
                        date TEXT NOT NULL
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS WithdrawalTransaction (
                        id TEXT PRIMARY KEY,
                        userId TEXT NOT NULL REFERENCES User(id),
                        amount REAL NOT NULL CHECK (amount > 0),
                        date TEXT NOT NULL
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS TransferTransaction (
                        id TEXT PRIMARY KEY,
                        userId TEXT NOT NULL REFERENCES User(id),
                        amount REAL NOT NULL CHECK (amount > 0),
                        date TEXT NOT NULL
                    )
                    """);
        }
    }
}
