package forZeroTier;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MulticastReceiver - Implementa la recepción de mensajes multicast con concurrencia
 * Proyecto: Sistema de Chat Multicast con ZeroTier
 * Características: Concurrencia, Multicast, Detección automática de interfaz ZeroTier
 */
public class MulticastReceiver implements Runnable {
    private final String multicastAddress;
    private final int port;
    private final AtomicBoolean running;
    private MulticastSocket socket;
    private InetAddress group;
    private NetworkInterface ztInterface;
    private int messagesReceived = 0;
    
    public MulticastReceiver(String multicastAddress, int port) {
        this.multicastAddress = multicastAddress;
        this.port = port;
        this.running = new AtomicBoolean(false);
    }
    
    /**
     * Metodo principal del hilo receptor - Implementa concurrencia
     */
    @Override
    public void run() {
        running.set(true);
        
        try {
            // Inicializar receptor multicast
            initializeMulticastReceiver();
            
            System.out.println("[MULTICAST] Receptor iniciado correctamente");
            System.out.println("[INTERFAZ] " + (ztInterface != null ? 
                "Usando interfaz ZeroTier: " + ztInterface.getDisplayName() : 
                "Usando interfaz por defecto"));
            System.out.println("[RECEPTOR] Escuchando mensajes en " + multicastAddress + ":" + port);
            System.out.println();
            
            // Bucle principal de recepción (Ejecución concurrente)
            receiveMessages();
            
        } catch (IOException e) {
            System.err.println("[ERROR] Error en MulticastReceiver: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    /**
     * Inicializa el socket multicast y se une al grupo
     */
    private void initializeMulticastReceiver() throws IOException {
        socket = new MulticastSocket(port);
        group = InetAddress.getByName(multicastAddress);
        
        // Detectar y configurar interfaz ZeroTier
        ztInterface = detectZeroTierInterface();
        if (ztInterface != null) {
            try {
                socket.setNetworkInterface(ztInterface);
                
                // Unirse al grupo multicast en la interfaz específica
                SocketAddress groupAddress = new InetSocketAddress(group, port);
                socket.joinGroup(groupAddress, ztInterface);
                
                System.out.println("[ZEROTIER] Unido al grupo multicast en interfaz: " + 
                    ztInterface.getDisplayName());
                
            } catch (IOException e) {
                System.out.println("[ADVERTENCIA] Error configurando ZeroTier, usando método estándar: " + 
                    e.getMessage());
                socket.joinGroup(group);
            }
        } else {
            // Método estándar si no se detecta ZeroTier
            socket.joinGroup(group);
            System.out.println("[MULTICAST] Unido al grupo usando interfaz por defecto");
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
                
                // Buscar interfaces ZeroTier
                if ((name.contains("zt") || displayName.contains("zerotier") || 
                     name.startsWith("zt") || displayName.contains("zt")) && 
                    ni.isUp() && !ni.isLoopback()) {
                    
                    // Verificar que tenga direcciones IP
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
     * Bucle principal de recepción de mensajes (Ejecución concurrente)
     */
    private void receiveMessages() {
        byte[] buffer = new byte[1024];
        
        while (running.get()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                // Procesar mensaje recibido
                processReceivedMessage(packet);
                messagesReceived++;
                
            } catch (SocketTimeoutException e) {
                // Timeout normal, continuar
                continue;
            } catch (IOException e) {
                if (running.get()) {
                    System.err.println("[ERROR] Error recibiendo mensaje: " + e.getMessage());
                }
                break;
            }
        }
    }
    
    /**
     * Procesa un mensaje recibido
     */
    private void processReceivedMessage(DatagramPacket packet) {
        try {
            String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
            String senderIP = packet.getAddress().getHostAddress();
            
            // Obtener timestamp
            String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            
            // Mostrar mensaje recibido con formato
            System.out.println("[RECIBIDO " + timestamp + "] [" + senderIP + "] " + message);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Error procesando mensaje: " + e.getMessage());
        }
    }
    
    /**
     * Detiene el receptor de manera segura
     */
    public void stop() {
        running.set(false);
        
        try {
            if (socket != null && !socket.isClosed()) {
                // Salir del grupo multicast antes de cerrar
                if (ztInterface != null) {
                    SocketAddress groupAddress = new InetSocketAddress(group, port);
                    socket.leaveGroup(groupAddress, ztInterface);
                } else {
                    socket.leaveGroup(group);
                }
            }
        } catch (IOException e) {
            System.err.println("[ADVERTENCIA] Error saliendo del grupo multicast: " + e.getMessage());
        }
    }
    
    /**
     * Limpia recursos al cerrar
     */
    private void cleanup() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        
        System.out.println("[MULTICAST] Receptor cerrado correctamente");
        System.out.println("[ESTADÍSTICAS] Mensajes recibidos: " + messagesReceived);
    }
    
    /**
     * Método para obtener estadísticas (uso académico)
     */
    public void mostrarEstadisticas() {
        System.out.println();
        System.out.println("=== ESTADÍSTICAS DEL RECEPTOR ===");
        System.out.println("Dirección Multicast: " + multicastAddress);
        System.out.println("Puerto: " + port);
        System.out.println("Estado: " + (running.get() ? "Activo" : "Inactivo"));
        System.out.println("Mensajes recibidos: " + messagesReceived);
        System.out.println("Interfaz ZeroTier: " + (ztInterface != null ? 
            ztInterface.getDisplayName() : "No detectada"));
        System.out.println("=================================");
        System.out.println();
    }
    
    /**
     * Verifica si el receptor está activo
     */
    public boolean isRunning() {
        return running.get();
    }
}