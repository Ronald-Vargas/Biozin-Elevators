package com.elevators.modelo;


import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representa una solicitud de un pasajero para usar un ascensor.
 * Se crea cuando el pasajero presiona el botón de llamada (subir/bajar)
 * desde un piso específico de un edificio.
 *
 * Las solicitudes se almacenan en una Cola por edificio y se priorizan
 * usando los algoritmos de ordenamiento (Quicksort, Mergesort, Heapsort).
 */
public class Solicitud {

    // Generador de IDs únicos en orden secuencial. AtomicInteger es thread-safe,
    // importante porque varios hilos pueden crear solicitudes simultáneamente.
    private static final AtomicInteger contadorId = new AtomicInteger(0);

    private final int id;                    // Identificador único de la solicitud
    private final int edificioId;            // A qué edificio pertenece (1, 2 o 3)
    private final int pisoOrigen;            // Piso desde donde el pasajero llama
    private final int pisoDestino;           // Piso al que quiere ir (puede ser -1 si aún no lo elige)
    private final Direccion direccion;       // SUBIR o BAJAR
    private final LocalDateTime momentoCreacion; // Cuándo se generó (para métricas)
    private int prioridad;                   // Valor para ordenar (a menor número, más prioridad)

    /**
     * Constructor para una solicitud desde el lobby (sin piso destino aún).
     */
    public Solicitud(int edificioId, int pisoOrigen, Direccion direccion) {
        this(edificioId, pisoOrigen, -1, direccion);
    }

    /**
     * Constructor para una solicitud completa (origen y destino).
     */
    public Solicitud(int edificioId, int pisoOrigen, int pisoDestino, Direccion direccion) {
        this.id = contadorId.incrementAndGet();
        this.edificioId = edificioId;
        this.pisoOrigen = pisoOrigen;
        this.pisoDestino = pisoDestino;
        this.direccion = direccion;
        this.momentoCreacion = LocalDateTime.now();
        // Por defecto, la prioridad es la distancia al piso 1 (los algoritmos pueden cambiarla)
        this.prioridad = pisoOrigen;
    }

    // ===== Getters =====
    public int getId() { return id; }
    public int getEdificioId() { return edificioId; }
    public int getPisoOrigen() { return pisoOrigen; }
    public int getPisoDestino() { return pisoDestino; }
    public Direccion getDireccion() { return direccion; }
    public LocalDateTime getMomentoCreacion() { return momentoCreacion; }
    public int getPrioridad() { return prioridad; }

    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }

    @Override
    public String toString() {
        return String.format("Solicitud#%d [Edif %d, Piso %d → %s, prioridad=%d]",
                id, edificioId, pisoOrigen, direccion, prioridad);
    }
}
