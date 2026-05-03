package com.elevators.vista;

import com.elevators.controlador.ControladorSistema;
import com.elevators.modelo.Ascensor;
import com.elevators.modelo.Direccion;
import com.elevators.modelo.Edificio;
import com.elevators.modelo.EstadoAscensor;
import com.elevators.vista.componentes.Estilos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Vista del interior del ascensor.
 * Muestra el display de piso actual y el panel con los 25 botones
 * para que el pasajero seleccione su destino.
 */
public class VistaAscensor {

    private final VistaPrincipal principal;
    private final ControladorSistema controlador;
    private final ScrollPane raiz;
    private final VBox contenedor;

    // Estado
    private int edificioId = -1;
    private int pisoOrigen = 1;
    private int pisoActual = 1;
    private int pisoDestino = -1;
    private String ascensorId = null;

    // Referencias
    private Label cabinFloor;
    private Label cabinDirection;
    private Label cabinStatus;
    private Pane cabinProgressFill;
    private Label cabinProgressLabel;
    private final Map<Integer, Button> botonesPisos = new HashMap<>();
    private Label headerTitulo;

    private static final int TOTAL_PISOS = Edificio.TOTAL_PISOS;

    public VistaAscensor(VistaPrincipal principal) {
        this.principal = principal;
        this.controlador = principal.getControlador();

        contenedor = new VBox(16);
        contenedor.setPadding(new Insets(20, 24, 24, 24));
        contenedor.setStyle("-fx-background-color: #0f1419;");

        raiz = new ScrollPane(contenedor);
        raiz.setFitToWidth(true);
        raiz.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        raiz.setStyle("-fx-background-color: #0f1419; -fx-background: #0f1419;");
    }

    public void configurar(int edificioId, int pisoOrigen, String ascensorId) {
        this.edificioId = edificioId;
        this.pisoOrigen = pisoOrigen;
        this.pisoActual = pisoOrigen;
        this.pisoDestino = -1;
        this.ascensorId = ascensorId;

        construir();
        registrarCallbacks();
    }

    private void construir() {
        contenedor.getChildren().clear();
        botonesPisos.clear();

        contenedor.getChildren().addAll(
                construirTopBar(),
                construirInterior()
        );
    }

    private HBox construirTopBar() {
        HBox bar = new HBox(16);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle(Estilos.ESTILO_PANEL);

        Button btnVolver = new Button("<- Salir");
        btnVolver.setStyle(
                "-fx-background-color: #232b35;" +
                        "-fx-text-fill: #e8ecf1;" +
                        "-fx-border-color: #3d4654;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 14 8 14;"
        );
        btnVolver.setOnAction(e -> principal.irALobby());

        headerTitulo = new Label(
                "Ascensor " + ascensorId + " - Edificio " + edificioId
        );
        headerTitulo.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #8b95a3;"
        );

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        bar.getChildren().addAll(btnVolver, headerTitulo, esp);
        return bar;
    }

    private GridPane construirInterior() {
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setPadding(new Insets(28));

        grid.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1a1f27 0%, #0f1419 100%);" +
                        "-fx-border-color: #2d3540;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 14;" +
                        "-fx-background-radius: 14;"
        );


        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        col1.setFillWidth(true);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(360);
        col2.setMaxWidth(360);
        col2.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(construirCabinSide(), 0, 0);
        grid.add(construirPanelPisos(), 1, 0);

        return grid;
    }

    // ===================================================================
    // LADO IZQUIERDO: DISPLAY DE LA CABINA
    // ===================================================================

    private VBox construirCabinSide() {
        VBox box = new VBox(28);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(24));
        box.setMinHeight(480);
        box.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(20,24,30,0.6), rgba(15,18,24,0.9));" +
                        "-fx-border-color: #2d3540;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        // Display
        VBox display = new VBox(8);
        display.setAlignment(Pos.CENTER);
        display.setPadding(new Insets(24, 32, 24, 32));
        display.setMinWidth(220);
        display.setStyle(
                "-fx-background-color: #061018;" +
                        "-fx-border-color: #2d3540;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        cabinFloor = new Label(String.format("%02d", pisoActual));
        cabinFloor.setStyle(
                "-fx-font-family: monospace;" +
                        "-fx-font-size: 64px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #fbbf24;"
        );

        cabinDirection = new Label("o");
        cabinDirection.setStyle(
                "-fx-font-size: 22px;" +
                        "-fx-text-fill: #fbbf24;"
        );

        cabinStatus = new Label("Selecciona destino");
        cabinStatus.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-text-fill: #8b95a3;"
        );

        display.getChildren().addAll(cabinFloor, cabinDirection, cabinStatus);

        // Barra de progreso
        VBox progressBox = new VBox(8);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setMaxWidth(260);

        Pane progressBar = new Pane();
        progressBar.setMinHeight(6);
        progressBar.setMaxHeight(6);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle(
                "-fx-background-color: #0a0e12;" +
                        "-fx-border-color: #2d3540;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 3;" +
                        "-fx-background-radius: 3;"
        );

        cabinProgressFill = new Pane();
        cabinProgressFill.setMinHeight(6);
        cabinProgressFill.setMaxHeight(6);
        cabinProgressFill.setPrefWidth(0);
        cabinProgressFill.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366f1, #818cf8);" +
                        "-fx-background-radius: 3;"
        );
        progressBar.getChildren().add(cabinProgressFill);

        cabinProgressLabel = new Label("Esperando destino...");
        cabinProgressLabel.setStyle(Estilos.ESTILO_LABEL_MUTED);

        progressBox.getChildren().addAll(progressBar, cabinProgressLabel);

        box.getChildren().addAll(display, progressBox);
        return box;
    }

    // ===================================================================
    // LADO DERECHO: PANEL DE 25 BOTONES
    // ===================================================================

    private VBox construirPanelPisos() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(22));
        box.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #2a3140, #1a1f27);" +
                        "-fx-border-color: #3d4654;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        Label titulo = new Label("PANEL DE PISOS");
        titulo.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #8b95a3;" +
                        "-fx-padding: 0 0 8 0;"
        );

        // Grid 5x5 con los pisos
        // Distribucion: top-left = 25, top-right = 21, bottom-left = 5, bottom-right = 1
        // Recorremos: fila 0 = pisos 21-25, fila 1 = 16-20, ..., fila 4 = 1-5
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                int piso = (4 - row) * 5 + (col + 1);
                Button btn = construirBotonPiso(piso);
                grid.add(btn, col, row);
            }
        }

        box.getChildren().addAll(titulo, grid);
        return box;
    }

    private Button construirBotonPiso(int piso) {
        Button btn = new Button(String.valueOf(piso));
        btn.setMinSize(54, 54);
        btn.setMaxSize(54, 54);

        boolean esCurrent = piso == pisoOrigen;

        btn.setStyle(esCurrent ? estiloPisoCurrent() : estiloPisoNormal());

        if (!esCurrent) {
            btn.setOnMouseEntered(e -> {
                if (!btn.getStyle().contains("#f59e0b") && !btn.getStyle().contains("#6366f1"))
                    btn.setStyle(estiloPisoHover());
            });
            btn.setOnMouseExited(e -> {
                if (!btn.getStyle().contains("#f59e0b") && !btn.getStyle().contains("#6366f1"))
                    btn.setStyle(estiloPisoNormal());
            });
            btn.setOnAction(e -> presionarPiso(piso));
        }

        botonesPisos.put(piso, btn);
        return btn;
    }

    private String estiloPisoCurrent() {
        return
                "-fx-background-color: #6366f1;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #818cf8;" +
                        "-fx-border-width: 2;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: not-allowed;";
    }

    private String estiloPisoNormal() {
        return
                "-fx-background-color: #0a0e12;" +
                        "-fx-text-fill: #e8ecf1;" +
                        "-fx-border-color: #3d4654;" +
                        "-fx-border-width: 2;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;";
    }

    private String estiloPisoHover() {
        return
                "-fx-background-color: #0a0e12;" +
                        "-fx-text-fill: #e8ecf1;" +
                        "-fx-border-color: #6366f1;" +
                        "-fx-border-width: 2;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;";
    }

    private String estiloPisoPresionado() {
        return
                "-fx-background-color: #f59e0b;" +
                        "-fx-text-fill: #2a1a02;" +
                        "-fx-border-color: #d97706;" +
                        "-fx-border-width: 2;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, #f59e0b88, 20, 0, 0, 0);";
    }

    // ===================================================================
    // LOGICA DEL VIAJE
    // ===================================================================

    private void presionarPiso(int destino) {
        if (destino == pisoOrigen || pisoDestino != -1) return;

        pisoDestino = destino;

        // Pintar boton presionado y deshabilitar el resto
        for (Map.Entry<Integer, Button> entry : botonesPisos.entrySet()) {
            int p = entry.getKey();
            Button btn = entry.getValue();
            if (p == destino) {
                btn.setStyle(estiloPisoPresionado());
            } else if (p != pisoOrigen) {
                btn.setOpacity(0.4);
                btn.setDisable(true);
            }
        }

        // Encolar directamente en este ascensor para garantizar que él sea quien se mueva
        Direccion dir = destino > pisoOrigen ? Direccion.SUBIR : Direccion.BAJAR;
        controlador.registrarSolicitudParaAscensor(ascensorId, pisoOrigen, destino, dir);

        cabinStatus.setText("Solicitud creada - cerrando puertas...");
    }

    // ===================================================================
    // CALLBACKS DEL CONTROLADOR
    // ===================================================================

    private void registrarCallbacks() {
        controlador.setOnAscensorCambio(this::onAscensorActualizado);
    }

    private void onAscensorActualizado(Ascensor ascensor) {
        // Solo nos interesa el ascensor que el pasajero abordo
        if (!ascensor.getId().equals(ascensorId)) return;

        pisoActual = ascensor.getPisoActual();
        EstadoAscensor estado = ascensor.getEstado();

        cabinFloor.setText(String.format("%02d", pisoActual));
        cabinFloor.setStyle(
                "-fx-font-family: monospace;" +
                        "-fx-font-size: 64px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + Estilos.colorLed(estado) + ";"
        );

        cabinDirection.setText(Estilos.simboloEstado(estado));
        cabinDirection.setStyle(
                "-fx-font-size: 22px;" +
                        "-fx-text-fill: " + Estilos.colorLed(estado) + ";"
        );

        switch (estado) {
            case SUBIENDO -> cabinStatus.setText("Subiendo");
            case BAJANDO -> cabinStatus.setText("Bajando");
            case ABRIENDO_PUERTAS -> {
                if (pisoDestino != -1 && pisoActual == pisoDestino) {
                    cabinStatus.setText("Llegamos al piso " + pisoDestino + "!");
                    cabinProgressLabel.setText("Puertas abriendo...");
                }
            }
            case INACTIVO -> cabinStatus.setText("En espera");
        }

        // Barra de progreso
        if (pisoDestino != -1) {
            int totalPisos = Math.abs(pisoDestino - pisoOrigen);
            int recorridos = Math.abs(pisoActual - pisoOrigen);
            if (totalPisos > 0) {
                double pct = (double) recorridos / totalPisos;
                Pane parent = (Pane) cabinProgressFill.getParent();
                if (parent != null) {
                    cabinProgressFill.setPrefWidth(parent.getWidth() * pct);
                }
                cabinProgressLabel.setText(recorridos + " de " + totalPisos + " pisos");
            }
        }
    }

    public Node getVista() { return raiz; }

}
