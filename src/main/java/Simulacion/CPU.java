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
                // Verificar estado ANTES de ejecutar
                if (procesoActual.getProcessState() != Status.Running || procesoActual.End()) {
                    logger.log(String.format("CPU: Proceso %s no está listo para ejecutar (Estado: %s) - Liberando", 
                        procesoActual.getName(), procesoActual.getProcessState()));
                    
                    if (procesoActual.End()) {
                        processManager.EndProcess();
                    } else if (procesoActual.getProcessState() == Status.Blocked) {
                        // Ya debería estar en cola de bloqueados, solo limpiar
                        processManager.setRunningProcess(null);
                    }
                    
                    procesoActual = null;
                    semaforoCPU.liberar();
                    continue;
                }
                
                // Ejecutar instrucción
                boolean instruccionEjecutada = procesoActual.executeInstruction();
                ciclosReloj++;

                if (instruccionEjecutada) {
                    logger.log(String.format("[Ciclo %d] Proceso %s - PC: %d/%d - MAR: %d",
                            Clock.getInstance().getCurrentCycle(),
                            procesoActual.getName(),
                            procesoActual.getPC(),
                            procesoActual.getTotal_Instructions(),
                            procesoActual.getMar()));

                    // Verificar si terminó después de ejecutar
                    if (procesoActual.End()) {
                        logger.log(String.format("[Ciclo %d] Proceso %s COMPLETADO", 
                            Clock.getInstance().getCurrentCycle(), procesoActual.getName()));
                        processManager.EndProcess();
                        procesoActual = null;
                    } 
                    // Verificar excepción I/O
                    else if (procesoActual.generate_EXC()) {
                        logger.log(String.format("[Ciclo %d] ¡EXCEPCIÓN I/O! Proceso %s solicita E/S", 
                            Clock.getInstance().getCurrentCycle(), procesoActual.getName()));
                        
                        // IMPORTANTE: Configurar los ciclos de E/S antes de bloquear
                        procesoActual.setCiclosPendientes(procesoActual.getCiclosPendientes());
                        
                        processManager.BlockCurrentProcess();
                        procesoActual = null;
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
