package dragon;

import dragon.repository.UserRepository;
import dragon.service.AuthService;

public class App {
    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        AuthService authService = new AuthService(userRepository);
        BankController app = new BankController(authService);
        app.init();
    }
}
