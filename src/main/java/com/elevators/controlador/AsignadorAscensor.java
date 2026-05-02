package com.elevators.controlador;

import com.elevators.modelo.Ascensor;
import com.elevators.modelo.Direccion;
import com.elevators.modelo.EstadoAscensor;
import com.elevators.modelo.Solicitud;
import java.util.List;

/**
 * Decide qué ascensor debe atender una solicitud específica.
 *
 * Criterios de selección (en orden de prioridad):
 *
 *  1. Ascensor en ruta compatible:
 *     Si un ascensor ya va en la misma dirección que la solicitud
 *     y está más cerca que el destino actual, lo tomamos.
 *     Ej: ascensor subiendo desde piso 3, solicitud en piso 7 dirección SUBIR → perfecto.
 *
 *  2. Ascensor más cercano inactivo:
 *     Si no hay ninguno en ruta, elegimos el que está más cerca y está INACTIVO.
 *
 *  3. Balanceo de carga:
 *     Si hay empate en distancia, elegimos el que tiene menos solicitudes
 *     pendientes asignadas (para no sobrecargar uno solo).
 */
public class AsignadorAscensor {

    /**
     * Selecciona el mejor ascensor para atender una solicitud.
     *
     * @param ascensores  lista de ascensores del edificio
     * @param solicitud   solicitud a asignar
     * @return el ascensor seleccionado, o null si no hay ninguno disponible
     */
    public static Ascensor seleccionar(List<Ascensor> ascensores,
                                       Solicitud solicitud) {
        Ascensor mejorEnRuta    = null;
        Ascensor mejorInactivo  = null;
        int menorDistanciaRuta  = Integer.MAX_VALUE;
        int menorDistanciaInact = Integer.MAX_VALUE;

        for (Ascensor ascensor : ascensores) {
            int distancia = ascensor.distanciaA(solicitud.getPisoOrigen());

            // Criterio 1: en ruta compatible
            if (estaEnRutaCompatible(ascensor, solicitud)) {
                if (distancia < menorDistanciaRuta) {
                    menorDistanciaRuta = distancia;
                    mejorEnRuta = ascensor;
                }
            }

            // Criterio 2: inactivo más cercano
            if (ascensor.getEstado() == EstadoAscensor.INACTIVO) {
                if (distancia < menorDistanciaInact) {
                    menorDistanciaInact = distancia;
                    mejorInactivo = ascensor;
                }
            }
        }

        // Prioridad: en ruta > inactivo > cualquiera (el más cercano en general)
        if (mejorEnRuta != null)   return mejorEnRuta;
        if (mejorInactivo != null) return mejorInactivo;
        return ascensorMasCercano(ascensores, solicitud);
    }

    /**
     * Verifica si un ascensor está en ruta compatible con una solicitud.
     *
     * Compatible significa:
     *  - Va en la misma dirección que la solicitud (ambos SUBIR o ambos BAJAR)
     *  - El piso de la solicitud está "por delante" del ascensor en su ruta
     */
    private static boolean estaEnRutaCompatible(Ascensor ascensor,
                                                Solicitud solicitud) {
        EstadoAscensor estado = ascensor.getEstado();
        int pisoActual = ascensor.getPisoActual();
        int pisoSolicitud = solicitud.getPisoOrigen();

        if (solicitud.getDireccion() == Direccion.SUBIR
                && estado == EstadoAscensor.SUBIENDO
                && pisoSolicitud >= pisoActual) {
            return true;
        }

        if (solicitud.getDireccion() == Direccion.BAJAR
                && estado == EstadoAscensor.BAJANDO
                && pisoSolicitud <= pisoActual) {
            return true;
        }

        return false;
    }

    /**
     * Fallback: retorna simplemente el ascensor más cercano al piso origen,
     * sin importar su estado. Usado cuando ninguno está inactivo ni en ruta.
     */
    private static Ascensor ascensorMasCercano(List<Ascensor> ascensores,
                                               Solicitud solicitud) {
        Ascensor mejor = null;
        int menorDistancia = Integer.MAX_VALUE;

        for (Ascensor ascensor : ascensores) {
            int distancia = ascensor.distanciaA(solicitud.getPisoOrigen());
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                mejor = ascensor;
            }
        }
        return mejor;
    }
}
