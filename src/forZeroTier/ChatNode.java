package forZeroTier;

import multicast.MulticastReceiver;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ChatNode - Clase principal que ejecuta el sistema de chat multicast
 * Proyecto: Sistema de Chat Multicast con ZeroTier y Concurrencia
 * 
 * Características implementadas:
 * - Multicast para comunicación en red
 * - Concurrencia con hilos separados para envío y recepción
 * - Compatibilidad con redes virtuales ZeroTier
 * - Gestión automática de recursos
 *
 * Créditos:
 * - Creado por: Andrés Alejandro Sánchez Garzón el 29/05/2025
 * - Implementación por: Jonathan David Panchana Rodríguez
 * - Supervisado por: Flavio Javier Sánchez Garzón
 */
public class ChatNode {
    private static final String MULTICAST_ADDRESS = "224.0.0.1"; // Dirección multicast válida
    private static final int PORT = 4446;                        // Puerto de comunicación
    
    private static MulticastReceiver receiver;
    private static ExecutorService executorService;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Banner del proyecto
        System.out.println("=====================================");
        System.out.println("  SISTEMA DE CHAT MULTICAST");
        System.out.println("  Implementación con Concurrencia");
        System.out.println("  Compatible con ZeroTier");
        System.out.println("=====================================");
        System.out.println();
        
        // Solicitar información del usuario
        System.out.print("Ingrese su nombre de usuario: ");
        String username = scanner.nextLine().trim();
        
        if (username.isEmpty()) {
            username = "Usuario" + System.currentTimeMillis() % 1000;
            System.out.println("Usando nombre por defecto: " + username);
        }
        
        System.out.println();
        System.out.println("Configuración del sistema:");
        System.out.println("- Dirección Multicast: " + MULTICAST_ADDRESS);
        System.out.println("- Puerto: " + PORT);
        System.out.println("- Usuario: " + username);
        System.out.println();
        
        // Configurar el pool de hilos para concurrencia
        executorService = Executors.newFixedThreadPool(2);
        
        try {
            // Inicializar y ejecutar el receptor en un hilo separado
            System.out.println("[SISTEMA] Iniciando receptor de mensajes...");
            receiver = new MulticastReceiver(MULTICAST_ADDRESS, PORT);
            executorService.submit(receiver);
            
            // Dar tiempo al receptor para inicializarse
            Thread.sleep(1500);
            
            // Configurar shutdown hook para cierre limpio
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                shutdown();
            }));
            
            System.out.println("[SISTEMA] Iniciando emisor de mensajes...");
            System.out.println("[SISTEMA] ¡Sistema listo! Puede comenzar a enviar mensajes.");
            System.out.println("[INSTRUCCIONES] Escriba 'exit' para salir del programa");
            System.out.println("========================================");
            System.out.println();
            
            // Inicializar y ejecutar el emisor en el hilo principal
            MulticastSender sender = new MulticastSender(MULTICAST_ADDRESS, PORT);
            sender.start(username);
            
        } catch (InterruptedException e) {
            System.err.println("[ERROR] Interrupción del sistema: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("[ERROR] Error al inicializar el sistema: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }
    
    /**
     * Método para cerrar el sistema de manera ordenada
     * Implementa el cierre correcto de hilos y recursos
     */
    private static void shutdown() {
        System.out.println();
        System.out.println("[SISTEMA] Cerrando sistema de chat...");
        
        try {
            // Detener el receptor si está activo
            if (receiver != null) {
                receiver.run();
            }
            
            // Cerrar el pool de hilos
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                
                // Esperar que terminen las tareas en ejecución
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("[SISTEMA] Forzando cierre de hilos...");
                    executorService.shutdownNow();
                }
            }
            
        } catch (InterruptedException e) {
            System.err.println("[ERROR] Error durante el cierre: " + e.getMessage());
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("[SISTEMA] ¡Sistema cerrado correctamente!");
        System.out.println("========================================");
    }
    
    /**
     * Método para mostrar información del sistema (uso académico)
     */
    public static void mostrarInformacionSistema() {
        System.out.println();
        System.out.println("=== INFORMACIÓN DEL SISTEMA ===");
        System.out.println("Tecnologías implementadas:");
        System.out.println("✓ Multicast UDP para comunicación en red");
        System.out.println("✓ Concurrencia con ExecutorService");
        System.out.println("✓ Hilos separados para envío y recepción");
        System.out.println("✓ Gestión automática de recursos");
        System.out.println("✓ Compatibilidad con redes virtuales ZeroTier");
        System.out.println("✓ Manejo de excepciones y cierre limpio");
        System.out.println("===============================");
        System.out.println();
    }
}
