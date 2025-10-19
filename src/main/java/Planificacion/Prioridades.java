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
public class Prioridades implements AlgoritmoPlanificacion {
    private Cola colaListos;
    private Proceso procesoActual;
    private Semaforo semaforoCola;
    private Logger logger;
    
    public Prioridades() {
        this.colaListos = new Cola();
        this.semaforoCola = new Semaforo(1);
        this.logger = Logger.getInstancia();
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            proceso.setProcessState(Status.Ready);
            
            // Prioridad: 1 (más alta) a 5 (más baja)
            int prioridad = asignarPrioridad(proceso);
            
            int index = 0;
            while (index < colaListos.size()) {
                Proceso p = colaListos.get(index);
                int prioridadP = asignarPrioridad(p);
                if (prioridad < prioridadP) { // Números más bajos = mayor prioridad
                    break;
                }
                index++;
            }
            
            insertarEnPosicion(proceso, index);
            logger.log(String.format("Proceso %s agregado a cola Prioridades (Prioridad: %d)", 
                proceso.getName(), prioridad));
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Error Prioridades al agregar proceso: " + e.getMessage());
        }
    }
    
    private int asignarPrioridad(Proceso proceso) {
        // 1. Procesos I/O-bound tienen mayor prioridad (para mejor respuesta)
        // 2. Procesos más cortos tienen mayor prioridad
        // 3. Procesos que han esperado mucho tienen mayor prioridad (envejecimiento)
        
        int prioridad = 3; // Prioridad media por defecto
        
        if (!proceso.isCPUbound()) {
            prioridad = 1; // I/O-bound - máxima prioridad
        } else if (proceso.getTotal_Instructions() <= 5) {
            prioridad = 2; // Procesos cortos CPU-bound
        }
        return prioridad;
    }
    
    private void insertarEnPosicion(Proceso proceso, int index) {
        if (colaListos.size() == 0) {
            colaListos.encolar(proceso);
            return;
        }
        
        Cola nuevaCola = new Cola();
        for (int i = 0; i < index; i++) {
            nuevaCola.encolar(colaListos.get(i));
        }
        nuevaCola.encolar(proceso);
        for (int i = index; i < colaListos.size(); i++) {
            nuevaCola.encolar(colaListos.get(i));
        }
        this.colaListos = nuevaCola;
    }
    
    @Override
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
                logger.log(String.format("Planificador Prioridades selecciona Proceso %s (Prioridad: %d)", 
                    siguiente.getName(), asignarPrioridad(siguiente)));
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
        return "Prioridades (I/O-bound > Cortos > Largos)";
    }
}