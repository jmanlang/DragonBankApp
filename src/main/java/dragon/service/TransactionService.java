package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.database.Database;
import dragon.entity.*;
import dragon.repository.CheckingAccountRepository;
import dragon.repository.SavingAccountRepository;
import dragon.repository.DepositTransactionRepository;
import dragon.repository.TransferTransactionRepository;
import dragon.repository.WithdrawalTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final CheckingAccountRepository checkingAccountRepository;
    private final SavingAccountRepository savingAccountRepository;
    private final DepositTransactionRepository depositTransactionRepository;
    private final WithdrawalTransactionRepository withdrawalTransactionRepository;
    private final TransferTransactionRepository transferTransactionRepository;

    public TransactionService(CheckingAccountRepository checkingAccountRepository,
                              SavingAccountRepository savingAccountRepository,
                              DepositTransactionRepository depositTransactionRepository,
                              WithdrawalTransactionRepository withdrawalTransactionRepository,
                              TransferTransactionRepository transferTransactionRepository) {
        this.checkingAccountRepository = checkingAccountRepository;
        this.savingAccountRepository = savingAccountRepository;
        this.depositTransactionRepository = depositTransactionRepository;
        this.withdrawalTransactionRepository = withdrawalTransactionRepository;
        this.transferTransactionRepository = transferTransactionRepository;
    }

    public boolean deposit(double amount, String accountType) {
        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();

        if (!isValidRequest(userId, amount)) {
            return false;
        }

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Account account = switch (accountType) {
                    case "1" -> checkingAccountRepository.findByOwnerID(connection, userId);
                    case "2" -> savingAccountRepository.findByOwnerID(connection, userId);
                    default -> null;
                };
                if (account == null) {
                    connection.rollback();
                    logger.error("Deposit failed because no checking account was found for user {}.", userId);
                    return false;
                }

                double newBalance = account.getBalance() + amount;
                switch(accountType){
                    case "1":
                        if (!checkingAccountRepository.updateCheckingBalance(connection, (CheckingAccount) account, newBalance)) {
                            connection.rollback();
                            logger.error("Deposit failed because checking account {} could not be updated.", account.getID());
                            return false;
                        }
                        break;
                    case "2":
                        if (!savingAccountRepository.updateSavingAccountBalance(connection, (SavingAccount) account, newBalance)) {
                            connection.rollback();
                            logger.error("Deposit failed because savings account {} could not be updated.", account.getID());
                            return false;
                        }
                        break;
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

    public boolean transfer(double amount, int direction) {
        // Direction variable -> 1 if checkings to savings, 2 if savings to checkings
        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();

        if (!isValidRequest(userId, amount)) {
            return false;
        }

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                CheckingAccount checkingAccount = checkingAccountRepository.findByOwnerID(connection, userId);
                SavingAccount savingAccount = savingAccountRepository.findByOwnerID(connection, userId);
                if (checkingAccount == null || savingAccount == null) {
                    connection.rollback();
                    logger.error("Transfer failed because no account was found for user {}.", userId);
                    return false;
                }

                UUID fromAccountId = direction == 1 ? checkingAccount.getID() : savingAccount.getID();
                UUID toAccountId = direction == 1 ? savingAccount.getID() : checkingAccount.getID();

                double fromBalance = direction == 1 ? checkingAccount.getBalance() : savingAccount.getBalance();
                if (fromBalance < amount) {
                    connection.rollback();
                    logger.error("Transfer failed because user {} has insufficient funds.", userId);
                    return false;
                }

                double newFromBalance = fromBalance - amount;
                double newToBalance = (direction == 1 ? savingAccount.getBalance() : checkingAccount.getBalance()) + amount;

                boolean fromUpdated = direction == 1
                        ? checkingAccountRepository.updateCheckingBalance(connection, checkingAccount, newFromBalance)
                        : savingAccountRepository.updateSavingAccountBalance(connection, savingAccount, newFromBalance);
                boolean toUpdated = direction == 1
                        ? savingAccountRepository.updateSavingAccountBalance(connection, savingAccount, newToBalance)
                        : checkingAccountRepository.updateCheckingBalance(connection, checkingAccount, newToBalance);

                if (!fromUpdated || !toUpdated) {
                    connection.rollback();
                    logger.error("Transfer failed because an account could not be updated for user {}.", userId);
                    return false;
                }

                transferTransactionRepository.save(connection, new TransferTransaction(
                        userId,
                        fromAccountId,
                        toAccountId,
                        amount));
                connection.commit();
                logger.info("Transfer of {} completed for user {}.", amount, userId);
                return true;
            } catch (SQLException e) {
                connection.rollback();
                logger.error("Transfer failed for user {}.", userId, e);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Transfer could not connect to the database for user {}.", userId, e);
            return false;
        }
    }

    public boolean withdraw(double amount, String accountType) {
        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();

        if (!isValidRequest(userId, amount)) {
            return false;
        }

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Account account = switch (accountType) {
                    case "1" -> checkingAccountRepository.findByOwnerID(connection, userId);
                    case "2" -> savingAccountRepository.findByOwnerID(connection, userId);
                    default -> null;
                };
                if (account == null) {
                    connection.rollback();
                    logger.error("Withdrawal failed because no checking account was found for user {}.", userId);
                    return false;
                }

                if (account.getBalance() < amount) {
                    connection.rollback();
                    logger.error("Withdrawal failed because user {} has insufficient funds.", userId);
                    return false;
                }

                double newBalance = account.getBalance() - amount;
                switch(accountType){
                    case "1":
                        if (!checkingAccountRepository.updateCheckingBalance(connection, (CheckingAccount) account, newBalance)) {
                            connection.rollback();
                            logger.error("Withdrawal failed because checking account {} could not be updated.", account.getID());
                            return false;
                        }
                        break;
                    case "2":
                        if (!savingAccountRepository.updateSavingAccountBalance(connection, (SavingAccount) account, newBalance)) {
                            connection.rollback();
                            logger.error("Withdrawal failed because savings account {} could not be updated.", account.getID());
                            return false;
                        }
                        break;
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
