package dragon.repository;

import dragon.entity.Transaction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionRepository {
    public List<Transaction> queryAllTransactions(UUID accountNum){
        //make sql query for all transactions for an account
        /* Select id, accountid, amount, date FROM transactions JOIN transferTransaction
        on accountID = accountID
        WHERE accountid == accountnum
        */
        List<Transaction> transactionList = new ArrayList<>();
        return transactionList;
    }

    public List<Transaction> queryRangeTransactions(UUID accountNum, Instant startDate, Instant endDate){
        //make sql query for all transactions for an account within the given date range
        //make sql query for all transactions for an account
        /* Select id, accountid, amount, date FROM transactions JOIN transferTransaction
        on accountID = accountID
        WHERE accountid == accountnum AND date >= startDate AND date <= endDate
        */
        List<Transaction> transactionList = new ArrayList<>();
        return transactionList;
    }


}
