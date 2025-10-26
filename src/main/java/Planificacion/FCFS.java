package Planificacion;

import model.Proceso;
import model.Status;
import Edd.Cola;
import utils.Logger;
import utils.Semaforo;
import Simulacion.ProcessManager;
/**
 *
 * @author sarazo
 */
public class FCFS implements AlgoritmoPlanificacion {
    private ProcessManager processManager;
    private Proceso procesoActual;
    private Semaforo semaforoCola;
    private Logger logger;
 
    
    public FCFS(ProcessManager processManager) {
        this.processManager = processManager;
        this.semaforoCola = new Semaforo(1);
        this.logger = Logger.getInstancia();

    }

    @Override
     public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            proceso.setProcessState(Status.Ready);

              
           
            
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Error al agregar proceso: " + e.getMessage());
        }
    }
@Override
public Proceso obtenerSiguienteProceso() {
    try {
        semaforoCola.adquirir();
        
        // DEBUG: Mostrar estado de la cola del ProcessManager
        if (processManager != null && processManager.getC_Ready() != null) {
            System.out.println("[DEBUG FCFS] Cola ProcessManager size: " + processManager.getC_Ready().size() + 
                              ", Proceso actual: " + (procesoActual != null ? procesoActual.getName() : "null"));
        }
        
        // CORRECCIÓN: Verificar si el proceso actual puede continuar (SIN liberar semáforo)
        if (procesoActual != null && 
            procesoActual.getProcessState() == Status.Running && 
            !procesoActual.End()) {
            // El proceso actual puede continuar ejecutándose
            // NO liberar el semáforo aquí - mantenerlo hasta el final
            Proceso resultado = procesoActual;
            semaforoCola.liberar();
            return resultado;
        }

        // Limpiar procesoActual si no es válido
        if (procesoActual != null) {
            boolean procesoTerminado = procesoActual.End();
            boolean procesoBloqueado = procesoActual.getProcessState() == Status.Blocked;
            boolean procesoNoEjecutable = procesoActual.getProcessState() != Status.Running && 
                                         procesoActual.getProcessState() != Status.Ready;
            
            if (procesoTerminado || procesoBloqueado || procesoNoEjecutable) {
                System.out.println("[DEBUG FCFS] Liberando proceso actual: " + procesoActual.getName() + 
                                 " (Estado: " + procesoActual.getProcessState() + ", Terminado: " + procesoActual.End() + ")");
                procesoActual = null;
            }
        }
        
        // BUSCAR NUEVO PROCESO de la cola del ProcessManager
        Proceso siguiente = null;
        if (processManager != null && processManager.getC_Ready() != null && !processManager.getC_Ready().isEmpty()) {
            siguiente = processManager.getC_Ready().desencolar();
            
            // CORRECCIÓN: Validar que el proceso sea ejecutable
            if (siguiente != null && !siguiente.End() && 
                (siguiente.getProcessState() == Status.Ready || siguiente.getProcessState() == Status.Running)) {
                
                siguiente.setProcessState(Status.Running);
                procesoActual = siguiente;
                System.out.println("[DEBUG FCFS] Nuevo proceso seleccionado: " + siguiente.getName());
                logger.log(String.format("Planificador FCFS selecciona Proceso %s", siguiente.getName()));
            } else {
                // CORRECCIÓN: Si el proceso no es válido, encolarlo de nuevo
                if (siguiente != null && processManager.getC_Ready() != null) {
                    System.out.println("[DEBUG FCFS] Proceso no válido, re-encolando: " + siguiente.getName());
                    processManager.getC_Ready().encolar(siguiente);
                }
                siguiente = null;
            }
        }
        
        semaforoCola.liberar();
        return siguiente;
        
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return null;
    }
}
    public void liberarProcesoActual() {
    try {
        semaforoCola.adquirir();
        if (procesoActual != null && procesoActual.End()) {
            logger.log(String.format("FCFS: Liberando proceso actual %s (Estado: %s)", 
                procesoActual.getName(), procesoActual.getProcessState()));
            procesoActual = null;
        } else {
            logger.log("FCFS: No hay proceso actual para liberar");
        }
        semaforoCola.liberar();
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
    @Override
    public Cola getColaListos() {
        return processManager != null ? processManager.getC_Ready() : null;
    }

    @Override
    public String getNombre() {
        return "FCFS (First Come First Served)";
    }
    
    public Proceso getProcesoActual() {
        return procesoActual;
    }
    
    public int getTamañoCola() {
        return processManager != null && processManager.getC_Ready() != null ? 
               processManager.getC_Ready().size() : 0;
    }
}
