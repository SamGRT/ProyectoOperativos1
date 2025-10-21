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
    private ExceptionHandler manejadorExcepciones;
    private ProcessManager processManager;
    
    public CPU(ProcessManager processManager) {
        this.processManager = processManager;
        this.ejecutando = false;
        this.semaforoCPU = new Semaforo(1);
        this.logger = Logger.getInstancia();
        this.ciclosReloj = 0;
        this.manejadorExcepciones = new ExceptionHandler(processManager);
    }

  
    
    public void iniciar() {
        if (!ejecutando) {
            ejecutando = true;
            hiloCPU = new Thread(this, "Hilo-CPU");
            hiloCPU.start();
            manejadorExcepciones.iniciar();
            logger.log("CPU y manejador de excepciones iniciados");
        }
    }
    
    public void detener() {
        ejecutando = false;
        manejadorExcepciones.detener();
        if (hiloCPU != null) {
            hiloCPU.interrupt();
        }
        logger.log("CPU y manejador de excepciones detenidos");
    }
    
   public void ejecutarProceso(Proceso proceso) {
    try {
        semaforoCPU.adquirir();
        this.procesoActual = proceso;
        
        // CORRECCIÓN: Verificar que processManager no sea null
        if (processManager != null) {
            if (proceso != null) {
                processManager.setRunningProcess(proceso); 
                logger.log(String.format("CPU ejecutando proceso: %s", proceso.getName()));
            } else {
                processManager.setRunningProcess(null);
            }
        } else {
            logger.log("ADVERTENCIA: ProcessManager es null en CPU");
        }
        
        semaforoCPU.liberar();
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.log("Error al ejecutar el proceso: " + e.getMessage());
    }
}
    
   @Override
public void run() {
    Clock.getInstance().start();
    while (ejecutando && !Thread.currentThread().isInterrupted()) {
        try {
            Clock.getInstance().incrementCycle();
            semaforoCPU.adquirir();

            if (procesoActual != null && processManager != null) {
                // ✅ VERIFICAR SI TERMINÓ ANTES de ejecutar
                if (procesoActual.End()) {
                    logger.log(String.format("Proceso %s TERMINADO - Moviendo a Finished", 
                        procesoActual.getName()));
                    processManager.EndProcess();
                    procesoActual = null;
                    processManager.setRunningProcess(null);
                    
                    semaforoCPU.liberar();
                    continue; // Pasar al siguiente ciclo
                }
                
                // Si no terminó, ejecutar instrucción
                boolean instruccionEjecutada = procesoActual.executeInstruction();
                ciclosReloj++;

                if (instruccionEjecutada) {
                    logger.log(String.format("Proceso %s - PC: %d/%d - MAR: %d",
                            procesoActual.getName(),
                            procesoActual.getPC(),
                            procesoActual.getTotal_Instructions(),
                            procesoActual.getMar()));

                    // VERIFICAR SI TERMINÓ DESPUÉS de ejecutar
                    if (procesoActual.End()) {
                        logger.log(String.format("Proceso %s COMPLETADO", procesoActual.getName()));
                        processManager.EndProcess();
                        procesoActual = null;
                        processManager.setRunningProcess(null);
                        
                        semaforoCPU.liberar();
                        continue;
                    }

                    // VERIFICAR EXCEPCIÓN I/O
                    if (procesoActual.generate_EXC()) {
                        logger.log(String.format("¡EXCEPCIÓN I/O! Proceso %s solicita E/S", 
                            procesoActual.getName()));
                        processManager.BlockCurrentProcess();
                        procesoActual = null;
                        processManager.setRunningProcess(null);
                    }
                }
            }

            semaforoCPU.liberar();
            Thread.sleep(Clock.getInstance().getCycleDuration());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
        }
    }
    Clock.getInstance().stop();
}
    
    public ExceptionHandler getManejadorExcepciones() {
        return manejadorExcepciones;
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
