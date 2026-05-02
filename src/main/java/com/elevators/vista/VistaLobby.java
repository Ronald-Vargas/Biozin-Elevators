package com.elevators.vista;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class VistaLobby {

    private final VistaPrincipal principal;
    private final VBox raiz;

    public VistaLobby(VistaPrincipal principal) {
        this.principal = principal;
        this.raiz = new VBox();
        this.raiz.setStyle("-fx-background-color: #0f1419;");
    }

    public void reset() {
        // Se llenará en el Paso 8
    }

    public Node getVista() { return raiz; }
}
