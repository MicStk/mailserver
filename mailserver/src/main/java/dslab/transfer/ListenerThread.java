package dslab.transfer;

import dslab.mailbox.DMTPHandler;
import dslab.util.Config;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

public class ListenerThread extends Thread{
    private ServerSocket serverSocket;
    private Config config;
    private ExecutorService pool;

    public ListenerThread(ServerSocket serverSocket, Config config, ExecutorService pool) {
        this.serverSocket=serverSocket;
        this.config=config;
        this.pool=pool;
    }

    public void run() {

        while (true) {
            Socket socket;
            try {
                socket = serverSocket.accept();
                pool.execute(new DMTPClient(socket, config, pool));

            } catch (SocketException e) {

                System.out.println("SocketException while handling socket: " + e.getMessage());
                break;
            } catch (IOException e) {

                throw new UncheckedIOException(e);
            }

        }
    }
}
