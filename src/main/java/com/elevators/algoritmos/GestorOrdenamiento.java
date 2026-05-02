package com.elevators.algoritmos;

import com.elevators.estructuras.Cola;
import com.elevators.modelo.Solicitud;

/**
 * Gestor que aplica el algoritmo de ordenamiento correcto
 * sobre la cola de solicitudes de un edificio.
 *
 * Flujo:
 *  1. Extrae todos los elementos de la cola como arreglo
 *  2. Aplica el algoritmo seleccionado
 *  3. Reconstruye la cola en el nuevo orden
 *
 * Esto permite cambiar el algoritmo en tiempo de ejecución
 * sin modificar la lógica del resto del sistema.
 */
public class GestorOrdenamiento {

    public static final String QUICKSORT  = "Quicksort";
    public static final String MERGESORT  = "Mergesort";
    public static final String HEAPSORT   = "Heapsort";

    /**
     * Ordena la cola de solicitudes usando el algoritmo especificado.
     *
     * @param cola      cola de solicitudes del edificio
     * @param algoritmo nombre del algoritmo ("Quicksort", "Mergesort", "Heapsort")
     */
    public static void ordenarCola(Cola<Solicitud> cola, String algoritmo) {
        if (cola == null || cola.estaVacia()) return;

        // 1. Extraer como arreglo
        Solicitud[] solicitudes = cola.toArray();

        // 2. Aplicar el algoritmo correspondiente
        switch (algoritmo) {
            case QUICKSORT  -> Quicksort.ordenar(solicitudes);
            case MERGESORT  -> Mergesort.ordenar(solicitudes);
            case HEAPSORT   -> Heapsort.ordenar(solicitudes);
            default         -> Quicksort.ordenar(solicitudes); // fallback
        }

        // 3. Reconstruir la cola con el nuevo orden
        cola.reconstruirDesdeArray(solicitudes);
    }

    /**
     * Calcula la prioridad de una solicitud según la posición del ascensor.
     * A menor distancia, mayor prioridad (número más bajo).
     *
     * Se llama antes de encolar para que el ordenamiento tenga sentido.
     */
    public static void calcularPrioridad(Solicitud solicitud, int pisoAscensorMasCercano) {
        int distancia = Math.abs(solicitud.getPisoOrigen() - pisoAscensorMasCercano);
        solicitud.setPrioridad(distancia);
    }
}
