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
public class Garantizado implements AlgoritmoPlanificacion {
    private Cola colaListos;
    private Proceso procesoActual;
    private Semaforo semaforoCola;
    private Logger logger;
    private int totalProcesos;
    private int ciclosTotales;
    
    public Garantizado() {
        this.colaListos = new Cola();
        this.semaforoCola = new Semaforo(1);
        this.logger = Logger.getInstancia();
        this.totalProcesos = 0;
        this.ciclosTotales = 0;
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            proceso.setProcessState(Status.Ready);
            colaListos.encolar(proceso);
            totalProcesos++;
            logger.log(String.format("Proceso %s agregado a cola Garantizado", proceso.getName()));
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Error Garantizado al agregar proceso: " + e.getMessage());
        }
    }
    
    @Override
    public Proceso obtenerSiguienteProceso() {
        try {
            semaforoCola.adquirir();
            
            if (procesoActual != null && !procesoActual.End()) {
                // Garantizar tiempo equitativo
                int tiempoGarantizado = Math.max(1, ciclosTotales / totalProcesos);
                if (procesoActual.getCiclos_ejecutados() < tiempoGarantizado) {
                    semaforoCola.liberar();
                    return procesoActual;
                }
            }
            
            // Rotar procesos para garantizar equidad
            if (procesoActual != null && !procesoActual.End()) {
                procesoActual.setProcessState(Status.Ready);
                colaListos.encolar(procesoActual);
            }
            
            Proceso siguiente = colaListos.desencolar();
            if (siguiente != null) {
                siguiente.setProcessState(Status.Running);
                ciclosTotales++;
                logger.log(String.format("Garantizado selecciona Proceso %s (Ciclo %d)", 
                    siguiente.getName(), ciclosTotales));
            }
            procesoActual = siguiente;
            semaforoCola.liberar();
            return siguiente;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    @Override
    public Cola getColaListos() {
        return colaListos;
    }
    
    @Override
    public String getNombre() {
        return "Garantizado (Equitativo)";
    }
}