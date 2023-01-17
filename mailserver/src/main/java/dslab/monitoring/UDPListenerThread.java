package dslab.monitoring;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

public class UDPListenerThread extends Thread {

    private DatagramSocket datagramSocket;
    private ConcurrentHashMap<String, Integer> addresses;
    private ConcurrentHashMap<String, Integer> servers;

    public UDPListenerThread(DatagramSocket socket, ConcurrentHashMap<String, Integer> addresses, ConcurrentHashMap<String, Integer> servers) {
    this.datagramSocket=socket;
    this.addresses=addresses;
    this.servers=servers;
    }

    public void run() {

        byte[] buffer;
        DatagramPacket packet;
        try {
            while (true) {
                buffer = new byte[1024];

                packet = new DatagramPacket(buffer, buffer.length);

                datagramSocket.receive(packet);

                String request = new String(packet.getData());
                request = request.trim();

                String[] parts = request.split(" ");

                if (parts.length == 2) {
                    String server = parts[0];
                    String address = parts[1];

                    servers.computeIfPresent(server, (s,v) -> v+1);
                    servers.put(server, servers.getOrDefault(server,1));

                    addresses.computeIfPresent(address, (s,v) -> v+1);
                    addresses.put(address, addresses.getOrDefault(address,1));

                }
            }

        } catch (SocketException e) {
            System.out.println("SocketException while waiting for/handling packets: " + e.getMessage());
            return;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (datagramSocket != null && !datagramSocket.isClosed()) {
                datagramSocket.close();
            }
        }

    }


}

