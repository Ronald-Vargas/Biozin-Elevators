package com.elevators.algoritmos;

import com.elevators.modelo.Solicitud;

/**
 * Implementación de Heapsort para ordenar solicitudes por prioridad.
 *
 * Estrategia: convierte el arreglo en un Max-Heap (árbol binario donde
 * el padre siempre es mayor que sus hijos). Luego extrae el máximo
 * repetidamente y lo coloca al final.
 *
 * Complejidad:
 *  - Siempre O(n log n)
 *  - Memoria: O(1) — ordena in-place, sin arreglo auxiliar
 *  - No estable: solicitudes con igual prioridad pueden cambiar de orden
 */

public class Heapsort {

    public static void ordenar(Solicitud[] solicitudes) {
        if (solicitudes == null || solicitudes.length <= 1) return;
        int n = solicitudes.length;

        // Fase 1: construir el Max-Heap
        // Empezamos desde el último nodo interno y "hundimos" hacia abajo
        for (int i = n / 2 - 1; i >= 0; i--) {
            hundir(solicitudes, n, i);
        }

        // Fase 2: extraer elementos del heap uno por uno
        for (int i = n - 1; i > 0; i--) {
            // El máximo siempre está en la raíz (índice 0)
            // Lo movemos al final del arreglo no ordenado
            intercambiar(solicitudes, 0, i);

            // Restaurar la propiedad del heap en el subarreglo reducido
            hundir(solicitudes, i, 0);
        }
    }

    /**
     * Hunde el elemento en la posición 'indice' hacia abajo en el heap
     * hasta que esté en la posición correcta.
     *
     * @param n       tamaño del heap activo (va reduciéndose en la fase 2)
     * @param indice  posición del elemento a hundir
     */
    private static void hundir(Solicitud[] arr, int n, int indice) {
        int mayor = indice;
        int hijoIzq = 2 * indice + 1;   // Hijo izquierdo en árbol implícito
        int hijoDer = 2 * indice + 2;   // Hijo derecho en árbol implícito

        // ¿El hijo izquierdo es mayor que el padre?
        if (hijoIzq < n &&
                arr[hijoIzq].getPrioridad() > arr[mayor].getPrioridad()) {
            mayor = hijoIzq;
        }

        // ¿El hijo derecho es mayor que el mayor actual?
        if (hijoDer < n &&
                arr[hijoDer].getPrioridad() > arr[mayor].getPrioridad()) {
            mayor = hijoDer;
        }

        // Si el mayor no es el padre, intercambiar y continuar hundiendo
        if (mayor != indice) {
            intercambiar(arr, indice, mayor);
            hundir(arr, n, mayor);
        }
    }

    private static void intercambiar(Solicitud[] arr, int i, int j) {
        Solicitud temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
