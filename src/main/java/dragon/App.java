package dragon;

import dragon.repository.CheckingAccountRepository;
import dragon.repository.SavingAccountRepository;
import dragon.repository.UserRepository;
import dragon.service.AuthService;
import dragon.service.BalanceService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");

        UserRepository userRepository = new UserRepository();
        AuthService authService = new AuthService(userRepository);

        CheckingAccountRepository checkingAccountRepository = new CheckingAccountRepository();
        SavingAccountRepository savingAccountRepository = new SavingAccountRepository();
        BalanceService balanceService = new BalanceService(connection, checkingAccountRepository, savingAccountRepository);

        BankController app = new BankController(authService, balanceService);
        app.init();
    }
}
