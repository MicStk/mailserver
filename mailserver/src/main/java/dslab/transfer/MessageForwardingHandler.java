package dslab.transfer;

import dslab.Domain;
import dslab.Message;
import dslab.util.Config;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;

public class MessageForwardingHandler implements Runnable{

    private Message message;
    private Config config;
    private Domain domain;

    public MessageForwardingHandler(Message message, Config config) {
        this.message=message;
        this.config=config;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        Socket sendSocket;
        List<String> tos = message.getTo();
        List<Domain> domains = new ArrayList<>();
        List<String> domainList = new ArrayList<>();
        Config domainConfig = new Config("domains");
        for (String to : tos){
            String toDomain = to.substring(to.indexOf("@") + 1);
           try {
               if (!domainList.contains(domainConfig.getString(toDomain))) {
                   domainList.add(domainConfig.getString(toDomain));
               }
           } catch (MissingResourceException e){
               System.out.println("MissingResourceException " + e);
           }
        }

        for (String domain: domainList) {

            try {
                String mailServers = domain;
                String[] helper = mailServers.split(":");
                String host = helper[0];
                int port = Integer.parseInt(helper[1]);
                sendSocket=new Socket(host,port);

                BufferedReader reader = new BufferedReader(new InputStreamReader(sendSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(sendSocket.getOutputStream(), true);

                writer.println("begin");

                writer.println("from " + message.getFrom());

                String from="";
                for (String recipient: message.getTo()) {

                    from+=recipient+",";
                }
                if (from.endsWith(",")) {
                     from = from.substring(0, from.length() - 1);
                }
                writer.println("to " + from);

                writer.println("subject " + message.getSubject());

                writer.println("data " + message.getData());

                writer.println("send");

                writer.println("quit");

            } catch (IOException e) {
                System.out.println("IOException " + e);
            }




        }

        DatagramSocket socket;
        try {

            socket = new DatagramSocket();

            byte[] buffer;
            DatagramPacket packet;
            String udpMessage="";
            String udpHost="";
            int udpPort=0;
            try {
               String localAdress = InetAddress.getLocalHost().getHostAddress();
                udpPort = config.getInt("monitoring.port");
                 udpHost += config.getString("monitoring.host");
                int tcpPort = config.getInt("tcp.port");
                udpMessage += localAdress + ":" + tcpPort + " " + message.getFrom();

            } catch (UnknownHostException e) {
                System.out.println("UnknownHostException " + e);
            }


                buffer = udpMessage.getBytes();

                packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(udpHost), udpPort);

                socket.send(packet);

            } catch (IOException e) {
            throw new UncheckedIOException(e);
        }



    }
}
