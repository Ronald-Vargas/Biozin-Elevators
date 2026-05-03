package com.elevators.estructuras;


/**
 * Cola genérica FIFO (First In, First Out) implementada con nodos enlazados.
 *
 * Se usa para manejar las solicitudes pendientes de cada edificio.
 * Capacidad máxima: 20 solicitudes por edificio (requisito del proyecto).
 *
 * Thread-safe: todos los métodos están sincronizados porque varios hilos
 * (uno por ascensor) pueden intentar tomar solicitudes al mismo tiempo.
 *
 * @param <T> Tipo de dato que almacena la cola (en nuestro caso: Solicitud)
 */
public class Cola<T> {

    // ===== Nodo interno =====
    // Cada elemento de la cola vive en un nodo que apunta al siguiente
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    private Nodo<T> frente;      // Primer elemento (el que sale primero)
    private Nodo<T> final_;      // Último elemento (el que entró último)
    private int tamanio;
    private final int capacidadMaxima;

    public Cola(int capacidadMaxima) {
        this.frente = null;
        this.final_ = null;
        this.tamanio = 0;
        this.capacidadMaxima = capacidadMaxima;
    }

    /**
     * Agrega un elemento al final de la cola.
     * @return true si se agregó, false si la cola está llena
     */
    public synchronized boolean encolar(T dato) {
        if (tamanio >= capacidadMaxima) return false;

        Nodo<T> nuevo = new Nodo<>(dato);

        if (estaVacia()) {
            frente = nuevo;
            final_ = nuevo;
        } else {
            final_.siguiente = nuevo;
            final_ = nuevo;
        }
        tamanio++;
        return true;
    }

    /**
     * Elimina y retorna el primer elemento de la cola.
     * @return el elemento del frente, o null si está vacía
     */
    public synchronized T desencolar() {
        if (estaVacia()) return null;

        T dato = frente.dato;
        frente = frente.siguiente;

        if (frente == null) final_ = null;

        tamanio--;
        return dato;
    }

    /**
     * Retorna el primer elemento SIN eliminarlo.
     */
    public synchronized T verFrente() {
        if (estaVacia()) return null;
        return frente.dato;
    }

    /**
     * Retorna todos los elementos como array para los algoritmos de ordenamiento.
     * No modifica la cola.
     */
    @SuppressWarnings("unchecked")
    public synchronized Object[] toArray() {
        Object[] array = new Object[tamanio];
        Nodo<T> actual = frente;
        int i = 0;
        while (actual != null) {
            array[i++] = actual.dato;
            actual = actual.siguiente;
        }
        return array;
    }

    /**
     * Reconstruye la cola a partir de un array (después de ordenar).
     */
    public synchronized void reconstruirDesdeArray(Object[] array) {
        frente = null;
        final_ = null;
        tamanio = 0;
        for (Object dato : array) {
            @SuppressWarnings("unchecked")
            T elemento = (T) dato;
            encolar(elemento);
        }
    }

    public synchronized boolean estaVacia() { return tamanio == 0; }
    public synchronized boolean estaLlena() { return tamanio >= capacidadMaxima; }
    public synchronized int getTamanio() { return tamanio; }
    public synchronized int getCapacidadMaxima() { return capacidadMaxima; }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder("Cola[");
        Nodo<T> actual = frente;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) sb.append(" → ");
            actual = actual.siguiente;
        }
        sb.append("]");
        return sb.toString();
    }
}
