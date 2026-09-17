package dragon.repository;

import dragon.entity.DepositTransaction;
import dragon.entity.HasDate;
import dragon.entity.TransferTransaction;
import dragon.entity.WithdrawalTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TransactionRepository {
    public List<HasDate> queryAllTransactions(UUID accountId, Connection connection) throws SQLException{

        List<HasDate> transactions = new ArrayList<>();

        String sqlDeposit = "SELECT * FROM DepositTransaction " +
                "WHERE accountId = ? " +
                "ORDER BY date";
        try(PreparedStatement statementDeposit = connection.prepareStatement(sqlDeposit)){
            statementDeposit.setString(1, String.valueOf(accountId));
            ResultSet rsDeposit = statementDeposit.executeQuery();
            while(rsDeposit.next()){
                DepositTransaction depositTransaction = new DepositTransaction(
                        UUID.fromString(rsDeposit.getString(1)),
                        UUID.fromString(rsDeposit.getString(2)),
                        rsDeposit.getDouble(3),
                        Instant.parse(rsDeposit.getString(4)),
                        UUID.fromString(rsDeposit.getString(5)));
                transactions.add(depositTransaction);
            }
        }


        String sqlWithdrawal = "SELECT * FROM WithdrawalTransaction " +
                "WHERE accountId = ? " +
                "ORDER BY DATE";
        try(PreparedStatement statementWithdrawal  = connection.prepareStatement(sqlWithdrawal )){
            statementWithdrawal .setString(1, String.valueOf(accountId));
            ResultSet rsWithdrawal = statementWithdrawal .executeQuery();
            while(rsWithdrawal.next()){
                WithdrawalTransaction withdrawalTransaction = new WithdrawalTransaction(
                        UUID.fromString(rsWithdrawal.getString(1)),
                        UUID.fromString(rsWithdrawal.getString(2)),
                        rsWithdrawal.getDouble(3),
                        Instant.parse(rsWithdrawal.getString(4)),
                        UUID.fromString(rsWithdrawal.getString(5)));
                transactions.add(withdrawalTransaction);
            }
        }

        String sqlTransfer = "SELECT * FROM TransferTransaction " +
                "WHERE fromAccount = ? OR toAccount = ?" +
                "ORDER BY date";
        try(PreparedStatement statementTransfer = connection.prepareStatement(sqlTransfer)){
            statementTransfer.setString(1, String.valueOf(accountId));
            statementTransfer.setString(2, String.valueOf(accountId));
            ResultSet rsTransfer= statementTransfer.executeQuery();
            while(rsTransfer.next()){
                TransferTransaction transferTransaction = new TransferTransaction(
                        UUID.fromString(rsTransfer.getString(1)),
                        UUID.fromString(rsTransfer.getString(2)),
                        UUID.fromString(rsTransfer.getString(3)),
                        UUID.fromString(rsTransfer.getString(4)),
                                rsTransfer.getDouble(5),
                        Instant.parse(rsTransfer.getString(6)));
                transactions.add(transferTransaction);
            }
        }

        transactions.sort(Comparator.comparing(HasDate::getDate));

        return transactions;
    }

    public List<HasDate> queryRangeTransactions(UUID accountId, Connection connection,Instant startDate, Instant endDate) throws SQLException{
        List<HasDate> transactions = new ArrayList<>();

        String sqlDeposit = "SELECT * FROM DepositTransaction " +
                "WHERE userId = ? AND date >= ? AND date <= ?" +
                "ORDER BY date";
        try(PreparedStatement statementDeposit = connection.prepareStatement(sqlDeposit)){
            statementDeposit.setString(1, String.valueOf(accountId));
            statementDeposit.setString(2, String.valueOf(startDate));
            statementDeposit.setString(3, String.valueOf(endDate));
            ResultSet rsDeposit = statementDeposit.executeQuery();
            while(rsDeposit.next()){
                DepositTransaction depositTransaction = new DepositTransaction(
                        UUID.fromString(rsDeposit.getString(1)),
                        UUID.fromString(rsDeposit.getString(2)),
                        rsDeposit.getDouble(3),
                        Instant.parse(rsDeposit.getString(4)),
                        UUID.fromString(rsDeposit.getString(5)));
                transactions.add(depositTransaction);
            }
        }


        String sqlWithdrawal = "SELECT * FROM WithdrawalTransaction " +
                "WHERE userId = ?  AND date >= ? AND date <= ?" +
                "ORDER BY date";
        try(PreparedStatement statementWithdrawal  = connection.prepareStatement(sqlWithdrawal )){
            statementWithdrawal .setString(1, String.valueOf(accountId));
            statementWithdrawal.setString(2, String.valueOf(startDate));
            statementWithdrawal.setString(3, String.valueOf(endDate));
            ResultSet rsWithdrawal = statementWithdrawal .executeQuery();
            while(rsWithdrawal.next()){
                WithdrawalTransaction withdrawalTransaction = new WithdrawalTransaction(
                        UUID.fromString(rsWithdrawal.getString(1)),
                        UUID.fromString(rsWithdrawal.getString(2)),
                        rsWithdrawal.getDouble(3),
                        Instant.parse(rsWithdrawal.getString(4)),
                        UUID.fromString(rsWithdrawal.getString(5)));
                transactions.add(withdrawalTransaction);
            }
        }

        String sqlTransfer = "SELECT * FROM TransferTransaction " +
                "WHERE userId = ?  AND date >= ? AND date <= ?" +
                "ORDER BY date";
        try(PreparedStatement statementTransfer = connection.prepareStatement(sqlTransfer)){
            statementTransfer.setString(1, String.valueOf(accountId));
            statementTransfer.setString(2, String.valueOf(startDate));
            statementTransfer.setString(3, String.valueOf(endDate));
            ResultSet rsTransfer= statementTransfer.executeQuery();
            while(rsTransfer.next()){
                TransferTransaction transferTransaction = new TransferTransaction(
                        UUID.fromString(rsTransfer.getString(1)),
                        UUID.fromString(rsTransfer.getString(2)),
                        UUID.fromString(rsTransfer.getString(3)),
                        UUID.fromString(rsTransfer.getString(4)),
                        rsTransfer.getDouble(5),
                        Instant.parse(rsTransfer.getString(6)));
                transactions.add(transferTransaction);
            }
        }

        transactions.sort(Comparator.comparing(HasDate::getDate));

        return transactions;

    }


}
