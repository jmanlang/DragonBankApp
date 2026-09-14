package dragon;

import dragon.repository.HistoryRepository;
import dragon.repository.UserRepository;
import dragon.service.AuthService;
import dragon.service.HistoryService;

public class App {
    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        HistoryRepository historyRepository = new HistoryRepository();

        AuthService authService = new AuthService(userRepository);
        HistoryService historyService = new HistoryService(historyRepository);
        BankController app = new BankController(authService, historyService);
        app.init();
    }
}