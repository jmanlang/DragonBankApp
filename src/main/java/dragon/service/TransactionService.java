package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.database.Database;
import dragon.entity.Account;
import dragon.entity.DepositTransaction;
import dragon.entity.WithdrawalTransaction;
import dragon.repository.AccountRepository;
import dragon.repository.DepositTransactionRepository;
import dragon.repository.WithdrawalTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public boolean deposit(double amount) {
        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();

        if (!isValidRequest(userId, amount)) {
            return false;
        }

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Account account = accountRepository.findByOwnerId(connection, userId);
                if (account == null) {
                    connection.rollback();
                    logger.error("Deposit failed because no account was found for user {}.", userId);
                    return false;
                }

                double newBalance = account.getBalance() + amount;
                if (!accountRepository.updateBalance(connection, account.getId(), newBalance)) {
                    connection.rollback();
                    logger.error("Deposit failed because account {} could not be updated.", account.getId());
                    return false;
                }

                depositTransactionRepository.save(connection, new DepositTransaction(userId, amount));
                connection.commit();
                logger.info("Deposit of {} completed for user {}.", amount, userId);
                return true;
            } catch (SQLException e) {
                connection.rollback();
                logger.error("Deposit failed for user {}.", userId, e);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Deposit could not connect to the database for user {}.", userId, e);
            return false;
        }
    }

    public boolean withdraw(double amount) {
        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();

        if (!isValidRequest(userId, amount)) {
            return false;
        }

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Account account = accountRepository.findByOwnerId(connection, userId);
                if (account == null) {
                    connection.rollback();
                    logger.error("Withdrawal failed because no account was found for user {}.", userId);
                    return false;
                }

                if (account.getBalance() < amount) {
                    connection.rollback();
                    logger.error("Withdrawal failed because user {} has insufficient funds.", userId);
                    return false;
                }

                double newBalance = account.getBalance() - amount;
                if (!accountRepository.updateBalance(connection, account.getId(), newBalance)) {
                    connection.rollback();
                    logger.error("Withdrawal failed because account {} could not be updated.", account.getId());
                    return false;
                }

                withdrawalTransactionRepository.save(connection, new WithdrawalTransaction(userId, amount));
                connection.commit();
                logger.info("Withdrawal of {} completed for user {}.", amount, userId);
                return true;
            } catch (SQLException e) {
                connection.rollback();
                logger.error("Withdrawal failed for user {}.", userId, e);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Withdrawal could not connect to the database for user {}.", userId, e);
            return false;
        }
    }

    private boolean isValidRequest(UUID userId, double amount) {
        if (userId == null) {
            logger.error("Transaction failed because no user is logged in.");
            return false;
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            logger.error("Transaction failed because the amount must be greater than zero.");
            return false;
        }

        return true;
    }
}
