package com.elevators.biozinelevators;

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

        System.out.println("\n========================\n");



        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
