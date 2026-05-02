package com.elevators.modelo;


import java.util.ArrayList;
import java.util.List;

/**
 * Representa uno de los 3 edificios del sistema.
 * Cada edificio contiene:
 *  - 25 pisos
 *  - 3 ascensores
 *  - Sus propias colas de solicitudes
 *  - Su algoritmo de ordenamiento configurable
 *
 * Esta clase es solo el "contenedor": las colas, ascensores y solicitudes
 * se gestionan en otras clases del paquete `controlador`.
 */
public class Edificio {

    public static final int TOTAL_PISOS = 25;
    public static final int TOTAL_ASCENSORES = 3;
    public static final int CAPACIDAD_MAXIMA_SOLICITUDES = 20;

    private final int id;                  // 1, 2 o 3
    private final String nombre;           // "Edificio 1", etc.
    private final List<Ascensor> ascensores;
    private String tipoOrdenamiento;       // "Quicksort", "Mergesort" o "Heapsort"

    public Edificio(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.tipoOrdenamiento = "Quicksort"; // Por defecto
        this.ascensores = new ArrayList<>();

        // Crear los 3 ascensores con prefijos según el edificio:
        // Edificio 1 → A1, A2, A3
        // Edificio 2 → B1, B2, B3
        // Edificio 3 → C1, C2, C3
        char prefijo = (char) ('A' + id - 1);
        for (int i = 1; i <= TOTAL_ASCENSORES; i++) {
            String idAscensor = prefijo + String.valueOf(i);
            // Posición inicial: distribuidos en distintos pisos para que no estén todos juntos
            int pisoInicial = (i - 1) * (TOTAL_PISOS / TOTAL_ASCENSORES) + 1;
            ascensores.add(new Ascensor(idAscensor, id, pisoInicial));
        }
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public List<Ascensor> getAscensores() { return ascensores; }

    public String getTipoOrdenamiento() { return tipoOrdenamiento; }
    public void setTipoOrdenamiento(String tipoOrdenamiento) {
        this.tipoOrdenamiento = tipoOrdenamiento;
    }

    @Override
    public String toString() {
        return String.format("%s [id=%d, %d pisos, %d ascensores, ordenamiento=%s]",
                nombre, id, TOTAL_PISOS, TOTAL_ASCENSORES, tipoOrdenamiento);
    }
}
