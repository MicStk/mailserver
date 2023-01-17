package dslab.transfer;

import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.util.Config;

public class TransferServer implements ITransferServer, Runnable {

    private ServerSocket serverSocket;
    private Config config;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Shell shell;
    private ExecutorService pool = Executors.newFixedThreadPool(100);
    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public TransferServer(String componentId, Config config, InputStream in, PrintStream out) {

        this.config=config;
        this.inputStream=in;
        this.outputStream=out;


        shell = new Shell(in, out);

        shell.register(this);

        shell.setPrompt(componentId + "> ");
    }

    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(config.getInt("tcp.port"));
            pool.execute(new ListenerThread(serverSocket, config, pool));
            pool.execute(shell);

        } catch (IOException e) {
            throw new UncheckedIOException("Error while creating server socket", e);
        }

        System.out.println("Server is up! Type shutdown to exit!");

    }

    @Override
    @Command
    public void shutdown() {

        pool.shutdown();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error while closing server socket: " + e.getMessage());
            }
        }
        pool.shutdownNow();
        throw new StopShellException();
    }

    public static void main(String[] args) throws Exception {
       ITransferServer server = ComponentFactory.createTransferServer(args[0], System.in, System.out);
        server.run();

    }

}
