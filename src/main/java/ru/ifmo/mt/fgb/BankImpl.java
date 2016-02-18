package ru.ifmo.mt.fgb;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bank implementation.
 *
 * @author Ван-Юн-Сян Тяня
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
        accounts[index].locker.lock();

        try {
            return accounts[index].amount;
        } finally {
            accounts[index].locker.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTotalAmount() {
        for (Account account : accounts) {
            account.locker.lock();
        }

        try {
            long sum = 0;
            for (Account account : accounts) {
                sum += account.amount;
            }
            return sum;
        } finally {
            for (Account account : accounts) {
                account.locker.unlock();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long deposit(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);

        Account account = accounts[index];

        account.locker.lock();
        try {
            if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT)
                throw new IllegalStateException("Overflow");

            account.amount += amount;
            return account.amount;
        } finally {
            account.locker.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long withdraw(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);

        Account account = accounts[index];

        account.locker.lock();
        try {
            if (account.amount - amount < 0)
                throw new IllegalStateException("Underflow");

            account.amount -= amount;
            return account.amount;
        } finally {
            account.locker.unlock();
        }
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

        Lock lesserLock = from.locker;
        Lock greaterLock = to.locker;
        if (fromIndex > toIndex) {
            lesserLock = to.locker;
            greaterLock = from.locker;
        }

        lesserLock.lock();
        try {
            greaterLock.lock();
            try {
                if (amount > from.amount)
                    throw new IllegalStateException("Underflow");
                else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT)
                    throw new IllegalStateException("Overflow");
                from.amount -= amount;
                to.amount += amount;
            } finally {
                greaterLock.unlock();
            }
        } finally {
            lesserLock.unlock();
        }
    }

    /**
     * Private account data structure.
     */
    private static class Account {
        /**
         * Amount of funds in this account.
         */
        final Lock locker = new ReentrantLock();
        int amount;
    }
}
