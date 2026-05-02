package com.elevators.vista.componentes;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Tarjeta de selección de modo en la pantalla de inicio.
 * Al hacer hover se levanta con animación.
 * Al hacer click ejecuta la acción configurada.
 */
public class TarjetaModo {

    private final VBox contenedor;

    public TarjetaModo(String emoji,
                       String titulo,
                       String descripcion,
                       String[] features,
                       String color,
                       Runnable accionClick) {

        contenedor = new VBox(16);
        contenedor.setPadding(new Insets(28));
        contenedor.setCursor(Cursor.HAND);
        contenedor.setAlignment(Pos.TOP_LEFT);

        String estiloBase =
                "-fx-background-color: #1a2028;" +
                        "-fx-border-color: #2d3540;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 16;" +
                        "-fx-background-radius: 16;";

        contenedor.setStyle(estiloBase);

        // Icono con fondo de color
        Label icono = new Label(emoji);
        icono.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10 14 10 14;" +
                        "-fx-min-width: 48;" +
                        "-fx-alignment: center;"
        );

        // Titulo
        Label labelTitulo = new Label(titulo);
        labelTitulo.setStyle(
                "-fx-font-size: 19px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #e8ecf1;"
        );

        // Descripcion
        Label labelDesc = new Label(descripcion);
        labelDesc.setWrapText(true);
        labelDesc.setMaxWidth(420);
        labelDesc.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #8b95a3;"
        );

        // Features con checkmarks
        VBox listaFeatures = new VBox(8);
        for (String feature : features) {
            HBox fila = new HBox(10);
            fila.setAlignment(Pos.CENTER_LEFT);

            Label check = new Label("\u2713");  // unicode check ✓
            check.setStyle(
                    "-fx-text-fill: " + color + ";" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;"
            );

            Label textoFeature = new Label(feature);
            textoFeature.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-text-fill: #8b95a3;"
            );

            fila.getChildren().addAll(check, textoFeature);
            listaFeatures.getChildren().add(fila);
        }

        contenedor.getChildren().addAll(
                icono, labelTitulo, labelDesc, listaFeatures
        );

        // ===== Animaciones hover =====
        String estiloHover =
                "-fx-background-color: #1a2028;" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 16;" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, " + color + "44, 24, 0, 0, 8);";

        contenedor.setOnMouseEntered(e -> {
            contenedor.setStyle(estiloHover);
            TranslateTransition subir = new TranslateTransition(
                    Duration.millis(200), contenedor);
            subir.setToY(-6);
            subir.play();
        });

        contenedor.setOnMouseExited(e -> {
            contenedor.setStyle(estiloBase);
            TranslateTransition bajar = new TranslateTransition(
                    Duration.millis(200), contenedor);
            bajar.setToY(0);
            bajar.play();
        });

        contenedor.setOnMousePressed(e -> {
            ScaleTransition press = new ScaleTransition(
                    Duration.millis(80), contenedor);
            press.setToX(0.97);
            press.setToY(0.97);
            press.play();
        });

        contenedor.setOnMouseReleased(e -> {
            ScaleTransition release = new ScaleTransition(
                    Duration.millis(80), contenedor);
            release.setToX(1.0);
            release.setToY(1.0);
            release.setOnFinished(ev -> accionClick.run());
            release.play();
        });
    }

    public VBox getContenedor() { return contenedor; }
}