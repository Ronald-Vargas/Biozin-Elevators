package com.elevators.controlador;

import com.elevators.modelo.Ascensor;
import com.elevators.modelo.EstadoAscensor;
import com.elevators.modelo.Solicitud;
import com.elevators.estructuras.Cola;
import com.elevators.estructuras.Pila;

/**
 * Hilo independiente que simula el movimiento de un ascensor.
 *
 * Ciclo de vida de cada tick:
 *  1. Si tiene destino → moverse un piso hacia él
 *  2. Si llegó al destino → abrir puertas (pausa)
 *  3. Si no tiene destino → pedir la siguiente solicitud de la cola
 *  4. Si la cola está vacía → quedarse INACTIVO
 *
 * Un hilo por ascensor = 9 hilos en total corriendo en paralelo.
 */
public class HiloAscensor extends Thread {

    private final Ascensor ascensor;
    private final Cola<Solicitud> colaSolicitudes;  // Cola compartida del edificio
    private final Pila<String> historialParadas;    // Pila propia del ascensor
    private final ControladorSistema controlador;   // Para notificar cambios a la UI

    private volatile boolean corriendo;             // volatile = visible entre hilos
    private volatile boolean pausado;
    private int intervaloMs;                        // Tiempo entre ticks (configurable)

    // Tiempo que permanece con puertas abiertas (3 ticks por defecto)
    private static final int TICKS_PUERTAS = 3;
    private int ticksPuertasRestantes = 0;

    public HiloAscensor(Ascensor ascensor,
                        Cola<Solicitud> colaSolicitudes,
                        Pila<String> historialParadas,
                        ControladorSistema controlador,
                        int intervaloMs) {
        this.ascensor = ascensor;
        this.colaSolicitudes = colaSolicitudes;
        this.historialParadas = historialParadas;
        this.controlador = controlador;
        this.intervaloMs = intervaloMs;
        this.corriendo = false;
        this.pausado = false;

        // Nombre del hilo para facilitar el debug
        setName("Hilo-" + ascensor.getId());
        // Daemon: si la app se cierra, los hilos mueren automáticamente
        setDaemon(true);
    }

    @Override
    public void run() {
        corriendo = true;

        while (corriendo) {
            try {
                // Respetar pausa
                synchronized (this) {
                    while (pausado) wait();
                }

                procesarTick();
                Thread.sleep(intervaloMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Lógica principal de cada tick del ascensor.
     */
    private void procesarTick() {
        switch (ascensor.getEstado()) {

            case SUBIENDO -> {
                int pisoActual = ascensor.getPisoActual();
                int pisoDestino = ascensor.getPisoDestino();

                if (pisoActual < pisoDestino) {
                    // Mover un piso hacia arriba
                    ascensor.setPisoActual(pisoActual + 1);
                    notificarCambio();
                } else {
                    // Llegó: abrir puertas
                    llegarDestino();
                }
            }

            case BAJANDO -> {
                int pisoActual = ascensor.getPisoActual();
                int pisoDestino = ascensor.getPisoDestino();

                if (pisoActual > pisoDestino) {
                    // Mover un piso hacia abajo
                    ascensor.setPisoActual(pisoActual - 1);
                    notificarCambio();
                } else {
                    llegarDestino();
                }
            }

            case ABRIENDO_PUERTAS -> {
                if (ticksPuertasRestantes > 0) {
                    ticksPuertasRestantes--;
                    notificarCambio();
                } else {
                    // Puertas cerradas: buscar siguiente solicitud
                    procesarSiguienteSolicitud();
                }
            }

            case INACTIVO -> {
                // Intentar tomar una solicitud de la cola
                procesarSiguienteSolicitud();
            }
        }
    }

    /**
     * Acciones al llegar al piso destino.
     */
    private void llegarDestino() {
        // Registrar en la pila de historial
        String registro = String.format("Ascensor %s → Piso %d",
                ascensor.getId(), ascensor.getPisoActual());
        historialParadas.apilar(registro);

        // Abrir puertas
        ascensor.setEstado(EstadoAscensor.ABRIENDO_PUERTAS);
        ticksPuertasRestantes = TICKS_PUERTAS;
        notificarCambio();
    }

    /**
     * Toma la siguiente solicitud de la cola y define el destino.
     * Sincronizado porque varios hilos comparten la misma cola.
     */
    private void procesarSiguienteSolicitud() {
        Solicitud siguiente;

        synchronized (colaSolicitudes) {
            siguiente = colaSolicitudes.desencolar();
        }

        if (siguiente != null) {
            int destino = siguiente.getPisoDestino() != -1
                    ? siguiente.getPisoDestino()
                    : siguiente.getPisoOrigen();

            ascensor.setPisoDestino(destino);

            if (destino > ascensor.getPisoActual()) {
                ascensor.setEstado(EstadoAscensor.SUBIENDO);
            } else if (destino < ascensor.getPisoActual()) {
                ascensor.setEstado(EstadoAscensor.BAJANDO);
            } else {
                // Ya está en ese piso
                llegarDestino();
            }

            notificarCambio();
        } else {
            ascensor.setEstado(EstadoAscensor.INACTIVO);
            notificarCambio();
        }
    }

    /**
     * Notifica al controlador que este ascensor cambió de estado.
     * El controlador se encarga de actualizar la UI con Platform.runLater().
     */
    private void notificarCambio() {
        if (controlador != null) {
            controlador.onAscensorActualizado(ascensor);
        }
    }

    // ===== Control del hilo =====

    public void pausar() {
        pausado = true;
    }

    public synchronized void reanudar() {
        pausado = false;
        notifyAll();
    }

    public void detener() {
        corriendo = false;
        reanudar(); // desbloquear si estaba pausado
        interrupt();
    }

    public void setIntervaloMs(int intervaloMs) {
        this.intervaloMs = intervaloMs;
    }

    public boolean isPausado() { return pausado; }
    public boolean isCorriendo() { return corriendo; }
}
