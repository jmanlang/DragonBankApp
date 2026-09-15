package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.UUID;



public class HistoryService {

    private final static  Logger logger = LoggerFactory.getLogger(HistoryService.class);
    private final TransactionRepository transactionRepository;

    public HistoryService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public boolean getAllHistory(){
        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();
        try{
            this.transactionRepository.queryAllTransactions(userId);
            //call repo toy get all transactions  a given account
            logger.info("Printed all transactions for account ID: {}", userId);
            return true;
        }
        catch(Exception e){
            //any exception except SQl, that's caught in repo layer.
            logger.error("Failed to print all transactions for account ID: {}, Message: {}", userId, e.toString());
            return false;
        }

    }


    public boolean getRangeHistory(Instant startInstant, Instant endInstant){
        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();
        try{
            if(startInstant.isAfter(endInstant)){
                throw new IllegalArgumentException("Starting Date must be before End Date.");
            }
            this.transactionRepository.queryRangeTransactions(userId, startInstant, endInstant);
            //call repo toy get all transactions for a date range for a given account
            logger.info("Printed transactions from {} to {} for account ID: {}", startInstant, endInstant, userId);
            return true;
        }
        catch(IllegalArgumentException e){
            logger.error("Failed to print transactions from {} to {} for account ID: {}, because date 1 is after date 2: {}", startInstant, endInstant, userId, e.toString());
            return false;
        }
        catch(Exception e){
            //any exception except SQl, that's caught in repo layer.
            logger.error("Failed to print transactions from {} to {} for account ID: {}, Message: {}", startInstant, endInstant, userId, e.toString());
            return false;
        }
    }
}
