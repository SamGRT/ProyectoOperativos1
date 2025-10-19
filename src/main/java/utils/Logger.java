package utils;

import Simulacion.Clock;

/**
 *
 * @author sarazo
 */
public class Logger {
    private static Logger instancia;
    private StringBuilder log;
    
    private Logger() {
        log = new StringBuilder();
    }
    
    public static synchronized Logger getInstancia() {
        if (instancia == null) {
            instancia = new Logger();
        }
        return instancia;
    }
    
    public void log(String mensaje) {
        int cicloActual = Clock.getInstance().getCurrentCycle();
        String entradaLog = String.format("[Ciclo %d] %s%n", cicloActual, mensaje);
        log.append(entradaLog);
        System.out.print(entradaLog);
    }
    
    public String getLog() {
        return log.toString();
    }
    
    public void LimpiarLog() {
        log = new StringBuilder();
    }
}
