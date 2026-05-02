package com.elevators.algoritmos;


import com.elevators.modelo.Solicitud;

/**
 * Implementación de Quicksort para ordenar solicitudes por prioridad.
 *
 * Estrategia: divide el arreglo en dos mitades usando un pivote.
 * Los elementos menores al pivote van a la izquierda,
 * los mayores a la derecha. Se aplica recursivamente.
 *
 * Complejidad:
 *  - Promedio: O(n log n)
 *  - Peor caso: O(n²) cuando el arreglo ya está ordenado
 *  - Memoria: O(log n) en la pila de recursión
 */

public class Quicksort {

    /**
     * Punto de entrada. Ordena el arreglo de solicitudes de menor
     * a mayor prioridad (menor número = mayor urgencia).
     */
    public static void ordenar(Solicitud[] solicitudes) {
        if (solicitudes == null || solicitudes.length <= 1) return;
        quicksort(solicitudes, 0, solicitudes.length - 1);
    }

    private static void quicksort(Solicitud[] arr, int izquierda, int derecha) {
        if (izquierda >= derecha) return;

        int indicePivote = particionar(arr, izquierda, derecha);

        // Ordenar recursivamente ambas mitades
        quicksort(arr, izquierda, indicePivote - 1);
        quicksort(arr, indicePivote + 1, derecha);
    }

    /**
     * Mueve el pivote a su posición final y deja los menores
     * a la izquierda y los mayores a la derecha.
     * Usamos el elemento del medio como pivote para evitar el peor caso
     * cuando el arreglo ya viene ordenado.
     */
    private static int particionar(Solicitud[] arr, int izquierda, int derecha) {
        // Elegir el pivote del medio y moverlo al final
        int medio = izquierda + (derecha - izquierda) / 2;
        intercambiar(arr, medio, derecha);

        int pivote = arr[derecha].getPrioridad();
        int i = izquierda - 1;

        for (int j = izquierda; j < derecha; j++) {
            if (arr[j].getPrioridad() <= pivote) {
                i++;
                intercambiar(arr, i, j);
            }
        }

        // Colocar el pivote en su posición correcta
        intercambiar(arr, i + 1, derecha);
        return i + 1;
    }

    private static void intercambiar(Solicitud[] arr, int i, int j) {
        Solicitud temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
