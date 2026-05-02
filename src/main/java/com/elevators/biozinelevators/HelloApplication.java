package com.elevators.biozinelevators;

import com.elevators.estructuras.ArbolPisos;
import com.elevators.estructuras.Cola;
import com.elevators.estructuras.GrafoEdificio;
import com.elevators.estructuras.Pila;
import com.elevators.modelo.Ascensor;
import com.elevators.modelo.Direccion;
import com.elevators.modelo.Edificio;
import com.elevators.modelo.Solicitud;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        // ===== Prueba temporal del modelo =====
        System.out.println("=== Prueba del modelo ===");

        Edificio edificio1 = new Edificio(1, "Edificio 1");
        Edificio edificio2 = new Edificio(2, "Edificio 2");
        Edificio edificio3 = new Edificio(3, "Edificio 3");

        System.out.println(edificio1);
        System.out.println(edificio2);
        System.out.println(edificio3);

        System.out.println("\n--- Ascensores del Edificio 1 ---");
        for (Ascensor a : edificio1.getAscensores()) {
            System.out.println(a);
        }

        System.out.println("\n--- Solicitudes de prueba ---");
        Solicitud s1 = new Solicitud(1, 5, Direccion.SUBIR);
        Solicitud s2 = new Solicitud(2, 18, 3, Direccion.BAJAR);
        System.out.println(s1);
        System.out.println(s2);


        System.out.println("=== Prueba de estructuras ===");

        // Cola
        Cola<String> cola = new Cola<>(5);
        cola.encolar("Solicitud A");
        cola.encolar("Solicitud B");
        cola.encolar("Solicitud C");
        System.out.println("Cola: " + cola);
        System.out.println("Desencolar: " + cola.desencolar());
        System.out.println("Cola luego: " + cola);

        // Pila
        Pila<String> pila = new Pila<>(10);
        pila.apilar("Piso 5");
        pila.apilar("Piso 12");
        pila.apilar("Piso 20");
        System.out.println("\nPila: " + pila);
        System.out.println("Desapilar: " + pila.desapilar());
        System.out.println("Pila luego: " + pila);

        // Árbol
        ArbolPisos arbol = ArbolPisos.crearParaEdificio(25);
        System.out.println("\nÁrbol contiene piso 15: " + arbol.contiene(15));
        System.out.println("Piso 1 restringido: " + arbol.esRestringido(1));
        System.out.println("Etiqueta piso 1: " + arbol.getEtiqueta(1));
        System.out.println("Etiqueta piso 25: " + arbol.getEtiqueta(25));

        // Grafo
        GrafoEdificio grafo = GrafoEdificio.crearParaEdificio(25);
        System.out.println("\nGrafo distancia piso 3 → piso 18: "
                + grafo.distanciaMinima(3, 18) + " pisos");
        System.out.println("Ruta piso 1 → piso 5: "
                + grafo.rutaCompleta(1, 5));

        System.out.println("============================\n");



        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
