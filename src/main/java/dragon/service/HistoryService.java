package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.Exceptions.NoAccountException;
import dragon.repository.HistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;



public class HistoryService {

    private final static  Logger logger = LoggerFactory.getLogger(HistoryService.class);
    private final HistoryRepository historyRepository;

    public HistoryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public boolean getAllHistory(){
        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();
//        if(userId == null){
//            logger.error("Failed to print all transactions because account does not exit");
//            return false;
//        }
        try{
            if(userId == null){
                throw new NoAccountException();
            }
            this.historyRepository.queryAllTransactions(userId);
            //call repo toy get all transactions  a given account
            logger.info("Printed all transactions for account ID: {}", userId);
            return true;
        }
        catch(NoAccountException e){
            logger.error("Failed to print all transactions because no one was logged in.");
            return false;
        }
        catch(Exception e){
            //any exception except SQl, that's caught in repo layer.
            logger.error("Failed to print all transactions for account ID: {}, Message: {}", userId, e.toString());
            return false;
        }

    }


    public boolean getRangeHistory(String startDate, String endDate){
        UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();
//        if(userId == null){
//            logger.error("Failed to print transactions in date range because account does not exit");
//            return false;
//        }

        try{
            //UUID userId = AuthenticatedAccountContext.getAuthenticatedUserId();
            if(userId == null){
                throw new NoAccountException();
            }
            if(startDate.compareTo(endDate) > 0 ){
                throw new IllegalArgumentException("Starting Date must be before End Date.");
            }
            this.historyRepository.queryRangeTransactions(userId, startDate, endDate);
            //call repo toy get all transactions for a date range for a given account
            logger.info("Printed transactions from dates {} to {} for account ID: {}", startDate, endDate, userId);
            return true;
        }
        catch(NoAccountException e){
            logger.error("Failed to print transactions because no one was logged in.");
            return false;
        }
        catch(IllegalArgumentException e){
            logger.error("Failed to print transactions from {} to {} for account ID: {}, because date 1 is after date 2: {}", startDate, endDate, userId, e.toString());
            return false;
        }
        catch(Exception e){
            //any exception except SQl, that's caught in repo layer.
            logger.error("Failed to print transactions from {} to {} for account ID: {}, Message: {}", startDate, endDate, userId, e.toString());
            return false;
        }
    }



}
