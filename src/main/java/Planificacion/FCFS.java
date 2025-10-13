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
public class FCFS {
    private Cola colaListos;
    private Proceso procesoActual;
    private Semaforo semaforoCola;
    private Logger logger;
    
    public FCFS() {
        this.colaListos = new Cola();
        this.semaforoCola = new Semaforo(1);
        this.logger = Logger.getInstancia();
    }
    
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
    
    public Proceso obtenerSiguienteProceso() {
        try {
            semaforoCola.adquirir();
            
            if (procesoActual != null && !procesoActual.End()) {
                semaforoCola.liberar();
                return procesoActual;
            }
            
            Proceso siguiente = colaListos.desencolar();
            if (siguiente != null) {
                siguiente.setProcessState(Status.Running);
                logger.log(String.format("Planificador FCFS selecciona Proceso %s", siguiente.getName()));
            }
            procesoActual = siguiente;
            semaforoCola.liberar();
            return siguiente;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Error al obtener el siguiente proceso: " + e.getMessage());
            return null;
        }
    }
    
    public void devolverProceso(Proceso proceso) {
        if (proceso != null && !proceso.End()) {
            agregarProceso(proceso);
        }
    }
    
    public Cola getColaListos() {
        return colaListos;
    }
    
    public Proceso getProcesoActual() {
        return procesoActual;
    }
    
    public int getTamañoCola() {
        return colaListos.size();
    }
}
