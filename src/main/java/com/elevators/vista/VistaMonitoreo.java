package com.elevators.vista;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class VistaMonitoreo {

    private final VistaPrincipal principal;
    private final VBox raiz;

    public VistaMonitoreo(VistaPrincipal principal) {
        this.principal = principal;
        this.raiz = new VBox();
        this.raiz.setStyle("-fx-background-color: #0f1419;");
    }

    public void activar() {
        // Se llenará en el Paso 7
    }

    public Node getVista() { return raiz; }
}
