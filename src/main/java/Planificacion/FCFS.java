package Planificacion;

import model.Proceso;
import model.Status;
import Edd.Cola;
import utils.Logger;
import utils.Semaforo;

/**
 *
 * @author sarazo
 */
public class FCFS implements AlgoritmoPlanificacion {
    private Cola colaListos;
    private Proceso procesoActual;
    private Semaforo semaforoCola;
    private Logger logger;
    private Cola colaExterna; // Referencia a la cola del ProcessManager
    
    public FCFS() {
        this.colaListos = new Cola();
        this.semaforoCola = new Semaforo(1);
        this.logger = Logger.getInstancia();
         this.colaExterna = null; // Se establecerá después
    }
     public void setColaExterna(Cola cola) {
        this.colaExterna = cola;
    }
    @Override
     public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            proceso.setProcessState(Status.Ready);
            
            logger.log(String.format("Proceso %s marcado como Ready para FCFS", proceso.getName()));
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
            
            // DEBUG: Mostrar estado de la cola EXTERNA
            if (colaExterna != null) {
                System.out.println("[DEBUG FCFS] Cola Externa size: " + colaExterna.size() + 
                                  ", Proceso actual: " + (procesoActual != null ? procesoActual.getName() : "null"));
            }
            
            // SIEMPRE limpiar procesoActual si no es válido
            if (procesoActual != null) {
                boolean procesoValido = (procesoActual.getProcessState() == Status.Ready || 
                                        procesoActual.getProcessState() == Status.Running) && 
                                       !procesoActual.End();
                
                if (!procesoValido) {
                    System.out.println("[DEBUG FCFS] Limpiando proceso inválido: " + procesoActual.getName());
                    procesoActual = null;
                }
            }
            
            // BUSCAR NUEVO PROCESO si no hay uno actual Y la cola externa tiene procesos
            if (procesoActual == null && colaExterna != null && !colaExterna.isEmpty()) {
                procesoActual = colaExterna.desencolar();
                if (procesoActual != null) {
                    System.out.println("[DEBUG FCFS] Nuevo proceso seleccionado: " + procesoActual.getName());
                }
            }
            
            semaforoCola.liberar();
            return procesoActual;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    

    public void liberarProcesoActual() {
    try {
        semaforoCola.adquirir();
        if (procesoActual != null) {
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
        return colaListos;
    }

    @Override
    public String getNombre() {
        return "FCFS (First Come First Served)";
    }
    
    public Proceso getProcesoActual() {
        return procesoActual;
    }
    
    public int getTamañoCola() {
        return colaListos.size();
    }
}
