package com.elevators.vista;

import com.elevators.vista.componentes.Estilos;
import com.elevators.vista.componentes.TarjetaModo;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * Pantalla de inicio: muestra el título del sistema
 * y las dos tarjetas para elegir modo (Admin o Pasajero).
 */
public class VistaInicio {

    private final VistaPrincipal principal;
    private final VBox raiz;

    public VistaInicio(VistaPrincipal principal) {
        this.principal = principal;
        this.raiz = construir();
        animarEntrada();
    }

    private VBox construir() {
        VBox contenedor = new VBox(40);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPadding(new Insets(60, 80, 60, 80));
        contenedor.setStyle("-fx-background-color: #0f1419;");

        contenedor.getChildren().addAll(
                construirEncabezado(),
                construirTarjetas()
        );
        return contenedor;
    }

    private VBox construirEncabezado() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);

        Image logo = new Image(getClass().getResourceAsStream("/Biozin-Elevators-Logo.png"));
        ImageView icono = new ImageView(logo);
        icono.setFitWidth(350);
        icono.setFitHeight(350);
        icono.setPreserveRatio(true);

        Label subtitulo = new Label(
                "Selecciona en qué modo deseas ingresar al sistema"
        );
        subtitulo.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-text-fill: #8b95a3;"
        );

        header.getChildren().addAll(icono, subtitulo);
        return header;
    }

    private HBox construirTarjetas() {
        HBox contenedor = new HBox(20);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setMaxWidth(1100);

        TarjetaModo tarjetaAdmin = new TarjetaModo(
                "ADM",
                "Modo Administrador",
                "Monitorea en tiempo real el estado de los 9 ascensores, " +
                        "las colas de solicitudes y el historial de paradas.",
                new String[]{
                        "Visualizacion en tiempo real de los 3 edificios",
                        "Seleccion entre Quicksort, Mergesort y Heapsort",
                        "Control de hilos y metricas del sistema"
                },
                "#6366f1",
                () -> principal.irAMonitoreo()
        );

        TarjetaModo tarjetaPasajero = new TarjetaModo(
                "PAS",
                "Modo Pasajero",
                "Simula la experiencia real de un usuario: entra al edificio, " +
                        "llama un ascensor desde el lobby y elige tu destino.",
                new String[]{
                        "Seleccion de edificio con fachadas unicas",
                        "Botones de llamada con asignacion automatica",
                        "Panel interno con los 25 pisos como ascensor real"
                },
                "#22c55e",
                () -> principal.irALobby()
        );

        // Forzar ancho igual a las dos tarjetas
        tarjetaAdmin.getContenedor().setPrefWidth(480);
        tarjetaAdmin.getContenedor().setMaxWidth(480);
        tarjetaAdmin.getContenedor().setMinHeight(320);

        tarjetaPasajero.getContenedor().setPrefWidth(480);
        tarjetaPasajero.getContenedor().setMaxWidth(480);
        tarjetaPasajero.getContenedor().setMinHeight(320);

        contenedor.getChildren().addAll(
                tarjetaAdmin.getContenedor(),
                tarjetaPasajero.getContenedor()
        );

        System.out.println("[DEBUG] Hijos del HBox: " + contenedor.getChildren().size());
        return contenedor;
    }

    private void animarEntrada() {
        FadeTransition fade = new FadeTransition(Duration.millis(500), raiz);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(
                Duration.millis(500), raiz
        );
        slide.setFromY(20);
        slide.setToY(0);

        fade.play();
        slide.play();
    }

    public Node getVista() { return raiz; }
}
