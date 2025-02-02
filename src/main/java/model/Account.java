package model;

import java.util.concurrent.atomic.AtomicReference;
public class Account
{
    private long accountNumber;

    private AtomicReference<Long> balance;

    private long userId;

    public Account(long accountNumber,long userId)
    {
        this.userId=userId;

        this.accountNumber=accountNumber;

        this.balance=new AtomicReference<Long>(0L);
    }

    public Account(long accountNumber, long userId, long balance)
    {
        this.userId = userId;

        this.accountNumber=accountNumber;

        if(balance>=0)
        {
            this.balance = new AtomicReference<>(balance);
        }
        else
        {
            throw new IllegalArgumentException("Please enter valid initial amount");
        }
    }

    public long getAccountNumber()
    {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber)
    {
        this.accountNumber = accountNumber;
    }

    public long getBalance()
    {
        return balance.get();
    }

    public long setBalance(long balance)
    {
            return this.balance.updateAndGet(oldBalance->balance);
    }

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId=userId;
    }
}
