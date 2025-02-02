package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.BankService;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;

    private final BankService bankService;

    public ClientHandler(Socket socket, BankService bankService)
    {
        this.socket = socket;

        this.bankService = bankService;
    }

    @Override
    public void run()
    {
        try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream()))
        {
            logger.debug("Working for client inside run");

            while (true)
            {
                output.reset();

                Map<String,Object> request = (Map<String, Object>) input.readObject();

                logger.debug(request+ " recived by server" );

                if(request.get("command").equals("EXIT"))
                {
                    logger.info("Exiting server");

                    break;
                }

                long userId = Long.parseLong(request.get("userId").toString());

                long accountNumber = Long.parseLong(request.get("accountNumber").toString());

                Map<String,Object> response = new HashMap<>();

                switch (request.get("command").toString())
                {
                    case "CHECK":

                        long balance = bankService.checkBalance(userId, accountNumber);

                        if(balance==-1)
                        {
                            logger.info("verification failed returning fail status");

                            response.put("status", "fail");

                            response.put("message", "verification failed");
                        }
                        else
                        {
                            response.put("balance", balance);

                            response.put("status", "success");
                        }
                        break;

                    case "DEPOSIT":

                        long depositAmount = Long.parseLong(request.get("amount").toString());

                        if(depositAmount==-1)
                        {
                            response.put("status", "fai");

                            response.put("message", "Verification Failed");
                        }
                        else
                        {
                            response.put("balance", bankService.deposit(userId, accountNumber, depositAmount));

                            response.put("status", "success");
                        }
                        break;

                    case "WITHDRAW":
                        long withdrawAmount = Long.parseLong(request.get("amount").toString());

                        if(withdrawAmount==-1)
                        {
                            response.put("status", "fail");

                            response.put("message", "Verification Failed or Insufficient Balance or you entered 0");
                        }
                        else
                        {
                            logger.debug("calling withdraw method");

                            response.put("balance", bankService.withdraw(userId, accountNumber, withdrawAmount));

                            logger.debug("withdraw completed");

                            response.put("status", "success");
                        }
                }
                output.writeObject(response);
            }
        }
        catch (SocketException e)
        {
            logger.error("Client exited unexpectedly");
        }
        catch (Exception e)
        {
            logger.error("Something went wrong");
        }
    }
}
