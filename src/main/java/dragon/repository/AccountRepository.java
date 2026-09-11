package dragon.repository;

import dragon.entity.Account;
import dragon.entity.User;

public class AccountRepository {
    // Does a bank account the id exist? If so, return the Account object, else return null
    public Account findByAccountId(String accountId) {
        return new Account("123", "123");
    }

    // Is there a user with the account id?
    public boolean existsByAccountId(String accountId) {
        return false;
    }

    // Return true if bank account belongs to passed in ownerId, false otherwise
    public boolean checkOwnership(Account account, String ownerId) {
        String inputOwner = account.getOwnerId();
        return inputOwner.equals(ownerId);
    }

    // Add a new Account to the db.
    public void save(User user) {};

    /* Deposits specified amount of money to account. Returns true if successful, false if not
    (Ex: User entered invalid amount)
    */

    public boolean deposit(Account account, float amount) {
        if (amount >= 0) {
            float currentBalance = account.getBalance();
            account.setBalance(currentBalance + amount);
            return true;
        }
        return false;
    }

    /* Withdraws specified amount of money from account. Returns true if successful, false if not
    (Ex: User entered invalid amount, not enough funds, etc.)
    */
    public boolean withdraw(Account account, float amount) {
        float currentBalance = account.getBalance();
        if (amount >= 0 && currentBalance >= amount) {
            account.setBalance(currentBalance + amount);
            return true;
        }
        return false;
    }
}
