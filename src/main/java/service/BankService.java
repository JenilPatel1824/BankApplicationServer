package service;

import exception.InsufficientBalanceException;

public abstract class BankService
{
    public abstract long checkBalance(long userId, long accountNumber);

    public abstract long deposit(long userId, long accountNumber, long amount);

    public abstract long withdraw(long userId, long accountNumber, long amount) throws InsufficientBalanceException;
}
