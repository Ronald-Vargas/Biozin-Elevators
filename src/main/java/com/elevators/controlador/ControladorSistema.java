package com.elevators.controlador;

import com.elevators.algoritmos.GestorOrdenamiento;
import com.elevators.estructuras.ArbolPisos;
import com.elevators.estructuras.Cola;
import com.elevators.estructuras.GrafoEdificio;
import com.elevators.estructuras.Pila;
import com.elevators.modelo.Ascensor;
import com.elevators.modelo.Direccion;
import com.elevators.modelo.Edificio;
import com.elevators.modelo.Solicitud;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Controlador central del sistema.
 *
 * Responsabilidades:
 *  - Inicializar los 3 edificios, 9 ascensores, colas y estructuras
 *  - Arrancar / pausar / detener los 9 hilos
 *  - Recibir nuevas solicitudes y asignarlas al ascensor correcto
 *  - Notificar a la UI cuando algo cambia (via callbacks)
 *
 * Es un Singleton: solo existe una instancia durante toda la ejecución.
 */
public class ControladorSistema {

    // ===== Singleton =====
    private static ControladorSistema instancia;

    public static ControladorSistema getInstance() {
        if (instancia == null) {
            instancia = new ControladorSistema();
        }
        return instancia;
    }

    // ===== Datos del sistema =====
    private final List<Edificio> edificios;

    // Una cola de solicitudes por ascensor (clave = id del ascensor "A1", "B2", etc.)
    private final Map<String, Cola<Solicitud>> colasAscensor;

    // Una pila de historial por ascensor (clave = id del ascensor "A1", "B2", etc.)
    private final Map<String, Pila<String>> historiales;

    // Un árbol de pisos por edificio
    private final Map<Integer, ArbolPisos> arboles;

    // Un grafo por edificio
    private final Map<Integer, GrafoEdificio> grafos;

    // Los 9 hilos de los ascensores
    private final List<HiloAscensor> hilos;

    // Intervalo de los hilos en milisegundos (configurable desde la UI)
    private int intervaloMs = 1000;

    // ===== Callbacks hacia la UI =====
    // La UI registra estas funciones y el controlador las llama
    // cuando hay cambios, usando Platform.runLater para thread safety

    private Consumer<Ascensor> onAscensorCambio;       // Un ascensor se movió
    private Consumer<Integer> onColaCambio;            // La cola de un edificio cambió
    private Runnable onHistorialCambio;                // El historial de paradas cambió
    private Consumer<Integer> onTotalViajesCambio;     // Contador de viajes

    private int totalViajes = 0;

    // ===== Constructor privado (Singleton) =====
    private ControladorSistema() {
        edificios     = new ArrayList<>();
        colasAscensor = new HashMap<>();
        historiales   = new HashMap<>();
        arboles       = new HashMap<>();
        grafos        = new HashMap<>();
        hilos         = new ArrayList<>();
        inicializar();
    }

    /**
     * Construye los 3 edificios con sus ascensores, colas y estructuras.
     */
    private void inicializar() {
        String[] nombres = {"Edificio 1", "Edificio 2", "Edificio 3"};

        for (int i = 1; i <= 3; i++) {
            // Modelo del edificio
            Edificio edificio = new Edificio(i, nombres[i - 1]);
            edificios.add(edificio);

            // Árbol de pisos balanceado
            arboles.put(i, ArbolPisos.crearParaEdificio(Edificio.TOTAL_PISOS));

            // Grafo del edificio
            grafos.put(i, GrafoEdificio.crearParaEdificio(Edificio.TOTAL_PISOS));

            // Un hilo por ascensor, cada uno con su propia cola
            for (Ascensor ascensor : edificio.getAscensores()) {
                // Cola exclusiva del ascensor (máx 20 por requisito del proyecto)
                Cola<Solicitud> cola = new Cola<>(Edificio.CAPACIDAD_MAXIMA_SOLICITUDES);
                colasAscensor.put(ascensor.getId(), cola);

                Pila<String> historial = new Pila<>(50);
                historiales.put(ascensor.getId(), historial);

                HiloAscensor hilo = new HiloAscensor(
                        ascensor,
                        cola,
                        historial,
                        this,
                        intervaloMs
                );
                hilos.add(hilo);
            }
        }
    }

    // ===== Control de la simulación =====

    /**
     * Arranca todos los hilos. Solo se puede llamar una vez por hilo.
     */
    public void iniciar() {
        for (HiloAscensor hilo : hilos) {
            if (!hilo.isCorriendo()) {
                hilo.start();
            }
        }
    }

    public void pausar() {
        hilos.forEach(HiloAscensor::pausar);
    }

    public void reanudar() {
        hilos.forEach(HiloAscensor::reanudar);
    }

    /**
     * Detiene todos los hilos permanentemente.
     * Para volver a simular habría que recrear el controlador.
     */
    public void detener() {
        hilos.forEach(HiloAscensor::detener);
    }

    /**
     * Cambia el intervalo de todos los hilos en tiempo real.
     */
    public void setIntervaloMs(int intervaloMs) {
        this.intervaloMs = intervaloMs;
        hilos.forEach(h -> h.setIntervaloMs(intervaloMs));
    }

    /**
     * Vacía las colas de todos los ascensores de un edificio específico.
     */
    public void destruirSolicitudes(int edificioId) {
        Edificio edificio = getEdificio(edificioId);
        if (edificio == null) return;
        for (Ascensor ascensor : edificio.getAscensores()) {
            Cola<Solicitud> cola = colasAscensor.get(ascensor.getId());
            if (cola != null) {
                while (!cola.estaVacia()) cola.desencolar();
            }
        }
        notificarColaCambio(edificioId);
    }

    /**
     * Vacía las colas de todos los edificios.
     */
    public void destruirTodasLasSolicitudes() {
        for (int i = 1; i <= 3; i++) destruirSolicitudes(i);
    }

    // ===== Gestión de solicitudes =====

    /**
     * Registra una nueva solicitud desde el lobby (vista pasajero).
     * Asigna el ascensor más adecuado y encola la solicitud en su cola exclusiva.
     *
     * @return el ascensor asignado, o null si el edificio está al máximo de capacidad
     */
    public Ascensor registrarSolicitud(int edificioId,
                                       int pisoOrigen,
                                       int pisoDestino,
                                       Direccion direccion) {
        Edificio edificio = getEdificio(edificioId);
        if (edificio == null) return null;

        // Verificar capacidad total del edificio (máx 20 solicitudes en total)
        if (getTamanioColaEdificio(edificioId) >= Edificio.CAPACIDAD_MAXIMA_SOLICITUDES) return null;

        Solicitud solicitud = new Solicitud(edificioId, pisoOrigen, pisoDestino, direccion);

        // Seleccionar el mejor ascensor
        Ascensor asignado = AsignadorAscensor.seleccionar(edificio.getAscensores(), solicitud);
        if (asignado == null) return null;

        // Calcular prioridad según distancia del ascensor asignado
        GestorOrdenamiento.calcularPrioridad(solicitud, asignado.getPisoActual());

        // Encolar en la cola exclusiva del ascensor asignado
        Cola<Solicitud> colaAsignado = colasAscensor.get(asignado.getId());
        if (colaAsignado == null) return null;
        colaAsignado.encolar(solicitud);

        // Re-ordenar la cola del ascensor asignado
        GestorOrdenamiento.ordenarCola(colaAsignado, edificio.getTipoOrdenamiento());

        notificarColaCambio(edificioId);
        return asignado;
    }

    /**
     * Versión simplificada: solicitud solo con origen y dirección
     * (el pasajero aún no eligió destino).
     */
    public Ascensor registrarSolicitud(int edificioId,
                                       int pisoOrigen,
                                       Direccion direccion) {
        return registrarSolicitud(edificioId, pisoOrigen, pisoOrigen, direccion);
    }

    /**
     * Registra una solicitud directamente en la cola del ascensor especificado.
     * Usado desde VistaAscensor cuando el pasajero selecciona el piso destino.
     */
    public void registrarSolicitudParaAscensor(String ascensorId,
                                               int pisoOrigen,
                                               int pisoDestino,
                                               Direccion direccion) {
        Cola<Solicitud> cola = colasAscensor.get(ascensorId);
        if (cola == null || cola.estaLlena()) return;

        // Buscar edificioId desde el ascensor
        int edificioId = -1;
        for (Edificio e : edificios) {
            for (Ascensor a : e.getAscensores()) {
                if (a.getId().equals(ascensorId)) {
                    edificioId = e.getId();
                    break;
                }
            }
            if (edificioId != -1) break;
        }

        Solicitud solicitud = new Solicitud(edificioId, pisoOrigen, pisoDestino, direccion);
        cola.encolar(solicitud);
        notificarColaCambio(edificioId);
    }

    /**
     * Cambia el algoritmo de ordenamiento de un edificio y re-ordena las colas de sus ascensores.
     */
    public void setAlgoritmoEdificio(int edificioId, String algoritmo) {
        Edificio edificio = getEdificio(edificioId);
        if (edificio != null) {
            edificio.setTipoOrdenamiento(algoritmo);
            for (Ascensor ascensor : edificio.getAscensores()) {
                Cola<Solicitud> cola = colasAscensor.get(ascensor.getId());
                if (cola != null) {
                    GestorOrdenamiento.ordenarCola(cola, algoritmo);
                }
            }
            notificarColaCambio(edificioId);
        }
    }

    // ===== Callbacks desde los hilos hacia la UI =====

    /**
     * Llamado por cada HiloAscensor cuando su ascensor cambia de estado.
     * Usa Platform.runLater para que la UI se actualice en el hilo correcto.
     */
    public void onAscensorActualizado(Ascensor ascensor) {
        // Contar viajes completados (cada vez que llega a un destino)
        if (ascensor.getEstado() ==
                com.elevators.modelo.EstadoAscensor.ABRIENDO_PUERTAS) {
            totalViajes++;
            Platform.runLater(() -> {
                if (onTotalViajesCambio != null) onTotalViajesCambio.accept(totalViajes);
            });
        }

        Platform.runLater(() -> {
            if (onAscensorCambio != null) onAscensorCambio.accept(ascensor);
            if (onHistorialCambio != null) onHistorialCambio.run();
        });
    }

    private void notificarColaCambio(int edificioId) {
        Platform.runLater(() -> {
            if (onColaCambio != null) onColaCambio.accept(edificioId);
        });
    }

    // ===== Registro de callbacks desde la UI =====

    public void setOnAscensorCambio(Consumer<Ascensor> callback) {
        this.onAscensorCambio = callback;
    }

    public void setOnColaCambio(Consumer<Integer> callback) {
        this.onColaCambio = callback;
    }

    public void setOnHistorialCambio(Runnable callback) {
        this.onHistorialCambio = callback;
    }

    public void setOnTotalViajesCambio(Consumer<Integer> callback) {
        this.onTotalViajesCambio = callback;
    }

    // ===== Getters =====

    public List<Edificio> getEdificios() { return edificios; }

    public Edificio getEdificio(int id) {
        return edificios.stream()
                .filter(e -> e.getId() == id)
                .findFirst().orElse(null);
    }

    /**
     * Retorna el total de solicitudes pendientes en los 3 ascensores de un edificio.
     */
    public int getTamanioColaEdificio(int edificioId) {
        Edificio edificio = getEdificio(edificioId);
        if (edificio == null) return 0;
        return edificio.getAscensores().stream()
                .mapToInt(a -> {
                    Cola<Solicitud> c = colasAscensor.get(a.getId());
                    return c != null ? c.getTamanio() : 0;
                }).sum();
    }

    public Cola<Solicitud> getCola(String ascensorId) {
        return colasAscensor.get(ascensorId);
    }

    public Pila<String> getHistorial(String ascensorId) {
        return historiales.get(ascensorId);
    }

    public ArbolPisos getArbol(int edificioId) {
        return arboles.get(edificioId);
    }

    public GrafoEdificio getGrafo(int edificioId) {
        return grafos.get(edificioId);
    }

    public int getTotalViajes() { return totalViajes; }
    public int getIntervaloMs() { return intervaloMs; }
}
