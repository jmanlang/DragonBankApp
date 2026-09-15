package dragon;

import dragon.database.Database;
import dragon.repository.TransactionRepository;
import dragon.repository.UserRepository;
import dragon.service.AuthService;
import dragon.service.HistoryService;

import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        try {
            Database.initialize();

            UserRepository userRepository = new UserRepository();
            TransactionRepository transactionRepository = new TransactionRepository();

            AuthService authService = new AuthService(userRepository);
            HistoryService historyService = new HistoryService(transactionRepository);
            BankController app = new BankController(authService, historyService);
            app.init();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}