package dragon.repository;

import java.util.UUID;

public class HistoryRepository {
    public boolean queryAllTransactions(UUID accountNum){
        //make sql query for all transactions for an account
        /* Select id, accountid, amount, date FROM transactions JOIN transferTransaction
        on accountID = accountID
        WHERE accountid == accountnum
        */
        return true;
    }

    public boolean queryRangeTransactions(UUID accountNum, String startDate, String endDate){
        //make sql query for all transactions for an account within the given date range
        //make sql query for all transactions for an account
        /* Select id, accountid, amount, date FROM transactions JOIN transferTransaction
        on accountID = accountID
        WHERE accountid == accountnum AND date >= startDate AND date <= endDate
        */
        return true;
    }


}
