package dragon.service;

import dragon.entity.CheckingAccount;
import dragon.entity.SavingAccount;
import dragon.repository.CheckingAccountRepository;
import dragon.repository.SavingAccountRepository;
import dragon.AuthenticatedAccountContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.UUID;
import java.sql.Connection;
import java.sql.SQLException;

public class BalanceService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceService.class);
    private final CheckingAccountRepository checkingAccountRepository;
    private final SavingAccountRepository savingAccountRepository;
    private Connection connection;

    public BalanceService(Connection connection, CheckingAccountRepository checkingAccountRepository, SavingAccountRepository savingAccountRepository) {
        this.connection = connection;
        this.checkingAccountRepository = checkingAccountRepository;
        this.savingAccountRepository = savingAccountRepository;
    }

    public Double getCheckingAccountBalance() throws SQLException {
        UUID owner = AuthenticatedAccountContext.getAuthenticatedUserId();

        try {
            CheckingAccount checkingAccount = checkingAccountRepository.findByOwnerID(connection, owner);
            if (checkingAccount == null) {
                logger.info("No checking account found for user {}", owner);
                return null;
        }
        logger.info("Checking balance retrieved successfully for user {}", owner);
        return checkingAccount.getBalance();
        } catch (SQLException e) {
            logger.error("SQLException caught while retrieving balance for user {}", owner, e);
            throw e;
        }
    }

    public Double getSavingAccountBalance() throws SQLException {
        UUID owner = AuthenticatedAccountContext.getAuthenticatedUserId();
        try {
            SavingAccount savingAccount = savingAccountRepository.findByOwnerID(connection, owner);
            if (savingAccount == null) {
                logger.info("No saving account found for user {}", owner);
                return null;
            }
            logger.info("Saving balance retrieved successfully for user {}", owner);
            return savingAccount.getBalance();
        } catch (SQLException e) {
            logger.error("SQLException caught while retrieving balance for user {}", owner, e);
            throw e;
        }
    }
}
