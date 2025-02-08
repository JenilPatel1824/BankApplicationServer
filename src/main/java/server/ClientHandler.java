package server;

import exception.InsufficientBalanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.BankServiceImpl;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;

    private final BankServiceImpl bankServiceImpl;

    public ClientHandler(Socket socket, BankServiceImpl bankServiceImpl)
    {
        this.socket = socket;

        this.bankServiceImpl = bankServiceImpl;
    }

    @Override
    public void run()
    {
        try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream()))
        {

            logger.debug("Processing client request");

            Map<String, Object> request = (Map<String, Object>) input.readObject();

            logger.debug(request + " received by server");

            Map<String, Object> response = new HashMap<>();

            String command = request.get("command").toString();

                long userId = Long.parseLong(request.get("userId").toString());
                long accountNumber = Long.parseLong(request.get("accountNumber").toString());

                switch (command)
                {
                    case "CHECK":

                        long balance = bankServiceImpl.checkBalance(userId, accountNumber);

                        if (!bankServiceImpl.isValidUser(userId, accountNumber))
                        {
                            logger.error("Invalid user");

                            response.put("status", "fail");

                            response.put("message", "Verification failed");
                        }
                        else
                        {
                            response.put("balance", balance);

                            response.put("status", "success");
                        }
                        break;

                    case "DEPOSIT":

                        long depositAmount = Long.parseLong(request.get("amount").toString());

                        long updatedBalanceAfterDeposit = bankServiceImpl.deposit(userId, accountNumber, depositAmount);

                        if (!bankServiceImpl.isValidUser(userId, accountNumber))
                        {
                            logger.error("Invalid user");

                            response.put("status", "fail");

                            response.put("message", "Verification Failed");
                        }
                        else if (depositAmount<=0)
                        {
                            response.put("status", "fail");

                            response.put("message", "Amount must be greater than zero");
                        }
                        else
                        {
                            response.put("balance",updatedBalanceAfterDeposit);

                            response.put("status", "success");
                        }
                        break;

                    case "WITHDRAW":

                        long withdrawAmount = Long.parseLong(request.get("amount").toString());

                        if (!bankServiceImpl.isValidUser(userId, accountNumber))
                        {
                            logger.error("Invalid user");

                            response.put("status", "fail");

                            response.put("message", "Verification Failed");
                        }
                        else if (withdrawAmount <= 0)
                        {
                            response.put("status", "fail");

                            response.put("message", "Amount must be greater than zero");
                        }
                        else
                        {
                            long updatedBalance = -1;

                            try
                            {
                                updatedBalance = bankServiceImpl.withdraw(userId, accountNumber, withdrawAmount);

                                response.put("status", "success");

                                response.put("balance", updatedBalance);
                            }
                            catch (InsufficientBalanceException e)
                            {
                                response.put("status", "fail");

                                response.put("message", "Insufficient Balance");
                            }
                        }
                        break;
                }
            output.writeObject(response);
        }
        catch (SocketException e)
        {
            logger.error("Socket error: " + e.getMessage());
        }
        catch (Exception e)
        {
            logger.error("Error processing request: " + e.getMessage());
        }
        finally
        {
            try
            {
                socket.close();

                logger.debug("Socket closed");
            }
            catch (IOException e)
            {
                logger.error("Error closing socket: " + e.getMessage());
            }
        }
    }
}
