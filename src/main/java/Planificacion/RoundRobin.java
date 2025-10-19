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
public class RoundRobin implements AlgoritmoPlanificacion {
    private Cola colaListos;
    private Proceso procesoActual;
    private Semaforo semaforoCola;
    private Logger logger;
    private int quantum;
    private int contadorQuantum;
    
    public RoundRobin() {
        this.colaListos = new Cola();
        this.semaforoCola = new Semaforo(1);
        this.logger = Logger.getInstancia();
        this.quantum = 3;
        this.contadorQuantum = 0;
    }
    
    public RoundRobin(int quantum) {
        this();
        this.quantum = quantum;
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            proceso.setProcessState(Status.Ready);
            colaListos.encolar(proceso);
            logger.log(String.format("Proceso %s agregado a cola Round Robin", proceso.getName()));
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
            
            //Si hay proceso actual y no ha consumido su quantum, continuar
            if (procesoActual != null && !procesoActual.End() && contadorQuantum < quantum) {
                contadorQuantum++;
                semaforoCola.liberar();
                return procesoActual;
            }
            
            //Si el proceso actual no terminó, vuelve a la cola
            if (procesoActual != null && !procesoActual.End()) {
                procesoActual.setProcessState(Status.Ready);
                colaListos.encolar(procesoActual);
                logger.log(String.format("Quantum agotado - Proceso %s devuelto a cola", procesoActual.getName()));
            }
            
            //Obtener siguiente proceso
            contadorQuantum = 0;
            Proceso siguiente = colaListos.desencolar();
            if (siguiente != null) {
                siguiente.setProcessState(Status.Running);
                contadorQuantum = 1;
                logger.log(String.format("Planificador Round Robin selecciona Proceso %s (Quantum %d", siguiente.getName(), quantum));
            }
            procesoActual = siguiente;
            semaforoCola.liberar();
            return siguiente;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }
    
    public int getQuantum() {
        return quantum;
    }
    
    @Override
    public Cola getColaListos() {
        return colaListos;
    }
    
    @Override
    public String getNombre() {
        return "Round Robin (Quantum: " + quantum + ")";
    }
}
