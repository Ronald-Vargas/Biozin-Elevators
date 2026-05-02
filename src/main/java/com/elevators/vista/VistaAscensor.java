package com.elevators.vista;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class VistaAscensor {

    private final VistaPrincipal principal;
    private final VBox raiz;

    public VistaAscensor(VistaPrincipal principal) {
        this.principal = principal;
        this.raiz = new VBox();
        this.raiz.setStyle("-fx-background-color: #0f1419;");
    }

    public void configurar(int edificioId, int pisoOrigen, String ascensorId) {
        // Se llenará en el Paso 8
    }

    public Node getVista() { return raiz; }
}
