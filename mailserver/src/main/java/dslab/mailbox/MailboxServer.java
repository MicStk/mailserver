package dslab.mailbox;

import java.io.*;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.Message;
import dslab.transfer.ListenerThread;
import dslab.util.Config;

public class MailboxServer implements IMailboxServer, Runnable {


    private ServerSocket dmtpSocket;
    private ServerSocket dmapSocket;
    private Config config;
    private String componentId;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ExecutorService pool;
    private Shell shell;
    private ConcurrentHashMap<Integer, Message> messages = new ConcurrentHashMap<>();

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MailboxServer(String componentId, Config config, InputStream in, PrintStream out) {

        this.config=config;
        this.inputStream=in;
        this.outputStream=out;
        this.componentId=componentId;
        this.pool= Executors.newFixedThreadPool(100);

        shell = new Shell(in, out);

        shell.register(this);

        shell.setPrompt(componentId + "> ");
    }

    @Override
    public void run() {

        try {
            dmtpSocket = new ServerSocket(config.getInt("dmtp.tcp.port"));
            dmapSocket = new ServerSocket(config.getInt("dmap.tcp.port"));

            pool.execute(new DMAPListenerThread(config, dmapSocket, messages, pool));
            pool.execute(new DMTPListenerThread(config, dmtpSocket, messages, pool));
            shell.run();

        } catch (IOException e) {
           throw new UncheckedIOException("Error while creating server socket", e);
        }



        System.out.println("Server is up! Type shutdown to exit!");


    }

    @Override
    @Command
    public void shutdown() {

        pool.shutdown();
        if (dmtpSocket != null) {
            try {
                dmtpSocket.close();
            } catch (IOException e) {
                System.err.println("Error while closing server socket: " + e.getMessage());
            }
        }

        if (dmapSocket != null) {
            try {
                dmapSocket.close();
            } catch (IOException e) {
                System.err.println("Error while closing server socket: " + e.getMessage());
            }
        }

        pool.shutdownNow();

       throw new StopShellException();
    }

    public static void main(String[] args) throws Exception {
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }
}
