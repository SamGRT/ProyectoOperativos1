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
    
    public FCFS() {
        this.colaListos = new Cola();
        this.semaforoCola = new Semaforo(1);
        this.logger = Logger.getInstancia();
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            proceso.setProcessState(Status.Ready);
            colaListos.encolar(proceso);
            logger.log(String.format("Proceso %s agregado a cola FCFS", proceso.getName()));
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
        
        // SIEMPRE verificar si el proceso actual sigue siendo válido
        if (procesoActual != null) {
            boolean procesoValido = (procesoActual.getProcessState() == Status.Running) && 
                                   !procesoActual.End() && 
                                   colaListos.contiene(procesoActual);
            
            if (!procesoValido) {
                logger.log(String.format("FCFS: Proceso actual %s no es válido (Estado: %s), limpiando", 
                    procesoActual.getName(), procesoActual.getProcessState()));
                procesoActual = null;
            }
        }
        
        // SIEMPRE intentar obtener nuevo proceso si no hay uno actual
        if (procesoActual == null) {
            procesoActual = colaListos.desencolar();
            if (procesoActual != null) {
                procesoActual.setProcessState(Status.Running);
                logger.log(String.format("Planificador FCFS selecciona Proceso %s", procesoActual.getName()));
            }
        }
        
        semaforoCola.liberar();
        return procesoActual;
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.log("Error al obtener el siguiente proceso: " + e.getMessage());
        return null;
    }
}
    public void liberarProcesoActual() {
        try {
            semaforoCola.adquirir();
            if (procesoActual != null) {
                logger.log(String.format("FCFS: Liberando proceso actual %s", procesoActual.getName()));
                procesoActual = null;
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
