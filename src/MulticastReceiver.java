import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiver implements Runnable {
    private final String multicastAddress;
    private final int port;

    public MulticastReceiver(String multicastAddress, int port) {
        this.multicastAddress = multicastAddress;
        this.port = port;
    }

    @Override
    public void run() {
        try (java.net.MulticastSocket socket = new java.net.MulticastSocket(port)) {
            java.net.InetAddress group = java.net.InetAddress.getByName(multicastAddress);
            socket.joinGroup(group);

            byte[] buffer = new byte[1024];
            while (true) {
                java.net.DatagramPacket packet = new java.net.DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
