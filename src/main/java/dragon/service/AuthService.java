package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.database.Database;
import dragon.entity.Account;
import dragon.entity.User;
import dragon.exception.AccountAlreadyExistsException;
import dragon.exception.InvalidCredentialsException;
import dragon.exception.ServiceUnavailableException;
import dragon.exception.ValidationException;
import dragon.repository.AccountRepository;
import dragon.repository.UserRepository;
import dragon.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    public void register(String accountId, String pin)
            throws ValidationException, AccountAlreadyExistsException, ServiceUnavailableException {

        String normalizedAccountId = normalizeAccountId(accountId);
        validateRegistrationInput(normalizedAccountId, pin);

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                if (userRepository.findByAccountId(connection, normalizedAccountId) != null) {
                    connection.rollback();
                    logger.error("Registration rejected because account ID {} already exists.", normalizedAccountId);
                    throw new AccountAlreadyExistsException("That account ID is already registered.");
                }

                User user = new User(normalizedAccountId, PasswordHasher.hash(pin));
                userRepository.save(connection, user);

                // The default $0 checking account is supporting setup so that
                // this branch's deposit/withdrawal features are usable immediately.
                Account checkingAccount = new Account(UUID.randomUUID(), user.getId(), BigDecimal.ZERO);
                accountRepository.createCheckingAccount(connection, checkingAccount);

                connection.commit();
                logger.info("User successfully registered with account ID {}.", normalizedAccountId);
            } catch (AccountAlreadyExistsException e) {
                throw e;
            } catch (SQLException | IllegalStateException e) {
                rollbackQuietly(connection, "registration");
                logger.error("Registration failed because of a technical error for account ID {}.",
                        normalizedAccountId, e);
                throw new ServiceUnavailableException("Registration service is currently unavailable.", e);
            }
        } catch (SQLException e) {
            logger.error("Could not open or close a database connection for registration.", e);
            throw new ServiceUnavailableException("Registration service is currently unavailable.", e);
        }
    }

    public void login(String accountId, String pin)
            throws InvalidCredentialsException, ServiceUnavailableException {

        AuthenticatedAccountContext.setAuthenticatedUserId(null);
        String normalizedAccountId = normalizeAccountId(accountId);

        if (normalizedAccountId == null || normalizedAccountId.isBlank() || pin == null || pin.isBlank()) {
            logger.error("Login rejected because the Account ID or PIN was missing.");
            throw new InvalidCredentialsException("Incorrect Account ID or PIN.");
        }

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                User user = userRepository.findByAccountId(connection, normalizedAccountId);

                if (user == null || !PasswordHasher.verify(pin, user.getHashedPassword())) {
                    connection.rollback();
                    logger.error("Incorrect Account ID or PIN entered for account ID {}.", normalizedAccountId);
                    throw new InvalidCredentialsException("Incorrect Account ID or PIN.");
                }

                connection.commit();
                AuthenticatedAccountContext.setAuthenticatedUserId(user.getId());
                logger.info("User successfully logged in with account ID {}.", normalizedAccountId);
            } catch (InvalidCredentialsException e) {
                throw e;
            } catch (SQLException | IllegalStateException e) {
                rollbackQuietly(connection, "login");
                logger.error("Login failed because of a technical error for account ID {}.",
                        normalizedAccountId, e);
                throw new ServiceUnavailableException("Login service is currently unavailable.", e);
            }
        } catch (SQLException e) {
            logger.error("Could not open or close a database connection for login.", e);
            throw new ServiceUnavailableException("Login service is currently unavailable.", e);
        }
    }

    private String normalizeAccountId(String accountId) {
        return accountId == null ? null : accountId.trim();
    }

    private void validateRegistrationInput(String accountId, String pin) throws ValidationException {
        if (accountId == null || accountId.isBlank()) {
            logger.warn("Registration rejected because the Account ID was missing.");
            throw new ValidationException("Account ID is required.");
        }

        if (pin == null || pin.isBlank()) {
            logger.warn("Registration rejected because the PIN was missing for account ID {}.", accountId);
            throw new ValidationException("PIN is required.");
        }
    }

    private void rollbackQuietly(Connection connection, String operation) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            logger.error("Could not roll back the {} transaction.", operation, rollbackError);
        }
    }
}
