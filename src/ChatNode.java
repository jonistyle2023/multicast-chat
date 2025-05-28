// Initial creation by Jonathan Panchana Rodríguez on 12/05/2025
// Group: 12 on Distributed Systems at Universidad Técnica Particular de Loja

import java.util.Scanner;

public class ChatNode {
    public static void main(String[] args) {
        final String multicastAddress = "224.0.0.0"; // Use a valid multicast address
        final int port = 4446;

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username:");
        String username = scanner.nextLine();

        // Start the multicast sender in a separate thread
        MulticastReceiver receiver = new MulticastReceiver(multicastAddress, port);
        Thread receiverThread = new Thread(receiver);
        receiverThread.start();

        // Start the multicast sender
        MulticastSender sender = new MulticastSender(multicastAddress, port);
        sender.start(username);

        // Wait for the receiver thread to finish
        System.out.println("Press any key to exit...");
        System.exit(0);
    }
}
