package dragon;

import dragon.repository.AccountRepository;
import dragon.repository.UserRepository;
import dragon.service.AuthService;

public class App {
    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        AuthService authService = new AuthService(userRepository);
        AccountRepository accountRepository = new AccountRepository();
        BankController app = new BankController(authService, accountRepository);
        app.init();
    }
}
