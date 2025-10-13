package Simulacion;

import Planificacion.FCFS;
import model.Proceso;
import utils.Logger;

/**
 *
 * @author sarazo
 */
public class Planificador implements Runnable {
    private FCFS planificadorFCFS;
    private CPU cpu;
    private boolean ejecutando;
    private Thread hiloPlanificador;
    private Logger logger;
    
    public Planificador(CPU cpu) {
        this.planificadorFCFS = new FCFS();
        this.cpu = cpu;
        this.ejecutando = false;
        this.logger = Logger.getInstancia();
    }
    
    public void iniciar() {
        if (!ejecutando) {
            ejecutando = true;
            hiloPlanificador = new Thread(this, "Hilo-Planificador");
            hiloPlanificador.start();
            logger.log("Planificador iniciado con política FCFS");
        }
    }
    
    public void detener() {
        ejecutando = false;
        if (hiloPlanificador != null) {
            hiloPlanificador.interrupt();
        }
        logger.log("Planificador detenido");
    }
    
    @Override
    public void run() {
        while (ejecutando && !Thread.currentThread().isInterrupted()) {
            try {
                Proceso siguienteProceso = planificadorFCFS.obtenerSiguienteProceso();
                if (siguienteProceso != null) {
                    cpu.ejecutarProceso(siguienteProceso);
                }
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public void agregarProceso(Proceso proceso) {
        planificadorFCFS.agregarProceso(proceso);
    }
    
    public FCFS getPlanificadorFCFS() {
        return planificadorFCFS;
    }
    
    public boolean isEjecutando() {
        return ejecutando;
    }
}
