package com.elevators.algoritmos;

import com.elevators.modelo.Solicitud;

/**
 * Implementación de Mergesort para ordenar solicitudes por prioridad.
 *
 * Estrategia: divide el arreglo por la mitad recursivamente hasta
 * tener subarreglos de 1 elemento, luego los fusiona en orden.
 *
 * Complejidad:
 *  - Siempre O(n log n) — más predecible que Quicksort
 *  - Memoria: O(n) — necesita arreglo auxiliar para el merge
 *  - Es estable: solicitudes con igual prioridad mantienen su orden original
 */
public class Mergesort {

    public static void ordenar(Solicitud[] solicitudes) {
        if (solicitudes == null || solicitudes.length <= 1) return;
        Solicitud[] auxiliar = new Solicitud[solicitudes.length];
        mergesort(solicitudes, auxiliar, 0, solicitudes.length - 1);
    }

    private static void mergesort(Solicitud[] arr, Solicitud[] aux,
                                  int izquierda, int derecha) {
        if (izquierda >= derecha) return;

        int medio = izquierda + (derecha - izquierda) / 2;

        // Dividir en dos mitades y ordenar cada una
        mergesort(arr, aux, izquierda, medio);
        mergesort(arr, aux, medio + 1, derecha);

        // Fusionar las dos mitades ya ordenadas
        fusionar(arr, aux, izquierda, medio, derecha);
    }

    /**
     * Fusiona dos subarreglos ya ordenados en uno solo.
     * arr[izquierda..medio] y arr[medio+1..derecha]
     */
    private static void fusionar(Solicitud[] arr, Solicitud[] aux,
                                 int izquierda, int medio, int derecha) {
        // Copiar al auxiliar
        for (int k = izquierda; k <= derecha; k++) {
            aux[k] = arr[k];
        }

        int i = izquierda;      // Puntero mitad izquierda
        int j = medio + 1;      // Puntero mitad derecha
        int k = izquierda;      // Puntero resultado

        while (i <= medio && j <= derecha) {
            // Comparar prioridades: el menor va primero
            if (aux[i].getPrioridad() <= aux[j].getPrioridad()) {
                arr[k++] = aux[i++];
            } else {
                arr[k++] = aux[j++];
            }
        }

        // Copiar los elementos restantes de la mitad izquierda (si quedan)
        while (i <= medio) {
            arr[k++] = aux[i++];
        }
        // Los de la derecha ya están en su lugar, no hace falta copiarlos
    }
}
