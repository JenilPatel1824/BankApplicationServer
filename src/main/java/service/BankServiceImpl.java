package service;

import exception.InsufficientBalanceException;
import model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.concurrent.ConcurrentHashMap;

public class BankServiceImpl extends BankService
{
    private static final Logger logger = LoggerFactory.getLogger(BankServiceImpl.class);

    private static final ConcurrentHashMap<Long, Account> accounts = new ConcurrentHashMap<>();

    private static final ZContext context = new ZContext();

    private final ZMQ.Socket pushSocket = context.createSocket(SocketType.PUSH);

    public BankServiceImpl()
    {
        logger.info("Initializing accounts...");

        accounts.put(1L, new Account(101, 1, 100));

        accounts.put(2L, new Account(102, 2, 100));

        accounts.put(3L, new Account(103, 3, 100));

        accounts.put(4L, new Account(104, 4, 100));

        accounts.put(5L, new Account(105, 5));

        try
        {
            logger.info("trying to conncet Notification server");

            pushSocket.bind("tcp://localhost:5556");

        }
        catch (Exception e)
        {
            logger.error("Connection to notification server failed");
        }

    }

    public boolean isValidUser(long userId, long accountNumber)
    {
        logger.debug("Validate user called for {} {}",userId , accountNumber);

        Account account = accounts.get(userId);

        if(account!=null)
        {
            if(account.getAccountNumber()==accountNumber)
            {
                pushSocket.send("New Login for "+accountNumber +" "+userId);

                logger.info("Notification sent");

                return true;
            }
        }
        return false;
    }

    @Override
    public long checkBalance(long userId, long accountNumber)
    {
        return accounts.computeIfPresent(userId, (key, account) -> {

                long balance = account.getBalance();

                logger.info("checkBalance Successfull for {} {} : {}",userId,accountNumber,balance);

                return account;

        }) != null ? accounts.get(userId).getBalance() : -1;
    }

    @Override
    public long withdraw(long userId,long accountNumber,long withdrawAmount)
    {
        logger.debug("Withdrawing {} from account number: {}", withdrawAmount, accountNumber);

        return accounts.computeIfPresent(userId, (key, account) -> {

                long balance;

                if(withdrawAmount>=0 && account.getBalance()>=withdrawAmount)
                {
                    balance=account.setBalance(account.getBalance()-withdrawAmount);

                    logger.info("withdraw Successfull for {} {} : new balance: {}",userId,accountNumber,balance);

                    pushSocket.send("withdrawal successful for amount: "+ withdrawAmount +" account number "+accountNumber +" userid: "+userId+" new balance: "+balance);

                    logger.info("Notification sent");
                }
                else
                {
                    logger.error("Insufficient Balance !!");

                    throw new InsufficientBalanceException("Insufficient Balance !!");
                }
                return account;

        }) != null ? accounts.get(userId).getBalance() : -1;
    }

    @Override
    public long deposit(long userId,long accountNumber,long depositAmount)
    {
        logger.debug("Depositing {} to account number: {}", depositAmount, accountNumber);

        return accounts.computeIfPresent(userId, (key, account) -> {

                if(depositAmount>=0)
                {
                    long balance = account.setBalance(depositAmount+account.getBalance());

                    logger.info("deposit Successfull for {} {} : {}",userId,accountNumber,balance);

                    pushSocket.send("deposit successful for amount: "+ depositAmount +" account number "+accountNumber +" userid: "+userId+" new balance: "+balance);

                    logger.info("Notification sent");
                }
                else
                {
                    logger.error("Balance not >=0");
                }
                return account;

        }) != null ? accounts.get(userId).getBalance() : -1;
    }
}

