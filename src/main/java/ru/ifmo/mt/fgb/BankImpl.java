package ru.ifmo.mt.fgb;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bank implementation.
 *
 * @author <Печеркин Александр>
 */
public class BankImpl implements Bank {
    /**
     * An array of accounts by index.
     */
    private final Account[] accounts;


    /**
     * Creates new bank instance.
     *
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
        accounts[index].lock.lock();
        try {
            return accounts[index].amount;
        } finally {
            accounts[index].lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTotalAmount() {
        long sum = 0;
        for (Account account : accounts) {
            account.lock.lock();
            sum += account.amount;
        }
        int i = accounts.length;
        while (i > 0) {
            accounts[i - 1].lock.unlock();
            i--;
        }
        return sum;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long deposit(int index, long amount) {
        checkAmountNonNegative(amount);
        Account account = accounts[index];
        account.lock.lock();
        try {
            if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT)
                throw new IllegalStateException("Overflow");
            account.amount += amount;
            return account.amount;
        } finally {
            account.lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long withdraw(int index, long amount) {
        checkAmountNonNegative(amount);
        Account account = accounts[index];
        account.lock.lock();
        try {
            if (account.amount - amount < 0)
                throw new IllegalStateException("Overflow");
            account.amount -= amount;
            return account.amount;
        } finally {
            account.lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transfer(int fromIndex, int toIndex, long amount) {
        checkAmountNonNegative(amount);
        if (fromIndex == toIndex)
            throw new IllegalArgumentException("fromIndex == toIndex");
        Account from = accounts[fromIndex];
        Account to = accounts[toIndex];
        boolean increase = toIndex >= fromIndex;
        if (increase) {
            lockInOrderIndex(from, to);
        } else {
            lockInOrderIndex(to, from);
        }
        try {
            if (amount > from.amount)
                throw new IllegalStateException("Underflow");
            else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT)
                throw new IllegalStateException("Overflow");
            from.amount -= amount;
            to.amount += amount;
        } finally {
            if (increase) {
                unlockInOrderIndex(to, from);
            } else {
                unlockInOrderIndex(from, to);
            }
        }
    }


    private void lockInOrderIndex(Account a, Account b) {
        a.lock.lock();
        b.lock.lock();
    }

    private void unlockInOrderIndex(Account a, Account b) {
        a.lock.unlock();
        b.lock.unlock();
    }


    private static void checkAmountNonNegative(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Negative amount");
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
        /**
         * Lock in this account.
         */
        private final Lock lock = new ReentrantLock();
    }
}
