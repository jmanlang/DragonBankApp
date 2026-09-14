package dragon.service;

import dragon.AuthenticatedAccountContext;
import dragon.entity.Account;
import dragon.Exceptions.InsufficientFundsException;
import dragon.Exceptions.InvalidAmountException;
import dragon.Exceptions.NoBankAccountException;
import dragon.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionServiceTest {
    TransactionService transactionService;
    @BeforeEach
    void setUp() {
        AccountRepository accountRepository = new AccountRepository();
        AuthenticatedAccountContext.setAuthenticatedUserId(UUID.randomUUID());
        UUID userUUID = AuthenticatedAccountContext.getAuthenticatedUserId();
        transactionService = new TransactionService(userUUID, accountRepository);
    }

    @Test
    void validateBankAccountPositive() {
        UUID userUUID = AuthenticatedAccountContext.getAuthenticatedUserId();
        Account a = new Account("1", userUUID);
        assertDoesNotThrow(() -> {
            transactionService.validateBankAccount(a);

        });
    }

    @Test
    void validateBankAccountNegative() {
        UUID random = UUID.randomUUID();
        Account a = new Account("1", random);
        assertThrows(NoBankAccountException.class, () -> {
            transactionService.validateBankAccount(a);
        });
    }

    @Test
    void validateAmountPositive() {
        assertDoesNotThrow(() -> transactionService.validateAmount(50f));
    }

    @Test
    void validateAmountNegative() {
        assertThrows(InvalidAmountException.class, () -> transactionService.validateAmount(-50f));
    }

    @Test
    void checkFundsPositive() {
        assertDoesNotThrow(() -> transactionService.checkFunds(100f, 50f));
    }

    @Test
    void checkFundsNegative() {
        assertThrows(InsufficientFundsException.class, () -> transactionService.checkFunds(20f, 50f));
    }

    @Test
    void depositPositive() {
        UUID userId = UUID.randomUUID();
        TestAccountRepository accountRepository = new TestAccountRepository();
        accountRepository.addAccount(new Account("1", userId, 100f));
        TransactionService service = new TransactionService(userId, accountRepository);

        assertTrue(service.deposit("1", 50f));
    }

    @Test
    void depositNegative() {
        UUID userId = UUID.randomUUID();
        TestAccountRepository accountRepository = new TestAccountRepository();
        accountRepository.addAccount(new Account("1", userId, 100f));
        TransactionService service = new TransactionService(userId, accountRepository);

        assertFalse(service.deposit("1", -50f));
    }

    @Test
    void withdrawPositive() {
        UUID userId = UUID.randomUUID();
        TestAccountRepository accountRepository = new TestAccountRepository();
        accountRepository.addAccount(new Account("1", userId, 100f));
        TransactionService service = new TransactionService(userId, accountRepository);

        assertTrue(service.withdraw("1", 50f));
    }

    @Test
    void withdrawNegative() {
        UUID userId = UUID.randomUUID();
        TestAccountRepository accountRepository = new TestAccountRepository();
        accountRepository.addAccount(new Account("1", userId, 20f));
        TransactionService service = new TransactionService(userId, accountRepository);

        assertFalse(service.withdraw("1", 50f));
    }

    @Test
    void transferPositive() {
        UUID userId = UUID.randomUUID();
        TestAccountRepository accountRepository = new TestAccountRepository();
        accountRepository.addAccount(new Account("from", userId, 100f));
        accountRepository.addAccount(new Account("to", userId, 0f));
        TransactionService service = new TransactionService(userId, accountRepository);

        assertTrue(service.transfer("from", "to", 50f));
    }

    @Test
    void transferNegative() {
        UUID userId = UUID.randomUUID();
        TestAccountRepository accountRepository = new TestAccountRepository();
        accountRepository.addAccount(new Account("from", userId, 20f));
        accountRepository.addAccount(new Account("to", userId, 0f));
        TransactionService service = new TransactionService(userId, accountRepository);

        assertFalse(service.transfer("from", "to", 50f));
    }

    // Returns accounts registered via addAccount instead of the base class's fixed stub
    private static class TestAccountRepository extends AccountRepository {
        private final Map<String, Account> accounts = new HashMap<>();

        void addAccount(Account account) {
            accounts.put(account.getBankAccountId(), account);
        }

        @Override
        public Account findByAccountId(String accountId) {
            return accounts.get(accountId);
        }
    }

}
