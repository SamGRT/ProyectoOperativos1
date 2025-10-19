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
public class MultiplesColas implements AlgoritmoPlanificacion {
    private Cola[] colas;
    private Proceso procesoActual;
    private Semaforo semaforoCola;
    private Logger logger;
    private int quantumBase;
    private int contadorQuantum;
    
    public MultiplesColas() {
        this.colas = new Cola[3]; // 3 colas con diferentes prioridades
        for (int i = 0; i < colas.length; i++) {
            colas[i] = new Cola();
        }
        this.semaforoCola = new Semaforo(1);
        this.logger = Logger.getInstancia();
        this.quantumBase = 2;
        this.contadorQuantum = 0;
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            proceso.setProcessState(Status.Ready);
            
            // Asignar a cola basado en si es CPU-bound o I/O-bound
            int colaIndex = proceso.isCPUbound() ? 0 : 1; // CPU-bound alta prioridad
            colas[colaIndex].encolar(proceso);
            
            logger.log(String.format("Proceso %s agregado a cola %d (Multiples Colas)", 
                proceso.getName(), colaIndex));
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Error Multiples Colas al agregar proceso: " + e.getMessage());
        }
    }
    
    @Override
    public Proceso obtenerSiguienteProceso() {
        try {
            semaforoCola.adquirir();
            
            // Si hay proceso actual y no ha consumido su quantum, continuar
            if (procesoActual != null && !procesoActual.End() && contadorQuantum < getQuantumActual()) {
                contadorQuantum++;
                semaforoCola.liberar();
                return procesoActual;
            }
            
            // Buscar en colas de mayor a menor prioridad
            Proceso siguiente = null;
            int colaOrigen = -1;
            
            for (int i = 0; i < colas.length; i++) {
                if (!colas[i].isEmpty()) {
                    siguiente = colas[i].desencolar();
                    colaOrigen = i;
                    break;
                }
            }
            
            if (siguiente != null) {
                siguiente.setProcessState(Status.Running);
                contadorQuantum = 1;
                logger.log(String.format("Multiples Colas selecciona Proceso %s de cola %d", 
                    siguiente.getName(), colaOrigen));
                
                // Si el proceso actual no terminó, moverlo a cola de menor prioridad
                if (procesoActual != null && !procesoActual.End()) {
                    int nuevaCola = Math.min(colaOrigen + 1, colas.length - 1);
                    procesoActual.setProcessState(Status.Ready);
                    colas[nuevaCola].encolar(procesoActual);
                    logger.log(String.format("Proceso %s movido a cola %d", 
                        procesoActual.getName(), nuevaCola));
                }
            }
            
            procesoActual = siguiente;
            semaforoCola.liberar();
            return siguiente;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    private int getQuantumActual() {
        return quantumBase * (procesoActual != null && procesoActual.isCPUbound() ? 2 : 1);
    }
    
    @Override
    public Cola getColaListos() {
        Cola combinada = new Cola();
        for (Cola cola : colas) {
            for (int i = 0; i < cola.size(); i++) {
                combinada.encolar(cola.get(i));
            }
        }
        return combinada;
    }
    
    @Override
    public String getNombre() {
        return "Múltiples Colas con Retroalimentación";
    }
}
