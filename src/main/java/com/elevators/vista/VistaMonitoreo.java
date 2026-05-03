package com.elevators.vista;

import com.elevators.controlador.ControladorSistema;
import com.elevators.modelo.Ascensor;
import com.elevators.modelo.Edificio;
import com.elevators.modelo.EstadoAscensor;
import com.elevators.modelo.Solicitud;
import com.elevators.estructuras.Cola;
import com.elevators.estructuras.Pila;
import com.elevators.vista.componentes.Estilos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

/**
 * Vista del panel de monitoreo.
 * Muestra los 3 edificios con sus 9 ascensores en tiempo real,
 * el panel de control, colas, historial y metricas.
 */
public class VistaMonitoreo {

    private final VistaPrincipal principal;
    private final ControladorSistema controlador;
    private final ScrollPane raiz;

    // Referencias a los elementos visuales que se actualizan en tiempo real
    // Clave: id del ascensor ("A1", "B2", etc.)
    private final Map<String, Rectangle> cabinas = new HashMap<>();
    private final Map<String, Label> ledsNumero = new HashMap<>();
    private final Map<String, Label> ledsFlecha = new HashMap<>();
    private final Map<String, Pane> shafts = new HashMap<>();

    private final Map<Integer, Label> pillsEdificio = new HashMap<>();
    private final Map<Integer, Label> footersEdificio = new HashMap<>();
    private final Map<Integer, Rectangle> barrasEdificio = new HashMap<>();


    // Paneles de informacion
    private VBox panelColas;
    private VBox panelHistorial;
    private VBox panelMetricas;

    // Stats en el top bar
    private Label labelActivas;
    private Label labelViajes;

    private static final int TOTAL_PISOS = Edificio.TOTAL_PISOS;
    private static final double ALTURA_SHAFT = 300.0;

    public VistaMonitoreo(VistaPrincipal principal) {
        this.principal = principal;
        this.controlador = principal.getControlador();

        VBox contenido = construir();

        // Limitar el ancho del contenido para que los HBox se dividan en columnas
        contenido.setMaxWidth(Double.MAX_VALUE);

        raiz = new ScrollPane(contenido);
        raiz.setFitToWidth(true);
        raiz.setFitToHeight(false);
        raiz.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        raiz.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        raiz.setStyle(
                "-fx-background-color: #0f1419;" +
                        "-fx-background: #0f1419;"
        );
    }

    // =====================================================================
    // CONSTRUCCION
    // =====================================================================

    private VBox construir() {
        VBox contenedor = new VBox(16);
        contenedor.setPadding(new Insets(20, 24, 24, 24));
        contenedor.setStyle("-fx-background-color: #0f1419;");

        contenedor.getChildren().addAll(
                construirTopBar(),
                construirEdificios(),       // ahora retorna GridPane
                construirControles(),
                construirPanelesInferiores() // ahora retorna GridPane
        );
        return contenedor;
    }

    // ── Top bar ──────────────────────────────────────────────────────────

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
        btnVolver.setOnAction(e -> principal.irAInicio());

        Label titulo = new Label("Panel de Monitoreo");
        titulo.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #8b95a3;"
        );

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        // Stats
        labelActivas = new Label("0");
        labelViajes  = new Label("0");

        HBox stats = new HBox(24,
                construirStat(labelActivas, "Activas"),
                construirStat(labelViajes,  "Viajes")
        );
        stats.setAlignment(Pos.CENTER_RIGHT);

        bar.getChildren().addAll(btnVolver, titulo, espaciador, stats);
        return bar;
    }

    private VBox construirStat(Label valor, String etiqueta) {
        valor.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #e8ecf1;"
        );
        Label lbl = new Label(etiqueta);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #8b95a3;");
        VBox box = new VBox(2, valor, lbl);
        box.setAlignment(Pos.CENTER_RIGHT);
        return box;
    }

    // ── Edificios ─────────────────────────────────────────────────────────

    private GridPane construirEdificios() {
        GridPane grid = new GridPane();
        grid.setHgap(16);

        // Configurar 3 columnas que se reparten el espacio en partes iguales
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            grid.getColumnConstraints().add(col);
        }

        int columna = 0;
        for (Edificio edificio : controlador.getEdificios()) {
            VBox panelEdificio = construirEdificio(edificio);
            grid.add(panelEdificio, columna, 0);
            columna++;
        }
        return grid;
    }
    private VBox construirEdificio(Edificio edificio) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(18));
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setMinWidth(0);
        panel.setStyle(Estilos.ESTILO_PANEL);

        // Cabecera
        HBox cabecera = new HBox();
        cabecera.setAlignment(Pos.CENTER_LEFT);

        Label nombre = new Label(edificio.getNombre());
        nombre.setStyle(Estilos.ESTILO_LABEL_TITULO);

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        Label pill = new Label(edificio.getTipoOrdenamiento());
        pill.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-text-fill: #818cf8;" +
                        "-fx-background-color: rgba(99,102,241,0.15);" +
                        "-fx-border-color: rgba(99,102,241,0.3);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 999;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 3 10 3 10;" +
                        "-fx-font-weight: bold;"
        );
        pill.setMinWidth(70);          // ← ancho minimo fijo
        pill.setMaxWidth(80);          // ← ancho maximo fijo
        pill.setAlignment(Pos.CENTER); // ← centrar texto
        pillsEdificio.put(edificio.getId(), pill); // ← guardar referencia

        cabecera.getChildren().addAll(nombre, esp, pill);

        // Shafts de los 3 ascensores
        HBox filaShafts = new HBox(8);
        filaShafts.setFillHeight(true);
        filaShafts.setMaxWidth(Double.MAX_VALUE);
        filaShafts.setMinWidth(0);

        for (Ascensor ascensor : edificio.getAscensores()) {
            VBox columna = construirColumnaAscensor(ascensor);
            columna.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(columna, Priority.ALWAYS);
            filaShafts.getChildren().add(columna);
        }

        // Footer: cola
        Label footerCola = new Label(
                "Cola: 0/20    3 ascensores"
        );
        footerCola.setStyle(Estilos.ESTILO_LABEL_MUTED);
        footersEdificio.put(edificio.getId(), footerCola);

        // Barra de capacidad
        StackPane barraContenedor = new StackPane();
        barraContenedor.setMaxHeight(4);
        barraContenedor.setMinHeight(4);

        Rectangle barraFondo = new Rectangle(0, 4);
        barraFondo.setStyle("-fx-fill: #0a0e12;");
        barraFondo.widthProperty().bind(panel.widthProperty().subtract(36));

        Rectangle barraRelleno = new Rectangle(0, 4);
        barraRelleno.setStyle("-fx-fill: #6366f1;");
        barrasEdificio.put(edificio.getId(), barraRelleno);

        barraContenedor.getChildren().addAll(barraFondo, barraRelleno);
        StackPane.setAlignment(barraRelleno, Pos.CENTER_LEFT);

        panel.getChildren().addAll(cabecera, filaShafts, footerCola, barraContenedor);
        return panel;
    }

    private VBox construirColumnaAscensor(Ascensor ascensor) {
        VBox columna = new VBox(8);
        columna.setAlignment(Pos.TOP_CENTER);
        columna.setMinWidth(0);

        // ── Shaft ──
        Pane shaft = new Pane();
        shaft.setMinHeight(ALTURA_SHAFT);
        shaft.setPrefHeight(ALTURA_SHAFT);
        shaft.setMaxHeight(ALTURA_SHAFT);
        shaft.setMinWidth(0);
        shaft.setMaxWidth(Double.MAX_VALUE);
        shaft.setStyle(
                "-fx-background-color: #0a0e12;" +
                        "-fx-border-color: #2d3540;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        // Clip para evitar que el dropshadow de la cabina desborde visualmente
        Rectangle clipShaft = new Rectangle();
        clipShaft.widthProperty().bind(shaft.widthProperty());
        clipShaft.setHeight(ALTURA_SHAFT);
        shaft.setClip(clipShaft);

        // Línea central (guía visual)
        Rectangle guia = new Rectangle(2, ALTURA_SHAFT);
        guia.setStyle("-fx-fill: #2d3540;");
        guia.layoutXProperty().bind(shaft.widthProperty().divide(2).subtract(1));

        // Marcas de pisos (1, 5, 10, 15, 20, 25)
        int[] marcas = {1, 5, 10, 15, 20, 25};
        for (int piso : marcas) {
            Label marca = new Label(String.valueOf(piso));
            marca.setStyle(
                    "-fx-font-size: 8px;" +
                            "-fx-text-fill: #5a636f;" +
                            "-fx-font-family: monospace;"
            );
            marca.setLayoutX(3);
            marca.setLayoutY(calcularY(piso) - 6);
            shaft.getChildren().add(marca);
        }

        // Cabina
        Rectangle cabina = new Rectangle(0, 22);
        cabina.setArcWidth(4);
        cabina.setArcHeight(4);
        cabina.setStyle(
                "-fx-fill: #232b35;" +
                        "-fx-stroke: #3d4654;" +
                        "-fx-stroke-width: 1;"
        );
        // Ancho dinámico
        cabina.widthProperty().bind(shaft.widthProperty().subtract(24));
        cabina.layoutXProperty().bind(shaft.widthProperty().subtract(cabina.widthProperty()).divide(2));
        cabina.setLayoutY(calcularY(ascensor.getPisoActual()) - 11);

        // Label del id dentro de la cabina
        Label labelId = new Label(ascensor.getId());
        labelId.setStyle(
                "-fx-font-size: 9px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #0f1419;"
        );
        labelId.layoutXProperty().bind(
                shaft.widthProperty().divide(2).subtract(labelId.widthProperty().divide(2))
        );
        labelId.setLayoutY(calcularY(ascensor.getPisoActual()) - 8);

        shaft.getChildren().addAll(guia, cabina, labelId);
        shafts.put(ascensor.getId(), shaft);
        cabinas.put(ascensor.getId(), cabina);

        // El label sigue la animación (TranslateTransition mueve translateY, no layoutY)
        labelId.translateYProperty().bind(cabina.translateYProperty());

        // ── LED Display ──
        HBox led = construirLed(ascensor);

        columna.getChildren().addAll(shaft, led);
        VBox.setVgrow(shaft, Priority.ALWAYS);
        return columna;
    }

    private HBox construirLed(Ascensor ascensor) {
        HBox led = new HBox();
        led.setAlignment(Pos.CENTER);
        led.setPadding(new Insets(6, 10, 6, 10));
        led.setMaxWidth(Double.MAX_VALUE);
        led.setMinWidth(0);
        led.setStyle(Estilos.ESTILO_LED);

        Label numero = new Label(
                String.format("%02d", ascensor.getPisoActual())
        );
        numero.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: monospace;" +
                        "-fx-text-fill: #5a636f;"
        );

        Region espLed = new Region();
        HBox.setHgrow(espLed, Priority.ALWAYS);

        Label flecha = new Label("\u00b7");  // punto = inactivo
        flecha.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #5a636f;"
        );

        led.getChildren().addAll(numero, espLed, flecha);

        ledsNumero.put(ascensor.getId(), numero);
        ledsFlecha.put(ascensor.getId(), flecha);

        return led;
    }

    // ── Controles ─────────────────────────────────────────────────────────

    private HBox construirControles() {
        HBox fila = new HBox(24);
        fila.setPadding(new Insets(20, 24, 20, 24));
        fila.setStyle(Estilos.ESTILO_PANEL);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setMaxWidth(Double.MAX_VALUE);

        // Dar espacio flexible a cada grupo
        VBox grupoSim  = construirGrupoSimulacion();
        VBox grupoOrd  = construirGrupoOrdenamiento();
        VBox grupoSol  = construirGrupoSolicitudes();

        HBox.setHgrow(grupoSim, Priority.ALWAYS);
        HBox.setHgrow(grupoOrd, Priority.ALWAYS);
        HBox.setHgrow(grupoSol, Priority.ALWAYS);

        grupoSim.setMinWidth(0);
        grupoOrd.setMinWidth(0);
        grupoSol.setMinWidth(0);

        fila.getChildren().addAll(
                grupoSim,
                separadorVertical(),
                grupoOrd,
                separadorVertical(),
                grupoSol
        );
        return fila;
    }

    private VBox construirGrupoSimulacion() {
        VBox grupo = new VBox(10);

        Label titulo = new Label("SIMULACION");
        titulo.setStyle(Estilos.ESTILO_LABEL_MUTED);

        Button btnIniciar = crearBoton("Iniciar / Reanudar", "#22c55e", "#052e0a");
        Button btnDetener = crearBoton("Detener", "#ef4444", "#2a0606");

        btnIniciar.setOnAction(e -> controlador.reanudar());
        btnDetener.setOnAction(e -> controlador.pausar());

        // Slider de velocidad
        Label lblVelocidad = new Label("Intervalo: 1000 ms");
        lblVelocidad.setStyle(Estilos.ESTILO_LABEL_MUTED);

        Slider slider = new Slider(200, 2000, 1000);
        slider.setShowTickLabels(false);
        slider.setStyle("-fx-accent: #6366f1;");
        slider.setPrefWidth(180);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int val = newVal.intValue();
            lblVelocidad.setText("Intervalo: " + val + " ms");
            controlador.setIntervaloMs(val);
        });

        grupo.getChildren().addAll(
                titulo,
                new HBox(8, btnIniciar, btnDetener),
                lblVelocidad,
                slider
        );
        return grupo;
    }

    private VBox construirGrupoOrdenamiento() {
        VBox grupo = new VBox(10);

        Label titulo = new Label("ORDENAMIENTO");
        titulo.setStyle(Estilos.ESTILO_LABEL_MUTED);

        // Botones de algoritmos
        ToggleGroup toggleGroup = new ToggleGroup();

        ToggleButton btnQuick  = crearToggle("Quicksort",  toggleGroup);
        ToggleButton btnMerge  = crearToggle("Mergesort",  toggleGroup);
        ToggleButton btnHeap   = crearToggle("Heapsort",   toggleGroup);

        btnQuick.setSelected(true);

        // Al seleccionar un algoritmo se aplica a todos los edificios
        toggleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) { oldT.setSelected(true); return; }
            String algoritmo = ((ToggleButton) newT).getText();
            for (Edificio edificio : controlador.getEdificios()) {
                controlador.setAlgoritmoEdificio(edificio.getId(), algoritmo);
                Label pill = pillsEdificio.get(edificio.getId());
                if (pill != null) pill.setText(algoritmo);
            }
        });

        HBox filaToggle = new HBox(8, btnQuick, btnMerge, btnHeap);

        Button btnCopiar = crearBoton("Copiar listas", "#232b35", "#e8ecf1");
        btnCopiar.setOnAction(e -> {
            btnCopiar.setText("Copiado!");
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ex) {}
                javafx.application.Platform.runLater(
                        () -> btnCopiar.setText("Copiar listas")
                );
            }).start();
        });

        grupo.getChildren().addAll(titulo, filaToggle, btnCopiar);
        return grupo;
    }

    private VBox construirGrupoSolicitudes() {
        VBox grupo = new VBox(10);

        Label titulo = new Label("SOLICITUDES");
        titulo.setStyle(Estilos.ESTILO_LABEL_MUTED);

        Button btnDestruir = crearBoton(
                "Destruir solicitudes", "#f59e0b", "#2a1a02"
        );
        Button btnAleatorio = crearBoton(
                "+ Solicitud aleatoria", "#232b35", "#e8ecf1"
        );

        btnDestruir.setOnAction(e -> {
            controlador.destruirTodasLasSolicitudes();
            actualizarPanelColas();
        });

        btnAleatorio.setOnAction(e -> {
            // Generar solicitud aleatoria en un edificio aleatorio
            int edificioId = (int)(Math.random() * 3) + 1;
            int pisoOrigen = (int)(Math.random() * TOTAL_PISOS) + 1;
            int pisoDestino;
            do {
                pisoDestino = (int)(Math.random() * TOTAL_PISOS) + 1;
            } while (pisoDestino == pisoOrigen);

            com.elevators.modelo.Direccion dir =
                    pisoDestino > pisoOrigen
                            ? com.elevators.modelo.Direccion.SUBIR
                            : com.elevators.modelo.Direccion.BAJAR;

            controlador.registrarSolicitud(
                    edificioId, pisoOrigen, pisoDestino, dir
            );
        });

        grupo.getChildren().addAll(
                titulo,
                new VBox(8, btnDestruir, btnAleatorio)
        );
        return grupo;
    }

    // ── Paneles inferiores ────────────────────────────────────────────────

    private GridPane construirPanelesInferiores() {
        panelColas      = construirPanel("Colas de solicitudes");
        panelHistorial  = construirPanel("Historial de paradas");
        panelMetricas   = construirPanel("Metricas del sistema");

        GridPane grid = new GridPane();
        grid.setHgap(16);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            grid.getColumnConstraints().add(col);
        }

        grid.add(panelColas,     0, 0);
        grid.add(panelHistorial, 1, 0);
        grid.add(panelMetricas,  2, 0);

        return grid;
    }

    private VBox construirPanel(String titulo) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(18));
        panel.setMinHeight(220);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setStyle(Estilos.ESTILO_PANEL);

        Label labelTitulo = new Label(titulo);
        labelTitulo.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #e8ecf1;"
        );
        // Punto decorativo
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Rectangle punto = new Rectangle(6, 6);
        punto.setArcWidth(6);
        punto.setArcHeight(6);
        punto.setStyle("-fx-fill: #6366f1;");
        header.getChildren().addAll(punto, labelTitulo);

        panel.getChildren().add(header);
        return panel;
    }

    // ── Helpers visuales ──────────────────────────────────────────────────

    private Button crearBoton(String texto, String bgColor, String textColor) {
        Button btn = new Button(texto);
        String estilo =
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 9 14 9 14;";
        btn.setStyle(estilo);
        return btn;
    }

    private ToggleButton crearToggle(String texto, ToggleGroup grupo) {
        ToggleButton btn = new ToggleButton(texto);
        btn.setToggleGroup(grupo);
        btn.setStyle(
                "-fx-background-color: #232b35;" +
                        "-fx-text-fill: #e8ecf1;" +
                        "-fx-border-color: #3d4654;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 12px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 12 8 12;"
        );
        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                btn.setStyle(
                        "-fx-background-color: #6366f1;" +
                                "-fx-text-fill: white;" +
                                "-fx-border-color: #818cf8;" +
                                "-fx-border-width: 1;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-font-size: 12px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 8 12 8 12;"
                );
            } else {
                btn.setStyle(
                        "-fx-background-color: #232b35;" +
                                "-fx-text-fill: #e8ecf1;" +
                                "-fx-border-color: #3d4654;" +
                                "-fx-border-width: 1;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-font-size: 12px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 8 12 8 12;"
                );
            }
        });
        return btn;
    }

    private Region separadorVertical() {
        Region sep = new Region();
        sep.setMinWidth(1);
        sep.setMaxWidth(1);
        sep.setMinHeight(60);
        sep.setStyle("-fx-background-color: #2d3540;");
        return sep;
    }

    // =====================================================================
    // CALCULO DE POSICION DENTRO DEL SHAFT
    // =====================================================================

    /**
     * Convierte un numero de piso a coordenada Y dentro del shaft.
     * Piso 1 = abajo (Y alta), Piso 25 = arriba (Y baja).
     */
    private double calcularY(int piso) {
        double margen = 10.0;
        double alturaUtil = ALTURA_SHAFT - margen * 2;
        double porcentaje = (double)(piso - 1) / (TOTAL_PISOS - 1);
        return ALTURA_SHAFT - margen - (porcentaje * alturaUtil);
    }

    // =====================================================================
    // ACTUALIZACION EN TIEMPO REAL (llamada desde callbacks del controlador)
    // =====================================================================

    /**
     * Actualiza la posicion y color de un ascensor en el shaft.
     * Este metodo se llama desde Platform.runLater() del controlador.
     */
    public void actualizarAscensor(Ascensor ascensor) {
        Rectangle cabina = cabinas.get(ascensor.getId());
        Label numero     = ledsNumero.get(ascensor.getId());
        Label flecha     = ledsFlecha.get(ascensor.getId());

        if (cabina == null || numero == null || flecha == null) return;

        EstadoAscensor estado = ascensor.getEstado();
        String color = Estilos.colorEstado(estado);
        String colorLed = Estilos.colorLed(estado);

        // Mover cabina con transicion suave
        double nuevaY = calcularY(ascensor.getPisoActual()) - 11;
        javafx.animation.TranslateTransition tt =
                new javafx.animation.TranslateTransition(
                        javafx.util.Duration.millis(200), cabina
                );
        tt.setToY(nuevaY - cabina.getLayoutY());
        tt.play();

        // Color de la cabina
        cabina.setStyle(
                "-fx-fill: " + color + ";" +
                        "-fx-stroke: " + color + "99;" +
                        "-fx-stroke-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, " + color + "88, 10, 0, 0, 0);"
        );

        // LED
        numero.setText(String.format("%02d", ascensor.getPisoActual()));
        numero.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: monospace;" +
                        "-fx-text-fill: " + colorLed + ";" +
                        "-fx-effect: dropshadow(gaussian, " + colorLed + ", 6, 0, 0, 0);"
        );

        flecha.setText(Estilos.simboloEstado(estado));
        flecha.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: " + colorLed + ";"
        );
    }

    public void actualizarPanelColas() {
        panelColas.getChildren().removeIf(
                n -> !(n instanceof HBox h && h.getChildren().size() == 2
                        && h.getChildren().get(0) instanceof Rectangle)
        );

        int totalActivas = 0;
        for (Edificio edificio : controlador.getEdificios()) {
            int tamanio = controlador.getTamanioColaEdificio(edificio.getId());
            totalActivas += tamanio;

            HBox fila = construirFilaInfo(
                    edificio.getNombre(),
                    tamanio + " pendientes",
                    Estilos.colorEdificio(edificio.getId())
            );
            panelColas.getChildren().add(fila);

            // Actualizar footer y barra del edificio
            Label footer = footersEdificio.get(edificio.getId());
            if (footer != null) {
                footer.setText("Cola: " + tamanio + "/20    3 ascensores");
            }
            Rectangle barra = barrasEdificio.get(edificio.getId());
            if (barra != null && barra.getParent() instanceof StackPane sp) {
                double ancho = sp.getWidth() > 0 ? sp.getWidth() : 200;
                barra.setWidth((tamanio / 20.0) * ancho);
                String colorBarra = tamanio >= 16 ? "#ef4444"
                        : tamanio >= 11 ? "#f59e0b" : "#6366f1";
                barra.setStyle("-fx-fill: " + colorBarra + ";");
            }
        }

        // Fila "en proceso"
        long enProceso = controlador.getEdificios().stream()
                .flatMap(e -> e.getAscensores().stream())
                .filter(a -> a.getEstado() != EstadoAscensor.INACTIVO)
                .count();

        panelColas.getChildren().add(
                construirFilaInfo("En proceso",
                        enProceso + " ascensores",
                        "#8b95a3")
        );

        labelActivas.setText(String.valueOf(totalActivas));
    }

    public void actualizarPanelHistorial() {
        panelHistorial.getChildren().removeIf(
                n -> !(n instanceof HBox h && h.getChildren().size() == 2
                        && h.getChildren().get(0) instanceof Rectangle)
        );

        for (Edificio edificio : controlador.getEdificios()) {
            for (Ascensor ascensor : edificio.getAscensores()) {
                Pila<String> pila = controlador.getHistorial(ascensor.getId());
                if (pila == null || pila.estaVacia()) continue;

                String ultimo = pila.verTope();
                HBox fila = construirFilaInfo(
                        ascensor.getId(),
                        ultimo,
                        Estilos.colorEdificio(edificio.getId())
                );
                panelHistorial.getChildren().add(fila);
            }
        }

        if (panelHistorial.getChildren().size() == 1) {
            Label empty = new Label("Sin paradas registradas");
            empty.setStyle(Estilos.ESTILO_LABEL_MUTED);
            panelHistorial.getChildren().add(empty);
        }
    }

    public void actualizarPanelMetricas() {
        panelMetricas.getChildren().removeIf(
                n -> !(n instanceof HBox h && h.getChildren().size() == 2
                        && h.getChildren().get(0) instanceof Rectangle)
        );

        int totalSolicitudes = controlador.getEdificios().stream()
                .mapToInt(e -> controlador.getTamanioColaEdificio(e.getId()))
                .sum();

        long inactivos = controlador.getEdificios().stream()
                .flatMap(e -> e.getAscensores().stream())
                .filter(a -> a.getEstado() == EstadoAscensor.INACTIVO)
                .count();

        panelMetricas.getChildren().addAll(
                construirFilaInfo("Total en cola",
                        totalSolicitudes + " solicitudes", "#8b95a3"),
                construirFilaInfo("Ascensores libres",
                        inactivos + "/9", "#22c55e"),
                construirFilaInfo("Viajes completados",
                        controlador.getTotalViajes() + "", "#6366f1")
        );

        labelViajes.setText(String.valueOf(controlador.getTotalViajes()));
    }

    private HBox construirFilaInfo(String clave, String valor, String color) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(8, 12, 8, 12));
        fila.setMaxWidth(Double.MAX_VALUE);   // ← AGREGAR
        fila.setStyle(
                "-fx-background-color: #232b35;" +
                        "-fx-background-radius: 6;"
        );

        Label lblClave = new Label(clave);
        lblClave.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: " + color + ";"
        );

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        Label lblValor = new Label(valor);
        lblValor.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #e8ecf1;"
        );

        fila.getChildren().addAll(lblClave, esp, lblValor);
        return fila;
    }

    // =====================================================================
    // ACTIVAR (registrar callbacks con el controlador)
    // =====================================================================

    /**
     * Se llama cada vez que se navega a esta vista.
     * Registra los callbacks para que el controlador actualice la UI.
     */
    public void activar() {
        controlador.setOnAscensorCambio(ascensor ->
                actualizarAscensor(ascensor)
        );
        controlador.setOnColaCambio(edificioId -> {
            actualizarPanelColas();
            actualizarPanelMetricas();
        });
        controlador.setOnHistorialCambio(() ->
                actualizarPanelHistorial()
        );
        controlador.setOnTotalViajesCambio(total -> {
            labelViajes.setText(String.valueOf(total));
            actualizarPanelMetricas();
        });

        actualizarPanelColas();
        actualizarPanelHistorial();
        actualizarPanelMetricas();
    }

    public Node getVista() { return raiz; }
}