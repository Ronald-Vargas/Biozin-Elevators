package com.elevators.vista.componentes;

/**
 * Constantes de estilos visuales del sistema.
 * Centralizar los colores aquí facilita cambiar el tema
 * sin tocar todas las vistas.
 */
public class Estilos {

    // ===== Colores base =====
    public static final String BG_PRINCIPAL     = "#0f1419";
    public static final String BG_PANEL         = "#1a2028";
    public static final String BG_PANEL_2       = "#232b35";
    public static final String BG_SHAFT         = "#0a0e12";
    public static final String BG_LED           = "#061018";

    // ===== Bordes =====
    public static final String BORDE            = "#2d3540";
    public static final String BORDE_FUERTE     = "#3d4654";

    // ===== Texto =====
    public static final String TEXTO            = "#e8ecf1";
    public static final String TEXTO_MUTED      = "#8b95a3";
    public static final String TEXTO_DIM        = "#5a636f";

    // ===== Colores de estado de ascensores =====
    public static final String COLOR_SUBIENDO   = "#22c55e";  // Verde
    public static final String COLOR_BAJANDO    = "#3b82f6";  // Azul
    public static final String COLOR_PUERTAS    = "#f59e0b";  // Ámbar
    public static final String COLOR_INACTIVO   = "#232b35";  // Gris oscuro

    // ===== LED display =====
    public static final String LED_SUBIENDO     = "#4ade80";  // Verde brillante
    public static final String LED_BAJANDO      = "#60a5fa";  // Azul brillante
    public static final String LED_PUERTAS      = "#fbbf24";  // Ámbar brillante

    // ===== Acento =====
    public static final String ACENTO           = "#6366f1";
    public static final String ACENTO_2         = "#818cf8";

    // ===== Colores por edificio =====
    public static final String COLOR_EDIFICIO_1 = "#3b82f6";
    public static final String COLOR_EDIFICIO_2 = "#f59e0b";
    public static final String COLOR_EDIFICIO_3 = "#818cf8";

    // ===== Estilos completos reutilizables =====
    public static final String ESTILO_PANEL =
            "-fx-background-color: " + BG_PANEL + ";" +
                    "-fx-border-color: " + BORDE + ";" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 14;" +
                    "-fx-background-radius: 14;";

    public static final String ESTILO_LABEL_TITULO =
            "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: " + TEXTO + ";";

    public static final String ESTILO_LABEL_MUTED =
            "-fx-font-size: 11px;" +
                    "-fx-text-fill: " + TEXTO_MUTED + ";";

    public static final String ESTILO_LED =
            "-fx-background-color: " + BG_LED + ";" +
                    "-fx-border-color: " + BORDE + ";" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 6;" +
                    "-fx-background-radius: 6;";

    /**
     * Retorna el color CSS del estado de un ascensor.
     */
    public static String colorEstado(
            com.elevators.modelo.EstadoAscensor estado) {
        return switch (estado) {
            case SUBIENDO        -> COLOR_SUBIENDO;
            case BAJANDO         -> COLOR_BAJANDO;
            case ABRIENDO_PUERTAS -> COLOR_PUERTAS;
            case INACTIVO        -> COLOR_INACTIVO;
        };
    }

    /**
     * Retorna el color LED según el estado.
     */
    public static String colorLed(
            com.elevators.modelo.EstadoAscensor estado) {
        return switch (estado) {
            case SUBIENDO        -> LED_SUBIENDO;
            case BAJANDO         -> LED_BAJANDO;
            case ABRIENDO_PUERTAS -> LED_PUERTAS;
            case INACTIVO        -> TEXTO_DIM;
        };
    }

    /**
     * Retorna el símbolo de dirección según el estado.
     */
    public static String simboloEstado(
            com.elevators.modelo.EstadoAscensor estado) {
        return switch (estado) {
            case SUBIENDO        -> "▲";
            case BAJANDO         -> "▼";
            case ABRIENDO_PUERTAS -> "◇";
            case INACTIVO        -> "·";
        };
    }

    /**
     * Retorna el color del edificio por su ID.
     */
    public static String colorEdificio(int edificioId) {
        return switch (edificioId) {
            case 1  -> COLOR_EDIFICIO_1;
            case 2  -> COLOR_EDIFICIO_2;
            default -> COLOR_EDIFICIO_3;
        };
    }

    // Constructor privado: esta clase no se instancia
    private Estilos() {}
}
