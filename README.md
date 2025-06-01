# MulticastChat - Comunicación en Sistemas Distribuidos con Java

>[!NOTE]  
>Este proyecto implementa un chat grupal utilizando **multicast UDP** y **concurrencia** en Java, cumpliendo con los requerimientos académicos de una red de nodos distribuidos que se comunican eficientemente en tiempo real.

---

>[!IMPORTANT]
> 
>Créditos:
> - **Desarrollo inicial:** Jonathan David Panchana Rodríguez (multicast package)
> - **Supervisado por:** Flavio Javier Carvajal Carrera
> - **Desarrollo final:** Andrés Alejandro Sánchez Garzón (forZeroTier Package)

## Objetivo del Proyecto

Desarrollar una aplicación distribuida que utilice **comunicación multicast** y **concurrencia** para enviar y recibir mensajes entre múltiples nodos en una red virtual, permitiendo la captura y análisis del tráfico mediante **Wireshark**.

---

## Requisitos Cubiertos

- ✔️ Implementación de Multicast
- ✔️ Manejo de Concurrencia
- ✔️ Captura y análisis con Wireshark
- ✔️ Informe y Conclusiones
- ✔️ Originalidad y Normas de APA

---

## Tecnologías Utilizadas

- Lenguaje: **Java 23**
- Entorno: **IntelliJ IDEA en Windows 11**
- Protocolo: **UDP Multicast**
- Red virtual: **Hamachi / ZeroTier**
- Análisis de tráfico: **Wireshark**

---

## Estructura del Proyecto

```bash
MulticastChat/
├── src/
│   ├── multicast.ChatNode.java        # Clase principal para ejecutar nodos de chat
│   ├── multicast.MulticastSender.java # Maneja envío de mensajes multicast
│   └── multicast.MulticastReceiver.java # Hilo concurrente para recepción de mensajes
└── README.md
```

---
## Instrucciones 

**De manera local:**
1. Clona el repositorio.
2. Asegúrate de tener Java 23 instalado.
3. Compila el proyecto con `javac src/*.java`.
4. Ejecuta el nodo con `java ChatNode.java`.

En este punto asegurate de abrir tantos terminales como nodos quieras crear, cada uno ejecutando el comando anterior.
Aqui un ejemplo:

_Nodo 1:_
![Nodo 1.png](resourses/Nodo%201.png)

_Nodo 2:_
![Nodo 2.png](resourses/Nodo%202.png)

y asi con cada nodo que quieras crear, recuerda cambiar el nombre de usuario en cada terminal para que se diferencien los mensajes.
