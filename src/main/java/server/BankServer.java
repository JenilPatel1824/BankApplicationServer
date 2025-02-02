package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.BankService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class BankServer
{
    private static final Logger logger = LoggerFactory.getLogger(BankServer.class);

    private static final int PORT = 9999;

    public static void main(String[] args)
    {
        BankService bankService = new BankService();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(10,10000,60, TimeUnit.SECONDS,new ArrayBlockingQueue<>(100000));

        try (ServerSocket serverSocket = new ServerSocket(PORT))
        {
            System.out.println("Bank Server running on port " + PORT);

            while (true)
            {
                Socket clientSocket = serverSocket.accept();

                logger.info("New client connected from " + clientSocket.getRemoteSocketAddress());

                executor.execute(new ClientHandler(clientSocket, bankService));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            executor.shutdown();
        }
    }
}
