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
public class RoundRobin implements AlgoritmoPlanificacion {
    private ProcessManager processManager;
    private Proceso procesoActual;
    private Semaforo semaforoCola;
    private Logger logger;
    private int quantum;
    private int contadorQuantum;
    
    public RoundRobin(ProcessManager processManager) {
        this.processManager= processManager;
        this.semaforoCola = new Semaforo(1);
        this.logger = Logger.getInstancia();
        this.quantum = 3;
        this.contadorQuantum = 0;
    }
    
    public RoundRobin(int quantum,ProcessManager processManager) {
        this(processManager);
        this.quantum = quantum;
    }
    
  @Override
public void agregarProceso(Proceso proceso) {
    try {
        semaforoCola.adquirir();
        
        // ✅ SOLUCIÓN: Solo cambiar estado, NO encolar nuevamente
        // El proceso YA debe estar en la cola ready del ProcessManager
        proceso.setProcessState(Status.Ready);
        
        logger.log(String.format("Proceso %s preparado para Round Robin", proceso.getName()));
        semaforoCola.liberar();
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.log("Error Round Robin al agregar proceso: " + e.getMessage());
    }
}
    
   @Override
public Proceso obtenerSiguienteProceso() {
    try {
        semaforoCola.adquirir();
        
        // DEBUG DETALLADO
        System.out.println("[DEBUG RoundRobin] INICIO - Proceso actual: " + 
            (procesoActual != null ? procesoActual.getName() : "null") +
            ", Quantum: " + contadorQuantum + "/" + quantum +
            ", Estado: " + (procesoActual != null ? procesoActual.getProcessState() : "N/A"));
        
        // VERIFICAR SI EL PROCESO ACTUAL SIGUE SIENDO VÁLIDO
        if (procesoActual != null) {
            // Si el proceso está bloqueado, terminado o suspendido, limpiarlo
            if (procesoActual.getProcessState() == Status.Blocked || 
                procesoActual.getProcessState() == Status.Blocked_Suspended ||
                procesoActual.End()) {
                
                System.out.println("[DEBUG RoundRobin] Proceso " + procesoActual.getName() + 
                    " no está disponible (Estado: " + procesoActual.getProcessState() + ") - Limpiando");
                procesoActual = null;
                contadorQuantum = 0;
            }
            // Si el proceso está listo pero agotó el quantum, devolver a cola
            else if (procesoActual.getProcessState() == Status.Ready && contadorQuantum >= quantum) {
                System.out.println("[DEBUG RoundRobin] QUANTUM AGOTADO - Proceso " + procesoActual.getName() + 
                    " devuelto a cola");
                if (processManager != null && processManager.getC_Ready() != null) {
                    processManager.getC_Ready().encolar(procesoActual);
                }
                logger.log(String.format("Quantum agotado - Proceso %s devuelto a cola", procesoActual.getName()));
                procesoActual = null;
                contadorQuantum = 0;
            }
        }
        
        // SI NO HAY PROCESO ACTUAL VÁLIDO, BUSCAR NUEVO
        if (procesoActual == null) {
            Proceso siguiente = null;
            if (processManager != null && processManager.getC_Ready() != null) {
                siguiente = processManager.getC_Ready().desencolar();
            }
            
            if (siguiente != null) {
                siguiente.setProcessState(Status.Running);
                procesoActual = siguiente;
                contadorQuantum = 1; // Empezar nuevo quantum
                System.out.println("[DEBUG RoundRobin] NUEVO PROCESO seleccionado: " + 
                    siguiente.getName() + " (quantum: " + contadorQuantum + "/" + quantum + ")");
                logger.log(String.format("Planificador Round Robin selecciona Proceso %s (Quantum %d)", 
                    siguiente.getName(), quantum));
            } else {
                System.out.println("[DEBUG RoundRobin] No hay procesos disponibles en cola");
            }
        } 
        // SI HAY PROCESO ACTUAL VÁLIDO, CONTINUAR EJECUTANDOLO
        else if (procesoActual.getProcessState() == Status.Running) {
            contadorQuantum++;
            System.out.println("[DEBUG RoundRobin] CONTINUANDO con proceso: " + procesoActual.getName() + 
                " (quantum: " + contadorQuantum + "/" + quantum + ")");
        }
        
        semaforoCola.liberar();
        return procesoActual;
        
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return null;
    }
}

public void notificarBloqueo(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            if (procesoActual == proceso) {
                System.out.println("[DEBUG RoundRobin] Notificado bloqueo de proceso: " + proceso.getName());
                procesoActual = null;
                contadorQuantum = 0;
            }
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

public void notificarFinalizacion(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            if (procesoActual == proceso) {
                System.out.println("[DEBUG RoundRobin] Notificado finalización de proceso: " + proceso.getName());
                procesoActual = null;
                contadorQuantum = 0;
            }
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
     @Override
    public Cola getColaListos() {
        // Devolver la cola del ProcessManager, no una cola interna
        return processManager != null ? processManager.getC_Ready() : null;
    }
    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }
    
    public int getQuantum() {
        return quantum;
    }
    
   
    
    @Override
    public String getNombre() {
        return "Round Robin (Quantum: " + quantum + ")";
    }
}
