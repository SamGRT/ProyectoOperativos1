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
        
        // DEBUG
        System.out.println("[DEBUG RoundRobin] INICIO - Proceso: " + 
            (procesoActual != null ? procesoActual.getName() : "null") +
            ", Quantum: " + contadorQuantum + "/" + quantum +
            ", Estado: " + (procesoActual != null ? procesoActual.getProcessState() : "N/A"));

        // SOLUCIÓN 1: Verificar preemption por quantum agotado
        if (procesoActual != null && procesoActual.getProcessState() == Status.Running) {
           if (contadorQuantum >= quantum) {
        // preemption
        procesoActual.setProcessState(Status.Ready);
        processManager.getC_Ready().encolar(procesoActual);
        procesoActual = null;
        contadorQuantum = 0;
    } else {
        contadorQuantum++;
        return procesoActual;
    }
}
        

        // SOLUCIÓN 2: Limpiar proceso no válido
        if (procesoActual != null && 
            (procesoActual.getProcessState() != Status.Running || procesoActual.End())) {
            System.out.println("[DEBUG RoundRobin] Limpiando proceso no válido: " + procesoActual.getName());
            procesoActual = null;
            contadorQuantum = 0;
        }

        // Buscar nuevo proceso
        if (procesoActual == null) {
            Proceso siguiente = null;
            if (processManager != null && processManager.getC_Ready() != null) {
                siguiente = processManager.getC_Ready().desencolar();
            }
            
            if (siguiente != null) {
                siguiente.setProcessState(Status.Running);
                procesoActual = siguiente;
                contadorQuantum = 1; // Reiniciar contador
                System.out.println("[DEBUG RoundRobin] NUEVO PROCESO: " + siguiente.getName());
                logger.log(String.format("Planificador Round Robin selecciona Proceso %s", siguiente.getName()));
            }
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
