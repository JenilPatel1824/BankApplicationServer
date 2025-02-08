import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.BankServiceImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BankServiceImplTest
{
    private static final Logger logger = LoggerFactory.getLogger(BankServiceImplTest.class);

    private BankServiceImpl bankServiceImpl;

    @Before
    public void setUp()
    {
        bankServiceImpl =new BankServiceImpl();
    }

    @Test
    public void testDeposit() throws InterruptedException
    {
        logger.debug("Starting test for deposit");

        ExecutorService executorService = Executors.newFixedThreadPool(100);

        for(int i=0;i<100;i++)
        {
            Runnable r1 = () -> bankServiceImpl.deposit(1, 101,100);

            executorService.submit(r1);
        }
        executorService.shutdown();

        if (!executorService.awaitTermination(10, TimeUnit.SECONDS))
        {
            logger.error("Thread operations timed out in deposit");
        }

        Assert.assertEquals(10100, bankServiceImpl.checkBalance(1,101),0);
    }

    @Test
    public void testWithdraw() throws InterruptedException
    {
        logger.debug("Starting test for withdraw");

        ExecutorService executorService = Executors.newFixedThreadPool(100);

        for(int i=0;i<99;i++)
        {
            Runnable r1 = () -> bankServiceImpl.withdraw(1,101, 1);

            executorService.submit(r1);
        }

        executorService.shutdown();

        if (!executorService.awaitTermination(10, TimeUnit.SECONDS))
        {
            logger.error("Thread operations timed out in withdraw");
        }
        Assert.assertEquals(1, bankServiceImpl.checkBalance(1,101),0);
    }
}