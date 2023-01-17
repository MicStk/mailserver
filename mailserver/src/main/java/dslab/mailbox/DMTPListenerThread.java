package dslab.mailbox;

import dslab.Message;
import dslab.util.Config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class DMTPListenerThread extends Thread {
    private Config config;
    private ServerSocket serverSocket;
    private ConcurrentHashMap<Integer, Message> messages;
    private ExecutorService pool;

    public DMTPListenerThread(Config config, ServerSocket dmtpSocket, ConcurrentHashMap<Integer, Message> messages, ExecutorService pool) {
        this.config=config;
        this.serverSocket=dmtpSocket;
        this.messages=messages;
        this.pool=pool;
    }

    public void run() {

        while (true) {
            Socket socket;
            try {

                socket = serverSocket.accept();

                    pool.execute(new DMTPHandler(socket, config, messages));


            } catch (SocketException e) {
                System.out.println("SocketException while handling socket: " + e.getMessage());
                break;
            } catch (IOException e) {

                throw new UncheckedIOException(e);
            }

        }
    }
}
