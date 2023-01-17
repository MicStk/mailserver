package dslab.monitoring;

import java.io.*;
import java.net.DatagramSocket;
import java.net.HttpCookie;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.util.Config;

public class MonitoringServer implements IMonitoringServer, Runnable {

    private DatagramSocket socket;
    private Config config;
    private Shell shell;
    private ConcurrentHashMap<String, Integer> addresses =new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> servers = new ConcurrentHashMap<>();
    private InputStream in;
    private PrintStream out;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MonitoringServer(String componentId, Config config, InputStream in, PrintStream out) throws SocketException {
        this.config = config;
        int port = config.getInt("udp.port");
        this.socket = new DatagramSocket(port);
        this.in=in;
        this.out=out;


        shell = new Shell(in, out);

        shell.register(this);

        shell.setPrompt(componentId + "> ");

    }

    @Override
    public void run() {
        try {
            new UDPListenerThread(socket, addresses, servers).start();
        } catch (RuntimeException e) {
            throw new RuntimeException("Cannot listen on UDP port.", e);
        }


        System.out.println("Server is up! Type shutdown to exit!");
        shell.run();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (IOException e) {

        }
    }

    @Override
    @Command
    public void addresses() {
        StringBuilder builder = new StringBuilder();
        addresses.forEach((k, v) -> builder.append(k).append(" ").append(v).append("\n"));
        shell.out().println(builder);
    }

    @Override
    @Command
    public void servers() {
        StringBuilder builder = new StringBuilder();
        servers.forEach((k, v) -> builder.append(k).append(" ").append(v).append("\n"));
        shell.out().println(builder);
    }

    @Override
    @Command
    public void shutdown() {
        if (socket != null) {
            socket.close();
        }
        throw new StopShellException();
    }

    public static void main(String[] args) throws Exception {
        IMonitoringServer server = ComponentFactory.createMonitoringServer(args[0], System.in, System.out);
        server.run();
    }

}
