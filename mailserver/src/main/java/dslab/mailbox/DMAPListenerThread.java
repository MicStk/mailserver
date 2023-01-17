package dslab.mailbox;

import dslab.Message;
import dslab.util.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class DMAPListenerThread extends Thread {

    private Config config;
    private ServerSocket serverSocket;
    private ConcurrentHashMap<Integer, Message> messages;
    private ExecutorService pool;
    public DMAPListenerThread(Config config, ServerSocket dmapSocket, ConcurrentHashMap<Integer, Message> messages, ExecutorService pool) {
        this.config=config;
        this.serverSocket=dmapSocket;
        this.messages=messages;
        this.pool=pool;
    }

    public void run() {

        while (true) {
            Socket socket;
            try {

                socket = serverSocket.accept();

                synchronized(this) {
                    pool.execute(new DMAPHandler(socket, config, messages));
                }

            } catch (SocketException e) {
                // when the socket is closed, the I/O methods of the Socket will throw a SocketException
                // almost all SocketException cases indicate that the socket was closed
                System.out.println("SocketException while handling socket: " + e.getMessage());
                break;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        }
    }

}

