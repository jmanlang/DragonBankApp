package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.Exceptions.NoTransactionsException;
import dragon.database.Database;
import dragon.entity.HasDate;
import dragon.repository.CheckingAccountRepository;
import dragon.repository.SavingAccountRepository;
import dragon.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


public class HistoryService {

    private final static Logger logger = LoggerFactory.getLogger(HistoryService.class);
    private final TransactionRepository transactionRepository;
    private final CheckingAccountRepository checkingAccountRepository;
    private final SavingAccountRepository savingAccountRepository;

    public HistoryService(TransactionRepository transactionRepository, CheckingAccountRepository checkingAccountRepository, SavingAccountRepository savingAccountRepository) {
        this.transactionRepository = transactionRepository;
        this.checkingAccountRepository = checkingAccountRepository;
        this.savingAccountRepository = savingAccountRepository;
    }


    public List<HasDate> getAllHistory(String accountType) {
        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();

        try(Connection connection = Database.getConnection()){
            UUID accountId = switch (accountType) {
                case "1" -> checkingAccountRepository.findByOwnerID(connection, userId).getID();
                case "2" -> savingAccountRepository.findByOwnerID(connection, userId).getID();
                default -> null;
            };
            if(accountId == null){
                logger.error("No account was found for {} while searching for transaction history", userId);
            }
            List<HasDate> transactions = this.transactionRepository
                    .queryAllTransactions(accountId, connection);
            if(transactions.isEmpty()){
                throw new NoTransactionsException(String.valueOf(userId));
            }
            logger.info("Printed all transactions for account ID: {}", userId);
            return transactions;
        }
        catch (SQLException e) {
            logger.error("SQL Error: failed to get all transactions for userId: {}, Message: {}", userId, e.toString());
            return null;
        }
        catch(NoTransactionsException e){
            logger.error("UserId {} has no relevant transactions, no history was printed.", userId);
            return null;
        }
        catch(Exception e){
            logger.error("Unexpected Exception, failed to print all transactions for account ID: {}, Message: {}", userId, e.toString());
            return null;
        }
    }


    public List<HasDate> getRangeHistory(Instant startInstant, Instant endInstant){
        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();
        try(Connection connection = Database.getConnection()){
            if(startInstant.isAfter(endInstant)){
                throw new IllegalArgumentException("Starting Date must be before End Date.");
            }
            List<HasDate> transactions = this.transactionRepository.queryRangeTransactions(userId, connection, startInstant, endInstant);
            if(transactions.isEmpty()){
                throw new NoTransactionsException(String.valueOf(userId));
            }
            logger.info("Printed transactions from {} to {} for account ID: {}", startInstant, endInstant, userId);
            return transactions;
        }catch(SQLException e){
            logger.error("SQL Error: failed to get all transactions from {} to {} for ID {}", userId, startInstant, endInstant);
            return null;
        }
        catch(IllegalArgumentException e){
            logger.error("Invalid date range: failed to print transactions from {} to {} for account ID: {}", startInstant, endInstant, userId);
            return null;
        }catch(NoTransactionsException e){
            logger.error("UserId {} has no relevant transactions in given date range {} to {}, no history was printed.", userId, startInstant, endInstant);
            return null;
        }
        catch(Exception e){
            logger.error("Unexpected Exception: Failed to print transactions from {} to {} for account ID: {}, Message: {}", startInstant, endInstant, userId, e.toString());
            return null;
        }
    }
}
