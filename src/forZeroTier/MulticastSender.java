package forZeroTier;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MulticastSender - Implementa el envío de mensajes multicast con concurrencia
 * Proyecto: Sistema de Chat Multicast con ZeroTier
 * Características: Concurrencia, Multicast, Detección automática de interfaz ZeroTier
 */
public class MulticastSender {
    private final String multicastAddress;
    private final int port;
    private final BlockingQueue<String> messageQueue;
    private final AtomicBoolean running;
    private MulticastSocket socket;
    private InetAddress group;
    private NetworkInterface ztInterface;
    
    public MulticastSender(String multicastAddress, int port) {
        this.multicastAddress = multicastAddress;
        this.port = port;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.running = new AtomicBoolean(false);
    }
    
    /**
     * Inicia el sistema de envío de mensajes multicast
     * Implementa concurrencia con hilos separados para entrada de usuario y envío
     */
    public void start(String username) {
        running.set(true);
        
        try {
            // Inicializar socket y configuración multicast
            initializeMulticast();
            
            System.out.println("[MULTICAST] Emisor iniciado correctamente");
            System.out.println("[INTERFAZ] " + (ztInterface != null ? 
                "Usando interfaz ZeroTier: " + ztInterface.getDisplayName() : 
                "Usando interfaz por defecto"));
            System.out.println();
            
            // Crear hilo para procesamiento de mensajes (Concurrencia)
            Thread messageSenderThread = new Thread(() -> processMessageQueue(username));
            messageSenderThread.setName("MessageSender-Thread");
            messageSenderThread.setDaemon(true);
            messageSenderThread.start();
            
            // Hilo principal para entrada de usuario
            handleUserInput();
            
        } catch (IOException e) {
            System.err.println("[ERROR] Error al inicializar MulticastSender: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    /**
     * Inicializa la configuración multicast y detecta interfaz ZeroTier
     */
    private void initializeMulticast() throws IOException {
        socket = new MulticastSocket();
        group = InetAddress.getByName(multicastAddress);
        
        // Detectar y configurar interfaz ZeroTier
        ztInterface = detectZeroTierInterface();
        if (ztInterface != null) {
            try {
                socket.setNetworkInterface(ztInterface);
                System.out.println("[ZEROTIER] Interfaz detectada: " + ztInterface.getDisplayName());
            } catch (SocketException e) {
                System.out.println("[ADVERTENCIA] No se pudo configurar interfaz ZeroTier: " + e.getMessage());
            }
        }
        
        // Configurar TTL para alcance en red
        try {
            socket.setTimeToLive(255);
        } catch (IOException e) {
            System.out.println("[ADVERTENCIA] No se pudo configurar TTL: " + e.getMessage());
        }
    }
    
    /**
     * Detecta automáticamente la interfaz de red ZeroTier
     */
    private NetworkInterface detectZeroTierInterface() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                String name = ni.getName().toLowerCase();
                String displayName = ni.getDisplayName().toLowerCase();
                
                // Buscar interfaces ZeroTier por nombre
                if ((name.contains("zt") || displayName.contains("zerotier") || 
                     name.startsWith("zt") || displayName.contains("zt")) && 
                    ni.isUp() && !ni.isLoopback()) {
                    
                    // Verificar que tenga direcciones IP asignadas
                    Enumeration<InetAddress> addresses = ni.getInetAddresses();
                    if (addresses.hasMoreElements()) {
                        return ni;
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("[ERROR] Error detectando interfaces: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Maneja la entrada de usuario en el hilo principal
     */
    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Escriba sus mensajes (escriba 'exit' para salir):");
        
        while (running.get()) {
            try {
                String message = scanner.nextLine();
                
                if ("exit".equalsIgnoreCase(message.trim())) {
                    System.out.println("[SISTEMA] Saliendo del chat...");
                    running.set(false);
                    break;
                }
                
                if (!message.trim().isEmpty()) {
                    // Agregar mensaje a la cola para procesamiento concurrente
                    messageQueue.offer(message);
                }
                
            } catch (Exception e) {
                if (running.get()) {
                    System.err.println("[ERROR] Error leyendo entrada: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Procesa la cola de mensajes en hilo separado (Implementación de Concurrencia)
     */
    private void processMessageQueue(String username) {
        while (running.get()) {
            try {
                // Esperar por mensajes en la cola (operación bloqueante)
                String message = messageQueue.take();
                
                if (message != null && !message.trim().isEmpty()) {
                    sendMulticastMessage(username, message);
                }
                
            } catch (InterruptedException e) {
                // Hilo interrumpido, salir del bucle
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[ERROR] Error procesando mensajes: " + e.getMessage());
            }
        }
    }
    
    /**
     * Envía mensaje multicast a la red
     */
    private void sendMulticastMessage(String username, String message) {
        try {
            String fullMessage = username + ": " + message;
            byte[] buffer = fullMessage.getBytes("UTF-8");
            
            DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, group, port);
            
            socket.send(packet);
            
            // Mostrar confirmación de envío
            String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.println("[ENVIADO " + timestamp + "] " + fullMessage);
            
        } catch (IOException e) {
            System.err.println("[ERROR] Error enviando mensaje: " + e.getMessage());
        }
    }
    
    /**
     * Limpia recursos al cerrar
     */
    private void cleanup() {
        running.set(false);
        
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        
        System.out.println("[MULTICAST] Emisor cerrado correctamente");
    }
    
    /**
     * Método para mostrar estadísticas (uso académico)
     */
    public void mostrarEstadisticas() {
        System.out.println();
        System.out.println("=== ESTADÍSTICAS DEL EMISOR ===");
        System.out.println("Dirección Multicast: " + multicastAddress);
        System.out.println("Puerto: " + port);
        System.out.println("Estado: " + (running.get() ? "Activo" : "Inactivo"));
        System.out.println("Mensajes en cola: " + messageQueue.size());
        System.out.println("Interfaz ZeroTier: " + (ztInterface != null ? 
            ztInterface.getDisplayName() : "No detectada"));
        System.out.println("==============================");
        System.out.println();
    }
}