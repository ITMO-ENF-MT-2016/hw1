package ru.ifmo.mt.fgb;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bank implementation.
 *
 * @author Максименко Александр
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
        accounts[index].lock.lock();
        try {
            return  accounts[index].amount;
        }
        finally {
            accounts[index].lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTotalAmount() {
        long sum = 0;
        for (int i = 0; i < accounts.length; i++) {
            accounts[i].lock.lock();
            sum += accounts[i].amount;
        }
        for (int i = accounts.length - 1; i >= 0; i--) {
            accounts[i].lock.unlock();
        }
        return sum;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long deposit(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        accounts[index].lock.lock();
        try {
            if (amount > MAX_AMOUNT || accounts[index].amount + amount > MAX_AMOUNT)
                throw new IllegalStateException("Overflow");
            accounts[index].amount += amount;
            return accounts[index].amount;
        }
        finally {
            accounts[index].lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long withdraw(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        accounts[index].lock.lock();
        try {
            if (accounts[index].amount - amount < 0)
                throw new IllegalStateException("Underflow");
            accounts[index].amount -= amount;
            return accounts[index].amount;
        }
        finally {
            accounts[index].lock.unlock();
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
        if (toIndex < fromIndex) {
            accounts[toIndex].lock.lock();
            accounts[fromIndex].lock.lock();
        } else {
            accounts[fromIndex].lock.lock();
            accounts[toIndex].lock.lock();
        }
        try {
            if (amount > accounts[fromIndex].amount)
                throw new IllegalStateException("Underflow");
            else if (amount > MAX_AMOUNT || accounts[toIndex].amount + amount > MAX_AMOUNT)
                throw new IllegalStateException("Overflow");
            accounts[fromIndex].amount -= amount;
            accounts[toIndex].amount += amount;
        }
        finally {
            if (toIndex < fromIndex) {
                accounts[fromIndex].lock.unlock();
                accounts[toIndex].lock.unlock();
            } else {
                accounts[toIndex].lock.unlock();
                accounts[fromIndex].lock.unlock();
            }
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
         * Lock the account
         */
        Lock lock = new ReentrantLock();
    }
}
