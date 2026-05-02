package com.elevators.estructuras;


/**
 * Pila genérica LIFO (Last In, First Out) implementada con nodos enlazados.
 *
 * Se usa para guardar el historial de paradas de cada ascensor.
 * "El último piso visitado" siempre está en el tope.
 *
 * @param <T> Tipo de dato (en nuestro caso: String con formato "Ascensor A1 → Piso 12")
 */
public class Pila<T> {

    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    private Nodo<T> tope;
    private int tamanio;
    private final int capacidadMaxima;

    public Pila(int capacidadMaxima) {
        this.tope = null;
        this.tamanio = 0;
        this.capacidadMaxima = capacidadMaxima;
    }

    /**
     * Apila un elemento en el tope.
     * Si la pila está llena, descarta el elemento más antiguo (el fondo)
     * para no perder el historial reciente.
     */
    public synchronized void apilar(T dato) {
        if (tamanio >= capacidadMaxima) {
            eliminarFondo();
        }
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = tope;
        tope = nuevo;
        tamanio++;
    }

    /**
     * Elimina y retorna el elemento del tope.
     */
    public synchronized T desapilar() {
        if (estaVacia()) return null;
        T dato = tope.dato;
        tope = tope.siguiente;
        tamanio--;
        return dato;
    }

    /**
     * Retorna el elemento del tope SIN eliminarlo.
     */
    public synchronized T verTope() {
        if (estaVacia()) return null;
        return tope.dato;
    }

    /**
     * Retorna todos los elementos como array (tope primero).
     * Se usa para mostrar el historial en la UI.
     */
    @SuppressWarnings("unchecked")
    public synchronized T[] toArray() {
        T[] array = (T[]) new Object[tamanio];
        Nodo<T> actual = tope;
        int i = 0;
        while (actual != null) {
            array[i++] = actual.dato;
            actual = actual.siguiente;
        }
        return array;
    }

    /**
     * Elimina el elemento más antiguo (el del fondo).
     * Auxiliar para cuando la pila está llena.
     */
    private void eliminarFondo() {
        if (tope == null) return;
        if (tope.siguiente == null) {
            tope = null;
            tamanio = 0;
            return;
        }
        Nodo<T> actual = tope;
        while (actual.siguiente.siguiente != null) {
            actual = actual.siguiente;
        }
        actual.siguiente = null;
        tamanio--;
    }

    public synchronized boolean estaVacia() { return tamanio == 0; }
    public synchronized int getTamanio() { return tamanio; }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder("Pila(tope→fondo)[");
        Nodo<T> actual = tope;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) sb.append(" | ");
            actual = actual.siguiente;
        }
        sb.append("]");
        return sb.toString();
    }


}
