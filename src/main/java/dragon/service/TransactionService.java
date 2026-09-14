package dragon.service;

import dragon.entity.Account;
import dragon.repository.AccountRepository;

import java.util.UUID;

public class TransactionService {
    private final UUID userId;
    private final AccountRepository accountRepository;

    public TransactionService(UUID autheticatedUserId, AccountRepository accountRepository) {
        this.userId = autheticatedUserId;
        this.accountRepository = accountRepository;
    }

    public boolean deposit(String accountId, float amount) {
        Account account = accountRepository.findByAccountId(accountId);
        if (account == null || !accountRepository.checkOwnership(account, userId.toString())) {
            return false;
        }
        return accountRepository.deposit(account, amount);
    }

    public boolean withdraw(String accountId, float amount) {
        Account account = accountRepository.findByAccountId(accountId);
        if (account == null || !accountRepository.checkOwnership(account, userId.toString())) {
            return false;
        }
        return accountRepository.withdraw(account, amount);
    }

    public boolean transfer(String fromAccountId, String toAccountId, float amount) {
        Account fromAccount = accountRepository.findByAccountId(fromAccountId);
        Account toAccount = accountRepository.findByAccountId(toAccountId);
        if (fromAccount == null || toAccount == null
                || !accountRepository.checkOwnership(fromAccount, userId.toString())) {
            return false;
        }
        if (!accountRepository.withdraw(fromAccount, amount)) {
            return false;
        }
        return accountRepository.deposit(toAccount, amount);
    }
}
