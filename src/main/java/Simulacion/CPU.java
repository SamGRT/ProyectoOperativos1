package Simulacion;

import model.Proceso;
import model.Status;
import utils.Logger;
import utils.Semaforo;

/**
 *
 * @author sarazo
 */
public class CPU implements Runnable {
    private Proceso procesoActual;
    private boolean ejecutando;
    private Thread hiloCPU;
    private Semaforo semaforoCPU;
    private Logger logger;
    private int ciclosReloj;
    
    public CPU() {
        this.ejecutando = false;
        this.semaforoCPU = new Semaforo(1);
        this.logger = Logger.getInstancia();
        this.ciclosReloj = 0;
    }
    
    public void iniciar() {
        if (!ejecutando) {
            ejecutando = true;
            hiloCPU = new Thread(this, "Hilo-CPU");
            hiloCPU.start();
            logger.log("CPU iniciada");
        }
    }
    
    public void detener() {
        ejecutando = false;
        if (hiloCPU != null) {
            hiloCPU.interrupt();
        }
        logger.log("CPU detenida");
    }
    
    public void ejecutarProceso(Proceso proceso) {
        try {
            semaforoCPU.adquirir();
            this.procesoActual = proceso;
            if (proceso != null) {
                logger.log(String.format("CPU ejecutando proceso: %s", proceso.getName()));
            }
            semaforoCPU.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Error al ejecutar el proceso: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        while (ejecutando && !Thread.currentThread().isInterrupted()) {
            try {
                semaforoCPU.adquirir();
                
                if (procesoActual != null) {
                    procesoActual.executeInstruction();
                    ciclosReloj++;
                    
                    logger.log(String.format("Proceso %s - PC: %d/%d - MAR: %d",
                            procesoActual.getName(),
                            procesoActual.getPC(),
                            procesoActual.getTotal_Instructions(),
                            procesoActual.getMar()));
                    
                    if (procesoActual.End()) {
                        procesoActual.setProcessState(Status.Finished);
                        logger.log(String.format("Proceso %s TERMINADO", procesoActual.getName()));
                    }
                }
                
                semaforoCPU.liberar();
                Thread.sleep(Clock.getInstance().getCycleDuration());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public Proceso getProcesoActual() {
        return procesoActual;
    }
    
    public int getCiclosReloj() {
        return ciclosReloj;
    }
    
    public boolean isEjecutando() {
        return ejecutando;
    }
}
