package com.elevators.biozinelevators;

import com.elevators.estructuras.ArbolPisos;
import com.elevators.estructuras.Cola;
import com.elevators.estructuras.GrafoEdificio;
import com.elevators.estructuras.Pila;
import com.elevators.modelo.Ascensor;
import com.elevators.modelo.Direccion;
import com.elevators.modelo.Edificio;
import com.elevators.modelo.Solicitud;
import com.elevators.vista.VistaPrincipal;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada de la aplicación.
 * JavaFX llama a start() automáticamente después de init().
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Delegar todo a VistaPrincipal
        VistaPrincipal vistaPrincipal = new VistaPrincipal(stage);
        vistaPrincipal.mostrar();
    }

    public static void main(String[] args) {
        launch();
    }
}
