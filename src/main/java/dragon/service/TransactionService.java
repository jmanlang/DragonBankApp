package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.database.Database;
import dragon.entity.Account;
import dragon.entity.DepositTransaction;
import dragon.entity.WithdrawalTransaction;
import dragon.exception.AccountNotFoundException;
import dragon.exception.AuthenticationRequiredException;
import dragon.exception.InsufficientFundsException;
import dragon.exception.ServiceUnavailableException;
import dragon.exception.ValidationException;
import dragon.repository.AccountRepository;
import dragon.repository.DepositTransactionRepository;
import dragon.repository.WithdrawalTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final AccountRepository accountRepository;
    private final DepositTransactionRepository depositTransactionRepository;
    private final WithdrawalTransactionRepository withdrawalTransactionRepository;

    public TransactionService(AccountRepository accountRepository,
                              DepositTransactionRepository depositTransactionRepository,
                              WithdrawalTransactionRepository withdrawalTransactionRepository) {
        this.accountRepository = accountRepository;
        this.depositTransactionRepository = depositTransactionRepository;
        this.withdrawalTransactionRepository = withdrawalTransactionRepository;
    }

    public BigDecimal deposit(BigDecimal amount)
            throws AuthenticationRequiredException, ValidationException,
            AccountNotFoundException, ServiceUnavailableException {

        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();
        validateRequest(userId, amount);

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Account account = getAccountForUser(connection, userId, "deposit");
                UUID accountId = account.getId();
                BigDecimal newBalance = account.getBalance().add(amount);

                if (!accountRepository.updateBalance(connection, accountId, newBalance)) {
                    rollbackQuietly(connection, "deposit");
                    logger.error("Deposit could not update account {} after it was loaded.", accountId);
                    throw new AccountNotFoundException("Your bank account could not be found.");
                }

                depositTransactionRepository.save(connection, new DepositTransaction(userId, amount));
                connection.commit();

                logger.info("Deposit of {} completed successfully for account {}.", amount, accountId);
                return newBalance;
            } catch (SQLException e) {
                rollbackQuietly(connection, "deposit");
                logger.error("Deposit failed because of a database error for user {}.", userId, e);
                throw new ServiceUnavailableException("Deposit service is currently unavailable.", e);
            }
        } catch (SQLException e) {
            logger.error("Could not open or close a database connection for deposit.", e);
            throw new ServiceUnavailableException("Deposit service is currently unavailable.", e);
        }
    }


    public BigDecimal withdraw(BigDecimal amount)
            throws AuthenticationRequiredException, ValidationException,
            AccountNotFoundException, InsufficientFundsException, ServiceUnavailableException {

        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();
        validateRequest(userId, amount);

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Account account = getAccountForUser(connection, userId, "withdrawal");
                UUID accountId = account.getId();

                if (account.getBalance().compareTo(amount) < 0) {
                    rollbackQuietly(connection, "withdrawal");
                    logger.warn("Withdrawal of {} rejected for account {} because funds were insufficient.",
                            amount, accountId);
                    throw new InsufficientFundsException("Insufficient funds for this withdrawal.");
                }

                BigDecimal newBalance = account.getBalance().subtract(amount);

                if (!accountRepository.updateBalance(connection, accountId, newBalance)) {
                    rollbackQuietly(connection, "withdrawal");
                    logger.error("Withdrawal could not update account {} after it was loaded.", accountId);
                    throw new AccountNotFoundException("Your bank account could not be found.");
                }

                withdrawalTransactionRepository.save(connection, new WithdrawalTransaction(userId, amount));
                connection.commit();

                logger.info("Withdrawal of {} completed successfully for account {}.", amount, accountId);
                return newBalance;
            } catch (SQLException e) {
                rollbackQuietly(connection, "withdrawal");
                logger.error("Withdrawal failed because of a database error for user {}.", userId, e);
                throw new ServiceUnavailableException("Withdrawal service is currently unavailable.", e);
            }
        } catch (SQLException e) {
            logger.error("Could not open or close a database connection for withdrawal.", e);
            throw new ServiceUnavailableException("Withdrawal service is currently unavailable.", e);
        }
    }

    private void validateRequest(UUID userId, BigDecimal amount)
            throws AuthenticationRequiredException, ValidationException {

        if (userId == null) {
            logger.error("Transaction rejected because no user is authenticated.");
            throw new AuthenticationRequiredException("Please log in before making a transaction.");
        }

        if (amount == null) {
            logger.warn("Transaction rejected because no amount was supplied for user {}.", userId);
            throw new ValidationException("A transaction amount is required.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Transaction amount {} rejected for user {} because it was not positive.",
                    amount, userId);
            throw new ValidationException("Transaction amount must be greater than zero.");
        }

        if (amount.stripTrailingZeros().scale() > 2) {
            logger.warn("Transaction amount {} rejected for user {} because it has more than two decimal places.",
                    amount, userId);
            throw new ValidationException("Transaction amount cannot have more than two decimal places.");
        }
    }

    private Account getAccountForUser(Connection connection, UUID userId, String operation)
            throws SQLException, AccountNotFoundException {

        Account account = accountRepository.findByOwnerId(connection, userId);

        if (account == null) {
            rollbackQuietly(connection, operation);
            logger.error("{} rejected because no bank account was found for user {}.", operation, userId);
            throw new AccountNotFoundException("No bank account was found for your user.");
        }

        return account;
    }

    private void rollbackQuietly(Connection connection, String operation) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            // Never swallow rollback failures: log them with their full stack trace.
            logger.error("Could not roll back the {} transaction.", operation, rollbackError);
        }
    }
}
