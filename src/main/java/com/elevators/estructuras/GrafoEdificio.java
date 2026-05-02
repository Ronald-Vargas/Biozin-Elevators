package com.elevators.estructuras;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Grafo no dirigido que modela el edificio como red de nodos y aristas.
 *
 * - Nodo = piso del edificio (1 a 25)
 * - Arista = ruta válida entre dos pisos (peso = tiempo de viaje en segundos)
 *
 * Implementado con lista de adyacencia para ser eficiente en memoria.
 *
 * Uso principal: calcular la ruta más corta entre el piso actual
 * del ascensor y el piso destino (BFS para distancia mínima).
 *
 *  Piso 25 ── Piso 24 ── Piso 23 ── ... ── Piso 2 ── Piso 1
 *    (cada piso conectado con el anterior y el siguiente)
 */
public class GrafoEdificio {

    private static class Arista {
        int pisoDestino;
        int peso;          // Tiempo estimado de viaje en "ticks" de simulación

        Arista(int pisoDestino, int peso) {
            this.pisoDestino = pisoDestino;
            this.peso = peso;
        }
    }

    private final int totalPisos;
    private final List<List<Arista>> listaAdyacencia;

    public GrafoEdificio(int totalPisos) {
        this.totalPisos = totalPisos;
        this.listaAdyacencia = new ArrayList<>();

        // Inicializar lista de adyacencia para cada piso (índice 0 = piso 1)
        for (int i = 0; i <= totalPisos; i++) {
            listaAdyacencia.add(new ArrayList<>());
        }
    }

    /**
     * Conecta dos pisos con una arista bidireccional.
     * @param pisoA  primer piso
     * @param pisoB  segundo piso
     * @param peso   tiempo de viaje entre ellos
     */
    public void conectar(int pisoA, int pisoB, int peso) {
        listaAdyacencia.get(pisoA).add(new Arista(pisoB, peso));
        listaAdyacencia.get(pisoB).add(new Arista(pisoA, peso));
    }

    /**
     * Retorna el número mínimo de pisos a recorrer entre origen y destino.
     * Usa BFS (Breadth-First Search) — garantiza la distancia mínima.
     */
    public int distanciaMinima(int origen, int destino) {
        if (origen == destino) return 0;

        boolean[] visitado = new boolean[totalPisos + 1];
        int[] distancia = new int[totalPisos + 1];
        Queue<Integer> cola = new LinkedList<>();

        visitado[origen] = true;
        cola.add(origen);

        while (!cola.isEmpty()) {
            int actual = cola.poll();

            for (Arista arista : listaAdyacencia.get(actual)) {
                if (!visitado[arista.pisoDestino]) {
                    visitado[arista.pisoDestino] = true;
                    distancia[arista.pisoDestino] = distancia[actual] + 1;

                    if (arista.pisoDestino == destino) {
                        return distancia[arista.pisoDestino];
                    }
                    cola.add(arista.pisoDestino);
                }
            }
        }
        return -1; // No hay ruta (no debería pasar en un edificio normal)
    }

    /**
     * Retorna la ruta completa de pisos entre origen y destino.
     * Se usa para mostrar la ruta planificada del ascensor en la UI.
     */
    public List<Integer> rutaCompleta(int origen, int destino) {
        boolean[] visitado = new boolean[totalPisos + 1];
        int[] anterior = new int[totalPisos + 1];
        Queue<Integer> cola = new LinkedList<>();

        for (int i = 0; i <= totalPisos; i++) anterior[i] = -1;

        visitado[origen] = true;
        cola.add(origen);

        while (!cola.isEmpty()) {
            int actual = cola.poll();
            if (actual == destino) break;

            for (Arista arista : listaAdyacencia.get(actual)) {
                if (!visitado[arista.pisoDestino]) {
                    visitado[arista.pisoDestino] = true;
                    anterior[arista.pisoDestino] = actual;
                    cola.add(arista.pisoDestino);
                }
            }
        }

        // Reconstruir la ruta de atrás hacia adelante
        List<Integer> ruta = new ArrayList<>();
        for (int p = destino; p != -1; p = anterior[p]) {
            ruta.add(0, p); // insertar al principio
        }
        return ruta;
    }

    /**
     * Construye el grafo estándar de un edificio:
     * cada piso conectado con el piso de arriba y de abajo.
     * Peso = 1 tick de simulación por piso.
     */
    public static GrafoEdificio crearParaEdificio(int totalPisos) {
        GrafoEdificio grafo = new GrafoEdificio(totalPisos);
        for (int i = 1; i < totalPisos; i++) {
            grafo.conectar(i, i + 1, 1);
        }
        return grafo;
    }

    public int getTotalPisos() { return totalPisos; }

    public List<Arista> getVecinos(int piso) {
        return listaAdyacencia.get(piso);
    }
}
