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
public class SJF implements AlgoritmoPlanificacion {
    private Cola colaListos;
    private Proceso procesoActual;
    private Semaforo semaforoCola;
    private Logger logger;
    
    public SJF() {
        this.colaListos = new Cola();
        this.semaforoCola = new Semaforo(1);
        this.logger = Logger.getInstancia();
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            proceso.setProcessState(Status.Ready);
            
            //Insertar ordenando por instrucciones restantes (SJF)
            int instruccionesRestantes = proceso.getTotal_Instructions() - proceso.getPC();
            int index = 0;
            
            while (index < colaListos.size()) {
                Proceso p = colaListos.get(index);
                int instP = p.getTotal_Instructions() - p.getPC();
                if (instruccionesRestantes > instP) {
                    break;
                }
                index++;
            }
            
            insertarEnPosicion(proceso, index);
            logger.log(String.format("Proceso %s agregado a cola SJF (Instrucciones: %d)",proceso.getName(), instruccionesRestantes));
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Error SJF al agregar proceso: " + e.getMessage());
        }
    }
    private void insertarEnPosicion(Proceso proceso, int index) {
        if (colaListos.size() == 0) {
            colaListos.encolar(proceso);
            return;
        }
        
        // Crear nueva cola con el proceso insertado en la posición correcta
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
                logger.log(String.format("Planificador SJF selecciona Proceso %s", siguiente.getName()));
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
        return "SJF (Shortest Job First)";
    }
}
