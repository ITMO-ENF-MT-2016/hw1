package ru.ifmo.mt.fgb;

import com.sun.deploy.util.SyncAccess;

import java.util.concurrent.locks.*;

/**
 * Bank implementation.
 *
 * @author <Бабкин>
 */

public class BankImpl implements Bank {
    /**
     * An array of accounts by index.
     */
    private final Account[] accounts;

    /**
     * Creates new bank instance.
     * @param n the number of accounts (numbered from 0 to n-1).
     */
    public BankImpl(int n) {
        accounts = new Account[n];
        for (int i = 0; i < n; i++) {
            accounts[i] = new Account();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfAccounts() {
        return accounts.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getAmount(int index) {
        accounts[index].lock();
        int amount = accounts[index].amount;
        accounts[index].unlock();

        return amount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTotalAmount() {
        long sum = 0;
        for (Account account : accounts) {
            account.lock();
            sum += account.amount;
        }

        for (int i = accounts.length - 1; i >= 0; i--)
            accounts[i].unlock();

        return sum;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long deposit(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        Account account = accounts[index];
        account.lock();
        if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT)
            throw new IllegalStateException("Overflow");
        account.amount += amount;
        long curAmount = account.amount;
        account.unlock();
        return curAmount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long withdraw(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        Account account = accounts[index];
        account.lock();
        if (account.amount - amount < 0)
            throw new IllegalStateException("Underflow");
        account.amount -= amount;
        long curAmount = account.amount;
        account.unlock();
        return curAmount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transfer(int fromIndex, int toIndex, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        if (fromIndex == toIndex)
            throw new IllegalArgumentException("fromIndex == toIndex");

        Account from = accounts[fromIndex];
        Account to = accounts[toIndex];

        if (fromIndex < toIndex) {
            from.lock();
            to.lock();
        } else {
            to.lock();
            from.lock();
        }

        if (amount > from.amount)
            throw new IllegalStateException("Underflow");
        else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT)
            throw new IllegalStateException("Overflow");
        from.amount -= amount;
        to.amount += amount;

        if (fromIndex < toIndex) {
            from.unlock();
            to.unlock();
        } else {
            to.unlock();
            from.unlock();
        }
    }

    /**
     * Private account data structure.
     */
    private static class Account {
        /**
         * Amount of funds in this account.
         */
        int amount;

        private final Lock lock = new ReentrantLock();

        void lock() {
            lock.lock();
        }

        void unlock() {
            lock.unlock();
        }
    }
}
