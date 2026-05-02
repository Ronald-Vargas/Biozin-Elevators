package com.elevators.modelo;


/**
 * Estados posibles de un ascensor durante la simulación.
 * Cada estado se renderiza con un color específico en la UI:
 *  - SUBIENDO: verde
 *  - BAJANDO: azul
 *  - ABRIENDO_PUERTAS: amarillo
 *  - INACTIVO: gris (sin solicitudes pendientes)
 */
public enum EstadoAscensor {
    SUBIENDO,
    BAJANDO,
    ABRIENDO_PUERTAS,
    INACTIVO
}
