package com.elevators.vista;

import com.elevators.controlador.ControladorSistema;
import com.elevators.modelo.Ascensor;
import com.elevators.modelo.Direccion;
import com.elevators.modelo.Edificio;
import com.elevators.modelo.EstadoAscensor;
import com.elevators.vista.componentes.Estilos;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Vista del modo Pasajero.
 * Tiene 2 sub-vistas internas:
 *   1. Seleccion de edificio (3 tarjetas con fachada SVG)
 *   2. Lobby del edificio (3 puertas + botones de llamada)
 */
public class VistaLobby {

    private final VistaPrincipal principal;
    private final ControladorSistema controlador;
    private final ScrollPane raiz;
    private final VBox contenedor;

    // Sub-vistas
    private VBox subVistaEdificios;
    private VBox subVistaLobby;

    // Estado del pasajero
    private int edificioSeleccionado = -1;
    private int pisoUsuario = 1;
    private String ascensorAsignado = null;

    // Referencias a elementos del lobby para actualizar en tiempo real
    private Label labelPisoUsuario;
    private final Map<String, VBox> stations = new HashMap<>();
    private final Map<String, Label> stationLeds = new HashMap<>();
    private final Map<String, Label> stationStatus = new HashMap<>();
    private final Map<String, Rectangle> stationDoorsLeft = new HashMap<>();
    private final Map<String, Rectangle> stationDoorsRight = new HashMap<>();
    private Button btnSubir, btnBajar;
    private Label headerTitulo;

    private static final int TOTAL_PISOS = Edificio.TOTAL_PISOS;

    public VistaLobby(VistaPrincipal principal) {
        this.principal = principal;
        this.controlador = principal.getControlador();

        contenedor = new VBox(16);
        contenedor.setPadding(new Insets(20, 24, 24, 24));
        contenedor.setStyle("-fx-background-color: #0f1419;");
        contenedor.getChildren().add(construirTopBar());

        raiz = new ScrollPane(contenedor);
        raiz.setFitToWidth(true);
        raiz.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        raiz.setStyle("-fx-background-color: #0f1419; -fx-background: #0f1419;");

        // Construir sub-vistas
        subVistaEdificios = construirSubVistaEdificios();
        subVistaLobby = construirSubVistaLobby();
    }

    public void reset() {
        edificioSeleccionado = -1;
        pisoUsuario = 1;
        ascensorAsignado = null;
        mostrarSubVistaEdificios();
        registrarCallbacks();
    }

    // ===================================================================
    // TOP BAR
    // ===================================================================

    private HBox construirTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle(Estilos.ESTILO_PANEL);
        bar.setSpacing(16);

        Button btnVolver = new Button("<- Inicio");
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
        btnVolver.setOnAction(e -> {
            // Si estamos dentro de un edificio, volver a la seleccion
            if (edificioSeleccionado != -1) {
                reset();
            } else {
                principal.irAInicio();
            }
        });

        headerTitulo = new Label("Vista de Pasajero");
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

    // ===================================================================
    // SUB-VISTA 1: SELECCION DE EDIFICIO
    // ===================================================================

    private VBox construirSubVistaEdificios() {
        VBox box = new VBox(24);
        box.setAlignment(Pos.TOP_CENTER);

        // Saludo
        VBox welcome = new VBox(8);
        welcome.setAlignment(Pos.CENTER);
        welcome.setPadding(new Insets(20, 0, 24, 0));

        Label titulo = new Label("Bienvenido");
        titulo.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #e8ecf1;"
        );

        Label subtitulo = new Label(
                "Selecciona el edificio donde te encuentras para llamar un ascensor"
        );
        subtitulo.setStyle("-fx-font-size: 13px; -fx-text-fill: #8b95a3;");

        welcome.getChildren().addAll(titulo, subtitulo);

        // Grid de edificios
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            grid.getColumnConstraints().add(col);
        }

        int columna = 0;
        for (Edificio edificio : controlador.getEdificios()) {
            VBox tarjeta = construirTarjetaEdificio(edificio);
            tarjeta.setMaxWidth(Double.MAX_VALUE);
            grid.add(tarjeta, columna++, 0);
        }

        box.getChildren().addAll(welcome, grid);
        return box;
    }

    private VBox construirTarjetaEdificio(Edificio edificio) {
        VBox tarjeta = new VBox(14);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(24, 22, 22, 22));
        tarjeta.setCursor(Cursor.HAND);

        String color = Estilos.colorEdificio(edificio.getId());

        String estiloBase =
                "-fx-background-color: #1a2028;" +
                        "-fx-border-color: #2d3540;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 14;" +
                        "-fx-background-radius: 14;";

        String estiloHover =
                "-fx-background-color: #1a2028;" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 14;" +
                        "-fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, " + color + "33, 24, 0, 0, 8);";

        tarjeta.setStyle(estiloBase);

        // Fachada
        Pane fachada = construirFachada(edificio);

        // Nombre
        Label nombre = new Label(edificio.getNombre());
        nombre.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #e8ecf1;"
        );

        // Meta
        int colaSize = controlador.getTamanioColaEdificio(edificio.getId());
        Label meta = new Label(
                TOTAL_PISOS + " pisos    3 ascensores    Cola: " + colaSize + "/20"
        );
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #8b95a3;");

        // Boton "Entrar"
        Label entrar = new Label("Entrar ->");
        entrar.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-padding: 7 14 7 14;" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 999;" +
                        "-fx-background-radius: 999;"
        );

        tarjeta.getChildren().addAll(fachada, nombre, meta, entrar);

        // Hover y click
        tarjeta.setOnMouseEntered(e -> {
            tarjeta.setStyle(estiloHover);
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), tarjeta);
            tt.setToY(-5);
            tt.play();
        });

        tarjeta.setOnMouseExited(e -> {
            tarjeta.setStyle(estiloBase);
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), tarjeta);
            tt.setToY(0);
            tt.play();
        });

        tarjeta.setOnMouseClicked(e -> {
            edificioSeleccionado = edificio.getId();
            pisoUsuario = 1;
            ascensorAsignado = null;
            headerTitulo.setText("Lobby - " + edificio.getNombre());
            mostrarSubVistaLobby();
        });

        return tarjeta;
    }

    /**
     * Construye una pequeña fachada SVG del edificio con ventanas iluminadas.
     */
    private Pane construirFachada(Edificio edificio) {
        Pane pane = new Pane();
        pane.setMinSize(160, 200);
        pane.setMaxSize(160, 200);
        pane.setPrefSize(160, 200);

        String color = Estilos.colorEdificio(edificio.getId());

        // Estructura del edificio (varia segun el id)
        Rectangle base;
        if (edificio.getId() == 1) {
            base = new Rectangle(20, 30, 120, 160);
        } else if (edificio.getId() == 2) {
            base = new Rectangle(15, 20, 130, 170);
        } else {
            base = new Rectangle(25, 15, 110, 175);
        }
        base.setStyle(
                "-fx-fill: " + color + "26;" +
                        "-fx-stroke: " + color + ";" +
                        "-fx-stroke-width: 1.5;"
        );
        pane.getChildren().add(base);

        // Ventanas
        int seed = edificio.getId() * 7;
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 4; c++) {
                boolean iluminada = ((r * 4 + c + seed) * 13) % 10 > 3;
                Rectangle ventana = new Rectangle(
                        32 + c * 24, 50 + r * 18, 14, 10
                );
                if (iluminada) {
                    ventana.setStyle(
                            "-fx-fill: " + color + "B3;" +
                                    "-fx-arc-width: 1;" +
                                    "-fx-arc-height: 1;"
                    );
                } else {
                    ventana.setStyle(
                            "-fx-fill: #1a1f27;"
                    );
                }
                pane.getChildren().add(ventana);
            }
        }

        // Puerta principal
        Rectangle puerta = new Rectangle(70, 170, 20, 22);
        puerta.setStyle("-fx-fill: " + color + "66;");
        pane.getChildren().add(puerta);

        return pane;
    }

    // ===================================================================
    // SUB-VISTA 2: LOBBY
    // ===================================================================

    private VBox construirSubVistaLobby() {
        VBox box = new VBox(20);

        // Panel del lobby
        VBox lobby = new VBox(20);
        lobby.setPadding(new Insets(24));
        lobby.setStyle(Estilos.ESTILO_PANEL);
        lobby.setMaxWidth(Double.MAX_VALUE);

        // Container que se rellenara dinamicamente segun el edificio
        VBox stationsContainer = new VBox(18);
        stationsContainer.setId("stations-container");

        // Botones de llamada
        VBox panelLlamada = construirPanelLlamada();

        lobby.getChildren().addAll(stationsContainer, panelLlamada);
        box.getChildren().add(lobby);
        return box;
    }

    /**
     * Reconstruye las 3 estaciones (puertas) segun el edificio seleccionado.
     */
    private void reconstruirEstaciones() {
        VBox stationsContainer = (VBox) subVistaLobby.lookup("#stations-container");
        if (stationsContainer == null) return;

        stationsContainer.getChildren().clear();
        stations.clear();
        stationLeds.clear();
        stationStatus.clear();
        stationDoorsLeft.clear();
        stationDoorsRight.clear();

        if (edificioSeleccionado == -1) return;

        Edificio edificio = controlador.getEdificio(edificioSeleccionado);

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setMaxWidth(Double.MAX_VALUE);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            grid.getColumnConstraints().add(col);
        }

        int columna = 0;
        for (Ascensor ascensor : edificio.getAscensores()) {
            VBox station = construirEstacion(ascensor);
            station.setMaxWidth(Double.MAX_VALUE);
            grid.add(station, columna++, 0);
        }

        stationsContainer.getChildren().add(grid);
    }

    private VBox construirEstacion(Ascensor ascensor) {
        VBox station = new VBox(10);
        station.setPadding(new Insets(16));
        station.setAlignment(Pos.TOP_CENTER);

        String estiloBase =
                "-fx-background-color: #0a0e12;" +
                        "-fx-border-color: #3d4654;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;";

        station.setStyle(estiloBase);

        // Display LED
        HBox display = new HBox();
        display.setAlignment(Pos.CENTER);
        display.setPadding(new Insets(10, 14, 10, 14));
        display.setMaxWidth(Double.MAX_VALUE);
        display.setStyle(Estilos.ESTILO_LED);

        Label numero = new Label(String.format("%02d", ascensor.getPisoActual()));
        numero.setStyle(
                "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: monospace;" +
                        "-fx-text-fill: " + Estilos.colorLed(ascensor.getEstado()) + ";"
        );

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        Label flecha = new Label(Estilos.simboloEstado(ascensor.getEstado()));
        flecha.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: " + Estilos.colorLed(ascensor.getEstado()) + ";"
        );

        display.getChildren().addAll(numero, esp, flecha);

        // Puertas
        Pane doorsFrame = new Pane();
        doorsFrame.setMinHeight(180);
        doorsFrame.setPrefHeight(180);
        doorsFrame.setMaxHeight(180);
        doorsFrame.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #161b22 0%, #0a0e12 100%);" +
                        "-fx-border-color: #2d3540;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        Rectangle doorLeft = new Rectangle();
        doorLeft.heightProperty().bind(doorsFrame.heightProperty());
        doorLeft.widthProperty().bind(doorsFrame.widthProperty().divide(2));
        doorLeft.setLayoutX(0);
        doorLeft.setStyle(
                "-fx-fill: linear-gradient(to bottom right, #3d4654, #2a323d);"
        );

        Rectangle doorRight = new Rectangle();
        doorRight.heightProperty().bind(doorsFrame.heightProperty());
        doorRight.widthProperty().bind(doorsFrame.widthProperty().divide(2));
        doorRight.layoutXProperty().bind(doorsFrame.widthProperty().divide(2));
        doorRight.setStyle(
                "-fx-fill: linear-gradient(to bottom right, #3d4654, #2a323d);"
        );

        doorsFrame.getChildren().addAll(doorLeft, doorRight);

        // Etiquetas
        Label label = new Label("Ascensor " + ascensor.getId());
        label.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #e8ecf1;"
        );

        Label status = new Label(textoEstado(ascensor.getEstado()));
        status.setStyle(Estilos.ESTILO_LABEL_MUTED);

        // Boton entrar
        Button btnEntrar = new Button("Entrar al ascensor ->");
        btnEntrar.setMaxWidth(Double.MAX_VALUE);
        btnEntrar.setStyle(
                "-fx-background-color: #22c55e;" +
                        "-fx-text-fill: #052e0a;" +
                        "-fx-border-color: #16a34a;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 9 14 9 14;"
        );
        btnEntrar.setVisible(false);
        btnEntrar.setManaged(false);
        btnEntrar.setOnAction(e -> {
            if (ascensor.getId().equals(ascensorAsignado)) {
                principal.irAVistaAscensor(
                        edificioSeleccionado, pisoUsuario, ascensorAsignado
                );
            }
        });

        station.getChildren().addAll(display, doorsFrame, label, status, btnEntrar);

        // Guardar referencias
        stations.put(ascensor.getId(), station);
        stationLeds.put(ascensor.getId(), numero);
        stationStatus.put(ascensor.getId(), status);
        stationDoorsLeft.put(ascensor.getId(), doorLeft);
        stationDoorsRight.put(ascensor.getId(), doorRight);

        // Asociar boton entrar a la station
        station.setUserData(btnEntrar);

        return station;
    }

    private VBox construirPanelLlamada() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(20));
        panel.setStyle(
                "-fx-background-color: #232b35;" +
                        "-fx-border-color: #2d3540;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        GridPane grid = new GridPane();
        grid.setHgap(24);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        col1.setFillWidth(true);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().addAll(col1, col2);

        // Seccion: ¿En que piso estas?
        VBox seccionPiso = new VBox(10);
        Label tituloPiso = new Label("En que piso estas?");
        tituloPiso.setStyle(Estilos.ESTILO_LABEL_MUTED);

        HBox floorInput = new HBox(8);
        floorInput.setAlignment(Pos.CENTER_LEFT);
        floorInput.setPadding(new Insets(6));
        floorInput.setMaxWidth(220);
        floorInput.setStyle(
                "-fx-background-color: #0a0e12;" +
                        "-fx-border-color: #3d4654;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 999;" +
                        "-fx-background-radius: 999;"
        );

        Button btnMenos = construirBotonRedondo("-");
        Button btnMas = construirBotonRedondo("+");

        labelPisoUsuario = new Label("Piso 1");
        labelPisoUsuario.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: monospace;" +
                        "-fx-text-fill: #818cf8;" +
                        "-fx-min-width: 80;" +
                        "-fx-alignment: center;"
        );

        btnMenos.setOnAction(e -> {
            if (pisoUsuario > 1) {
                pisoUsuario--;
                labelPisoUsuario.setText("Piso " + pisoUsuario);
                limpiarAsignacion();
            }
        });
        btnMas.setOnAction(e -> {
            if (pisoUsuario < TOTAL_PISOS) {
                pisoUsuario++;
                labelPisoUsuario.setText("Piso " + pisoUsuario);
                limpiarAsignacion();
            }
        });

        floorInput.getChildren().addAll(btnMenos, labelPisoUsuario, btnMas);
        seccionPiso.getChildren().addAll(tituloPiso, floorInput);

        // Seccion: Llamar ascensor
        VBox seccionLlamar = new VBox(10);
        seccionLlamar.setAlignment(Pos.CENTER);
        Label tituloLlamar = new Label("Llamar ascensor");
        tituloLlamar.setStyle(Estilos.ESTILO_LABEL_MUTED);

        HBox botonesLlamar = new HBox(12);
        botonesLlamar.setAlignment(Pos.CENTER);

        btnSubir = construirBotonLlamada("^", "Subir");
        btnBajar = construirBotonLlamada("v", "Bajar");

        btnSubir.setOnAction(e -> {
            if (pisoUsuario == TOTAL_PISOS) return;
            llamarAscensor(Direccion.SUBIR);
        });
        btnBajar.setOnAction(e -> {
            if (pisoUsuario == 1) return;
            llamarAscensor(Direccion.BAJAR);
        });

        botonesLlamar.getChildren().addAll(btnSubir, btnBajar);
        seccionLlamar.getChildren().addAll(tituloLlamar, botonesLlamar);

        grid.add(seccionPiso, 0, 0);
        grid.add(seccionLlamar, 1, 0);

        panel.getChildren().add(grid);
        return panel;
    }

    private Button construirBotonRedondo(String texto) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: #1a2028;" +
                        "-fx-text-fill: #e8ecf1;" +
                        "-fx-min-width: 32;" +
                        "-fx-min-height: 32;" +
                        "-fx-max-width: 32;" +
                        "-fx-max-height: 32;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );
        return btn;
    }

    private Button construirBotonLlamada(String simbolo, String tooltip) {
        Button btn = new Button(simbolo);
        btn.setMinSize(70, 70);
        btn.setMaxSize(70, 70);
        btn.setStyle(estiloBotonLlamada(false));
        btn.setOnMouseEntered(e -> {
            if (!ascensorAsignadoActivo(btn))
                btn.setStyle(estiloBotonLlamadaHover());
        });
        btn.setOnMouseExited(e -> {
            if (!ascensorAsignadoActivo(btn))
                btn.setStyle(estiloBotonLlamada(false));
        });
        return btn;
    }

    private boolean ascensorAsignadoActivo(Button btn) {
        return btn.getStyle().contains("#f59e0b");
    }

    private String estiloBotonLlamada(boolean activo) {
        if (activo) {
            return
                    "-fx-background-color: #f59e0b;" +
                            "-fx-text-fill: #2a1a02;" +
                            "-fx-border-color: #d97706;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 999;" +
                            "-fx-background-radius: 999;" +
                            "-fx-font-size: 24px;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, #f59e0b88, 24, 0, 0, 0);";
        }
        return
                "-fx-background-color: #0a0e12;" +
                        "-fx-text-fill: #e8ecf1;" +
                        "-fx-border-color: #3d4654;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 999;" +
                        "-fx-background-radius: 999;" +
                        "-fx-font-size: 24px;" +
                        "-fx-cursor: hand;";
    }

    private String estiloBotonLlamadaHover() {
        return
                "-fx-background-color: #0a0e12;" +
                        "-fx-text-fill: #e8ecf1;" +
                        "-fx-border-color: #6366f1;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 999;" +
                        "-fx-background-radius: 999;" +
                        "-fx-font-size: 24px;" +
                        "-fx-cursor: hand;";
    }

    // ===================================================================
    // LOGICA DEL FLUJO
    // ===================================================================

    private void llamarAscensor(Direccion direccion) {
        limpiarAsignacion();

        // Activar boton visualmente
        Button btn = direccion == Direccion.SUBIR ? btnSubir : btnBajar;
        btn.setStyle(estiloBotonLlamada(true));

        // Crear solicitud y obtener ascensor asignado
        Ascensor asignado = controlador.registrarSolicitud(
                edificioSeleccionado, pisoUsuario, direccion
        );

        if (asignado != null) {
            ascensorAsignado = asignado.getId();
            VBox station = stations.get(ascensorAsignado);
            if (station != null) {
                station.setStyle(
                        "-fx-background-color: #0a0e12;" +
                                "-fx-border-color: #6366f1;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 12;" +
                                "-fx-background-radius: 12;" +
                                "-fx-effect: dropshadow(gaussian, #6366f144, 28, 0, 0, 0);"
                );
                Label status = stationStatus.get(ascensorAsignado);
                if (status != null) {
                    status.setText("Asignado - llegando...");
                }
            }
        }
    }

    private void limpiarAsignacion() {
        ascensorAsignado = null;
        if (btnSubir != null) btnSubir.setStyle(estiloBotonLlamada(false));
        if (btnBajar != null) btnBajar.setStyle(estiloBotonLlamada(false));

        for (Map.Entry<String, VBox> entry : stations.entrySet()) {
            VBox station = entry.getValue();
            station.setStyle(
                    "-fx-background-color: #0a0e12;" +
                            "-fx-border-color: #3d4654;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;"
            );
            // Cerrar puertas
            cerrarPuertas(entry.getKey());
            // Ocultar boton entrar
            Object userData = station.getUserData();
            if (userData instanceof Button btn) {
                btn.setVisible(false);
                btn.setManaged(false);
            }
        }
    }

    private void abrirPuertas(String ascensorId) {
        Rectangle left = stationDoorsLeft.get(ascensorId);
        Rectangle right = stationDoorsRight.get(ascensorId);
        if (left == null || right == null) return;

        TranslateTransition leftAnim = new TranslateTransition(Duration.millis(800), left);
        leftAnim.setToX(-left.getWidth());
        leftAnim.play();

        TranslateTransition rightAnim = new TranslateTransition(Duration.millis(800), right);
        rightAnim.setToX(right.getWidth());
        rightAnim.play();
    }

    private void cerrarPuertas(String ascensorId) {
        Rectangle left = stationDoorsLeft.get(ascensorId);
        Rectangle right = stationDoorsRight.get(ascensorId);
        if (left == null || right == null) return;

        TranslateTransition leftAnim = new TranslateTransition(Duration.millis(500), left);
        leftAnim.setToX(0);
        leftAnim.play();

        TranslateTransition rightAnim = new TranslateTransition(Duration.millis(500), right);
        rightAnim.setToX(0);
        rightAnim.play();
    }

    // ===================================================================
    // CALLBACKS - actualizan en tiempo real lo que ven los usuarios
    // ===================================================================

    private void registrarCallbacks() {
        controlador.setOnAscensorCambio(this::onAscensorActualizado);
    }

    private void onAscensorActualizado(Ascensor ascensor) {
        // Solo actualizar si es del edificio seleccionado
        if (ascensor.getEdificioId() != edificioSeleccionado) return;

        Label led = stationLeds.get(ascensor.getId());
        Label status = stationStatus.get(ascensor.getId());
        VBox station = stations.get(ascensor.getId());

        if (led == null) return;

        led.setText(String.format("%02d", ascensor.getPisoActual()));
        led.setStyle(
                "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: monospace;" +
                        "-fx-text-fill: " + Estilos.colorLed(ascensor.getEstado()) + ";"
        );

        // Si el ascensor asignado llega al piso del usuario y abre puertas
        if (ascensor.getId().equals(ascensorAsignado)
                && ascensor.getPisoActual() == pisoUsuario
                && ascensor.getEstado() == EstadoAscensor.ABRIENDO_PUERTAS) {

            if (status != null) status.setText("Llego! Puertas abiertas");
            if (station != null) {
                station.setStyle(
                        "-fx-background-color: #0a0e12;" +
                                "-fx-border-color: #22c55e;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 12;" +
                                "-fx-background-radius: 12;" +
                                "-fx-effect: dropshadow(gaussian, #22c55e55, 32, 0, 0, 0);"
                );
                Object userData = station.getUserData();
                if (userData instanceof Button btn) {
                    btn.setVisible(true);
                    btn.setManaged(true);
                }
            }
            abrirPuertas(ascensor.getId());
        } else if (status != null && !ascensor.getId().equals(ascensorAsignado)) {
            status.setText(textoEstado(ascensor.getEstado()));
        }
    }

    private String textoEstado(EstadoAscensor estado) {
        return switch (estado) {
            case SUBIENDO -> "Subiendo";
            case BAJANDO -> "Bajando";
            case ABRIENDO_PUERTAS -> "Puertas abiertas";
            case INACTIVO -> "En espera";
        };
    }

    // ===================================================================
    // CAMBIO DE SUB-VISTAS
    // ===================================================================

    private void mostrarSubVistaEdificios() {
        contenedor.getChildren().setAll(
                (Node) contenedor.getChildren().get(0) // top bar
        );
        // Reconstruir para que las colas de los edificios se vean actualizadas
        subVistaEdificios = construirSubVistaEdificios();
        contenedor.getChildren().add(subVistaEdificios);
    }

    private void mostrarSubVistaLobby() {
        contenedor.getChildren().setAll(
                (Node) contenedor.getChildren().get(0) // top bar
        );
        contenedor.getChildren().add(subVistaLobby);
        reconstruirEstaciones();
    }

    public Node getVista() { return raiz; }
}

