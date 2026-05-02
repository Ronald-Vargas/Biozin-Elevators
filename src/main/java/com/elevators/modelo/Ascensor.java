package com.elevators.modelo;


/**
 * Representa un ascensor individual dentro de un edificio.
 * Cada edificio tiene 3 ascensores (A1-A3, B1-B3, C1-C3).
 *
 * En la simulación real, cada ascensor correrá en su propio hilo
 * (un Thread por ascensor) y actualizará su estado de forma asíncrona.
 *
 * Los métodos están sincronizados para evitar condiciones de carrera
 * cuando el hilo del ascensor y el hilo de la UI accedan al mismo objeto.
 */
public class Ascensor {

    private final String id;              // Identificador: "A1", "A2", "B1", etc.
    private final int edificioId;         // 1, 2 o 3
    private int pisoActual;               // Piso donde se encuentra (1 a 25)
    private int pisoDestino;              // Piso al que se dirige
    private EstadoAscensor estado;        // SUBIENDO, BAJANDO, etc.
    private int ocupacion;                // Cantidad de pasajeros (0-10)
    private static final int CAPACIDAD_MAXIMA = 10;

    public Ascensor(String id, int edificioId, int pisoInicial) {
        this.id = id;
        this.edificioId = edificioId;
        this.pisoActual = pisoInicial;
        this.pisoDestino = pisoInicial;
        this.estado = EstadoAscensor.INACTIVO;
        this.ocupacion = 0;
    }

    // ===== Getters sincronizados =====
    // synchronized garantiza que solo un hilo lea/escriba a la vez

    public synchronized String getId() { return id; }
    public synchronized int getEdificioId() { return edificioId; }
    public synchronized int getPisoActual() { return pisoActual; }
    public synchronized int getPisoDestino() { return pisoDestino; }
    public synchronized EstadoAscensor getEstado() { return estado; }
    public synchronized int getOcupacion() { return ocupacion; }

    // ===== Setters sincronizados =====

    public synchronized void setPisoActual(int pisoActual) {
        this.pisoActual = pisoActual;
    }

    public synchronized void setPisoDestino(int pisoDestino) {
        this.pisoDestino = pisoDestino;
    }

    public synchronized void setEstado(EstadoAscensor estado) {
        this.estado = estado;
    }

    public synchronized void setOcupacion(int ocupacion) {
        this.ocupacion = Math.max(0, Math.min(ocupacion, CAPACIDAD_MAXIMA));
    }

    /**
     * Calcula la distancia desde el piso actual hasta un piso dado.
     * Se usa en el algoritmo de "ascensor más cercano".
     */
    public synchronized int distanciaA(int piso) {
        return Math.abs(pisoActual - piso);
    }

    /**
     * Indica si el ascensor está disponible (no en movimiento ni saturado).
     */
    public synchronized boolean estaDisponible() {
        return estado == EstadoAscensor.INACTIVO && ocupacion < CAPACIDAD_MAXIMA;
    }

    @Override
    public synchronized String toString() {
        return String.format("Ascensor[%s] piso=%d estado=%s ocupacion=%d/%d",
                id, pisoActual, estado, ocupacion, CAPACIDAD_MAXIMA);
    }
}
