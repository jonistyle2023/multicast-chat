import java.net.*;
import java.util.*;

public class UnicastSender {
    private static final int PORT = 5000;

    // Reemplaza estas IPs con las direcciones ZeroTier de tus compañeros
    private static final List<String> peerIPs = Arrays.asList(
            "10.147.17.213",  // IP ZeroTier de Javier
            "10.147.17.243",  // IP ZeroTier de compañero 2
            "192.168.191.4"   // IP ZeroTier de compañero 3
    );

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        DatagramSocket socket = new DatagramSocket();

        System.out.print("Ingresa tu nombre de usuario: ");
        String username = scanner.nextLine();

        System.out.println("Comienza a chatear:");

        while (true) {
            String mensaje = scanner.nextLine();
            String mensajeFinal = username + ": " + mensaje;

            for (String ip : peerIPs) {
                InetAddress address = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(
                        mensajeFinal.getBytes(), mensajeFinal.length(), address, PORT
                );
                socket.send(packet);
            }
        }
    }
}
