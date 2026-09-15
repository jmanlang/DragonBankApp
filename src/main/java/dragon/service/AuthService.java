package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.entity.User;
import dragon.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.HexFormat;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean register(String accountId, String password) {
        if (accountId == null || accountId.isBlank() || password == null || password.isBlank()) {
            logger.error("Registration failed because an account ID or password was missing.");
            return false;
        }

        String normalizedAccountId = accountId.trim();

        try {
            if (userRepository.existsByAccountId(normalizedAccountId)) {
                logger.error("Registration failed because account ID {} already exists.", normalizedAccountId);
                return false;
            }

            userRepository.save(new User(normalizedAccountId, hashPassword(password)));
            logger.info("User successfully registered with account ID {}.", normalizedAccountId);
            return true;
        } catch (RuntimeException e) {
            logger.error("Registration failed for account ID {}.", normalizedAccountId, e);
            return false;
        }
    }

    public boolean login(String accountId, String password) {
        AuthenticatedAccountContext.setAuthenticatedUserId(null);

        if (accountId == null || accountId.isBlank() || password == null || password.isBlank()) {
            logger.error("Login failed because an account ID or password was missing.");
            return false;
        }

        String normalizedAccountId = accountId.trim();

        try {
            if (userRepository.authenticate(normalizedAccountId, hashPassword(password))) {
                UUID userId = userRepository.findByAccountId(normalizedAccountId).getId();
                AuthenticatedAccountContext.setAuthenticatedUserId(userId);
                logger.info("User successfully logged in with account ID {}.", normalizedAccountId);
                return true;
            }
        } catch (RuntimeException e) {
            logger.error("Login failed for account ID {}.", normalizedAccountId, e);
            return false;
        }

        logger.error("Login failed because the account ID or password was incorrect.");
        return false;
    }

    private String hashPassword(String password) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("MD5").digest(password.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 hashing is unavailable.", e);
        }
    }
}
