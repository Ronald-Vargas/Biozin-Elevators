package com.elevators.vista;

import com.elevators.controlador.ControladorSistema;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Ventana principal de la aplicación.
 * Contiene un StackPane raíz donde se intercambian las vistas
 * sin cerrar ni recrear la ventana.
 *
 * Flujo:
 *   VistaPrincipal → VistaInicio → VistaMonitoreo
 *                               → VistaLobby → VistaAscensor
 */
public class VistaPrincipal {

    private final Stage stage;
    private final StackPane raiz;
    private final Scene scene;
    private final ControladorSistema controlador;

    // Vistas disponibles (se crean una vez y se reutilizan)
    private VistaInicio vistaInicio;
    private VistaMonitoreo vistaMonitoreo;
    private VistaLobby vistaLobby;
    private VistaAscensor vistaAscensor;

    public VistaPrincipal(Stage stage) {
        this.stage = stage;
        this.raiz = new StackPane();
        this.scene = new Scene(raiz, 1280, 780);
        this.controlador = ControladorSistema.getInstance();

        // Iniciar la simulación al arrancar
        controlador.iniciar();

        // Aplicar estilos globales
        aplicarEstilos();

        // Manejar cierre de ventana
        stage.setOnCloseRequest(e -> controlador.detener());
    }

    public void mostrar() {
        stage.setTitle("Sistema Inteligente de Ascensores");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();

        // Mostrar la pantalla de inicio
        irAInicio();
    }

    // ===== Navegación =====

    public void irAInicio() {
        if (vistaInicio == null) {
            vistaInicio = new VistaInicio(this);
        }
        cambiarVista(vistaInicio.getVista());
    }

    public void irAMonitoreo() {
        if (vistaMonitoreo == null) {
            vistaMonitoreo = new VistaMonitoreo(this);
        }
        vistaMonitoreo.activar();
        cambiarVista(vistaMonitoreo.getVista());
    }

    public void irALobby() {
        if (vistaLobby == null) {
            vistaLobby = new VistaLobby(this);
        }
        vistaLobby.reset();
        cambiarVista(vistaLobby.getVista());
    }

    public void irAVistaAscensor(int edificioId,
                                 int pisoOrigen,
                                 String ascensorId) {
        if (vistaAscensor == null) {
            vistaAscensor = new VistaAscensor(this);
        }
        vistaAscensor.configurar(edificioId, pisoOrigen, ascensorId);
        cambiarVista(vistaAscensor.getVista());
    }

    /**
     * Intercambia el contenido del StackPane con la nueva vista.
     * La animación de entrada está en cada vista.
     */
    private void cambiarVista(javafx.scene.Node nuevaVista) {
        raiz.getChildren().setAll(nuevaVista);
    }

    private void aplicarEstilos() {
        // Color de fondo global de la app
        raiz.setStyle("-fx-background-color: #0f1419;");
    }

    // ===== Getters =====

    public Stage getStage() { return stage; }
    public ControladorSistema getControlador() { return controlador; }
}
