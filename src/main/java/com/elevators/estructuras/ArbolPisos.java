package com.elevators.estructuras;


/**
 * Árbol Binario de Búsqueda (BST) para indexar los pisos de un edificio.
 *
 * Usos en el sistema:
 *  1. Búsqueda eficiente: encontrar si un piso existe en O(log n)
 *  2. Permisos: marcar pisos restringidos (ej. piso ejecutivo)
 *  3. Rango: encontrar todos los pisos entre A y B (para optimizar rutas)
 *
 * Estructura del árbol para 25 pisos (raíz = piso 13, el del medio):
 *
 *              13
 *           /      \
 *          7        19
 *        /   \    /    \
 *       4    10  16    22
 *      ...  ...  ...   ...
 */
public class ArbolPisos {

    private static class Nodo {
        int piso;
        boolean restringido;     // true = acceso limitado (solo ciertos ascensores)
        String etiqueta;         // "Lobby", "Ejecutivo", "Azotea", etc.
        Nodo izquierdo;
        Nodo derecho;

        Nodo(int piso) {
            this.piso = piso;
            this.restringido = false;
            this.etiqueta = "Piso " + piso;
            this.izquierdo = null;
            this.derecho = null;
        }
    }

    private Nodo raiz;
    private int totalNodos;

    public ArbolPisos() {
        this.raiz = null;
        this.totalNodos = 0;
    }

    /**
     * Inserta un piso en el árbol.
     * Los pisos menores van a la izquierda, mayores a la derecha.
     */
    public void insertar(int piso) {
        raiz = insertarRecursivo(raiz, piso);
        totalNodos++;
    }

    private Nodo insertarRecursivo(Nodo actual, int piso) {
        if (actual == null) return new Nodo(piso);
        if (piso < actual.piso)
            actual.izquierdo = insertarRecursivo(actual.izquierdo, piso);
        else if (piso > actual.piso)
            actual.derecho = insertarRecursivo(actual.derecho, piso);
        // Si piso == actual.piso, ya existe, no hacemos nada
        return actual;
    }

    /**
     * Busca si un piso existe en el árbol. O(log n)
     */
    public boolean contiene(int piso) {
        return buscarNodo(raiz, piso) != null;
    }

    private Nodo buscarNodo(Nodo actual, int piso) {
        if (actual == null) return null;
        if (piso == actual.piso) return actual;
        if (piso < actual.piso) return buscarNodo(actual.izquierdo, piso);
        return buscarNodo(actual.derecho, piso);
    }

    /**
     * Marca un piso como restringido (acceso limitado).
     */
    public void marcarRestringido(int piso, String etiqueta) {
        Nodo nodo = buscarNodo(raiz, piso);
        if (nodo != null) {
            nodo.restringido = true;
            nodo.etiqueta = etiqueta;
        }
    }

    /**
     * Verifica si un piso es de acceso restringido.
     */
    public boolean esRestringido(int piso) {
        Nodo nodo = buscarNodo(raiz, piso);
        return nodo != null && nodo.restringido;
    }

    /**
     * Retorna la etiqueta de un piso (ej. "Lobby", "Piso 5").
     */
    public String getEtiqueta(int piso) {
        Nodo nodo = buscarNodo(raiz, piso);
        return nodo != null ? nodo.etiqueta : "Desconocido";
    }

    /**
     * Recorrido en orden (in-order): retorna los pisos de menor a mayor.
     * Útil para mostrar la lista completa de pisos en la UI.
     */
    public int[] getPisosEnOrden() {
        int[] resultado = new int[totalNodos];
        int[] indice = {0};
        inOrder(raiz, resultado, indice);
        return resultado;
    }

    private void inOrder(Nodo actual, int[] resultado, int[] indice) {
        if (actual == null) return;
        inOrder(actual.izquierdo, resultado, indice);
        resultado[indice[0]++] = actual.piso;
        inOrder(actual.derecho, resultado, indice);
    }

    /**
     * Inicializa el árbol con todos los pisos de un edificio.
     * Se insertan en orden balanceado (mitad primero) para que
     * el árbol quede balanceado y las búsquedas sean O(log n).
     */
    public static ArbolPisos crearParaEdificio(int totalPisos) {
        ArbolPisos arbol = new ArbolPisos();
        insertarBalanceado(arbol, 1, totalPisos);
        // Etiquetas especiales
        arbol.marcarRestringido(1, "Lobby / Planta baja");
        arbol.marcarRestringido(totalPisos, "Azotea");
        return arbol;
    }

    private static void insertarBalanceado(ArbolPisos arbol, int min, int max) {
        if (min > max) return;
        int medio = (min + max) / 2;
        arbol.insertar(medio);
        insertarBalanceado(arbol, min, medio - 1);
        insertarBalanceado(arbol, medio + 1, max);
    }

    public int getTotalNodos() { return totalNodos; }
}
