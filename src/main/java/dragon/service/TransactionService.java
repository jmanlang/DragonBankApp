package dragon.service;

import dragon.Exceptions.InsufficientFundsException;
import dragon.Exceptions.InvalidAmountException;
import dragon.Exceptions.NoBankAccountException;
import dragon.entity.Account;
import dragon.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class TransactionService {
    private final UUID userId;
    private final AccountRepository accountRepository;
    private final static Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(UUID autheticatedUserId, AccountRepository accountRepository) {
        this.userId = autheticatedUserId;
        this.accountRepository = accountRepository;
    }

    public void validateAmount(float amount) throws InvalidAmountException {
        if (amount < 0) {
            throw new InvalidAmountException();
        }
    }

    public void validateBankAccount(Account account) throws NoBankAccountException {
        if (account == null) {
            throw new NoBankAccountException(null);
        }
        if (!accountRepository.checkOwnership(account, userId)) {
            throw new NoBankAccountException(account.getBankAccountId());
        }
    }

    public void checkFunds(float availableBalance, float withdrawAmount) throws InsufficientFundsException {
        if (availableBalance < withdrawAmount) {
            throw new InsufficientFundsException(availableBalance, withdrawAmount);
        }
    }


    public boolean deposit(String accountId, float amount) {
        try {

            validateAmount(amount);
            Account account = accountRepository.findByAccountId(accountId);
            validateBankAccount(account);

            float balance = account.getBalance();
            return accountRepository.deposit(account, amount);

        } catch (InvalidAmountException e) {
            logger.error("Deposit Failed: User entered invalid deposit amount: " + amount);
            return false;
        } catch (NoBankAccountException e) {
            logger.error("Deposit Failed: User does not own bank account with accountId " + accountId);
            return false;
        } catch (Exception e) {
            logger.error("Deposit Failed: Failed to deposit $" + amount + " to bank account with accountId " + accountId);
            return false;
        }
    }

    public boolean withdraw(String accountId, float amount) {
        try {
            validateAmount(amount);
            Account account = accountRepository.findByAccountId(accountId);
            validateBankAccount(account);
            float balance = account.getBalance();
            checkFunds(balance, amount);

            return accountRepository.withdraw(account, amount);
        } catch (InvalidAmountException e) {
            logger.error("Withdraw Failed: User entered invalid withdraw amount: " + amount);
            return false;
        } catch (NoBankAccountException e) {
            logger.error("Withdraw Failed: User does not own bank account with accountId " + accountId);
            return false;
        } catch (InsufficientFundsException e) {
            Account account = accountRepository.findByAccountId(accountId);
            float availableBalance = account.getBalance();
            logger.error("Withdraw Failed: User tried to withdraw $ " + amount + " from account " + account.getBankAccountId() + " with balance $ " + availableBalance);
            return false;
        } catch (Exception e) {
            logger.error("Withdraw Failed: Failed to withdraw $" + amount + " from bank account with accountId " + accountId);
            return false;
        }


    }

    public boolean transfer(String fromAccountId, String toAccountId, float amount) {
        try {
            validateAmount(amount);
            Account fromAccount = accountRepository.findByAccountId(fromAccountId);
            validateBankAccount(fromAccount);
            Account toAccount = accountRepository.findByAccountId(toAccountId);
            validateBankAccount(toAccount);

            float fromAccountBalance = fromAccount.getBalance();
            checkFunds(fromAccountBalance, amount);
            boolean withdrawProcess = accountRepository.withdraw(fromAccount, amount);
            return accountRepository.deposit(toAccount, amount);


        } catch (InvalidAmountException e) {
            logger.error("Transfer Failed: User entered invalid withdraw amount: " + amount);
            return false;
        } catch (NoBankAccountException e) {
            logger.error("Transfer Failed: User does not own one of the specified accounts");
            return false;
        } catch (InsufficientFundsException e) {
            Account account = accountRepository.findByAccountId(fromAccountId);
            float availableBalance = account.getBalance();
            logger.error("Transfer Failed: User tried to transfer $ " + amount + " from account " + account.getBankAccountId() + " with balance $" + availableBalance);
            return false;
        } catch (Exception e) {
            logger.error("Transfer Failed: Failed to transfer $ " + amount + " from account " + fromAccountId + " to " + toAccountId);
            return false;
        }
    }
}
