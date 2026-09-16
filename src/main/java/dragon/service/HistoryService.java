package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.Exceptions.NoTransactionsError;
import dragon.entity.HasDate;
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
    private final Connection connection;

    public HistoryService(TransactionRepository transactionRepository, Connection connection) {
        this.transactionRepository = transactionRepository;
        this.connection = connection;
    }


    public List<HasDate> getAllHistory() {
        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();
        try{
            List<HasDate> transactions = this.transactionRepository.queryAllTransactions(userId, connection);
            if(transactions.isEmpty()){
                throw new NoTransactionsError(String.valueOf(userId));
            }
            logger.info("Printed all transactions for account ID: {}", userId);
            return transactions;
        }
        catch (SQLException e) {
            logger.error("SQL Error: failed to get all transactions for userId {}", userId);
            return null;
        }
        catch(NoTransactionsError e){
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
        try{
            if(startInstant.isAfter(endInstant)){
                throw new IllegalArgumentException("Starting Date must be before End Date.");
            }
            List<HasDate> transactions = this.transactionRepository.queryRangeTransactions(userId, connection, startInstant, endInstant);
            if(transactions.isEmpty()){
                throw new NoTransactionsError(String.valueOf(userId));
            }
            logger.info("Printed transactions from {} to {} for account ID: {}", startInstant, endInstant, userId);
            return transactions;
        }catch(SQLException e){
            logger.error("SQL Error: failed to get all transactions for ID {}", userId);
            return null;
        }
        catch(IllegalArgumentException e){
            logger.error("Failed to print transactions from {} to {} for account ID: {}, because date 1 is after date 2: {}", startInstant, endInstant, userId, e.toString());
            return null;
        }
        catch(Exception e){
            logger.error("Failed to print transactions from {} to {} for account ID: {}, Message: {}", startInstant, endInstant, userId, e.toString());
            return null;
        }
    }
}
