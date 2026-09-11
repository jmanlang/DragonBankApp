package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.entity.User;
import dragon.repository.UserRepository;

import java.util.UUID;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean register(String accountId, String password) {
        if (!userRepository.existsByAccountId(accountId)) {
            // TODO: store a hashed password
            userRepository.save(new User(accountId, password));
            return true;
        }
        return false;
    }

    public boolean login(String accountId, String password) {
        if (userRepository.authenticate(accountId, password)) {
            UUID userId = userRepository.findByAccountId(accountId).getId();
            AuthenticatedAccountContext.setAuthenticatedUserId(userId);
            return true;
        }
        return false;
    }
}
