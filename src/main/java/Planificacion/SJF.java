package Planificacion;

import model.Proceso;
import model.Status;
import Edd.Cola;
import Simulacion.ProcessManager;
import utils.Logger;
import utils.Semaforo;

/**
 *
 * @author sarazo
 */
public class SJF implements AlgoritmoPlanificacion {
    private ProcessManager processManager;
    private Proceso procesoActual;
    private Semaforo semaforoCola;
    private Logger logger;
    
    public SJF(ProcessManager processManager) {
        this.processManager = processManager;
        this.semaforoCola = new Semaforo(1);
        this.logger = Logger.getInstancia();
    }
    
   @Override
public void agregarProceso(Proceso proceso) {
    try {
        semaforoCola.adquirir();
        
        // SOLUCIÓN: Solo cambiar el estado, NO encolar nuevamente
        // El proceso YA está en C_Ready desde ProcessManager.addProcess()
        proceso.setProcessState(Status.Ready);
        
        logger.log(String.format("Proceso %s preparado para SJF", proceso.getName()));
        semaforoCola.liberar();
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
    private void insertarEnPosicion(Proceso proceso, int index) {
        if (processManager.getC_Ready().size() == 0) {
            processManager.getC_Ready().encolar(proceso);
            return;
        }
        
        // Crear nueva cola con el proceso insertado en la posición correcta
        Cola nuevaCola = new Cola();
        for (int i = 0; i < index; i++) {
            nuevaCola.encolar(processManager.getC_Ready().get(i));
        }
        nuevaCola.encolar(proceso);
        for (int i = index; i < processManager.getC_Ready().size(); i++) {
            nuevaCola.encolar(processManager.getC_Ready().get(i));
        }
        Cola colaListos = processManager.getC_Ready();
        colaListos = nuevaCola;
    }
 @Override
public Proceso obtenerSiguienteProceso() {
    try {
        semaforoCola.adquirir();
        
        // Si hay proceso actual válido, continuar con él
        if (procesoActual != null && 
            procesoActual.getProcessState() == Status.Running && 
            !procesoActual.End()) {
            semaforoCola.liberar();
            return procesoActual;
        }

        // Buscar proceso con menos instrucciones restantes
        Proceso siguiente = null;
        if (processManager != null && processManager.getC_Ready() != null) {
            Cola cola = processManager.getC_Ready();
            int minInstrucciones = Integer.MAX_VALUE;
            
            // SOLUCIÓN: Buscar sin modificar la cola
            for (int i = 0; i < cola.size(); i++) {
                Proceso p = cola.get(i);
                int instRestantes = p.getTotal_Instructions() - p.getPC();
                if (instRestantes < minInstrucciones && p.getProcessState() == Status.Ready) {
                    minInstrucciones = instRestantes;
                    siguiente = p;
                }
            }
            
            if (siguiente != null) {
                // SOLUCIÓN: Solo cambiar estado, NO modificar la cola
                siguiente.setProcessState(Status.Running);
                procesoActual = siguiente;
                logger.log(String.format("Planificador SJF selecciona Proceso %s (Inst. restantes: %d)", 
                    siguiente.getName(), minInstrucciones));
            }
        }
        
        semaforoCola.liberar();
        return siguiente;
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return null;
    }
}
    
    public void liberarProcesoActual() {
        try {
            semaforoCola.adquirir();
            if (procesoActual != null) {
                System.out.println("[DEBUG SJF] Liberando proceso actual: " + procesoActual.getName());
                procesoActual = null;
            }
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public Cola getColaListos() {
        return processManager != null ? processManager.getC_Ready() : null;
    }
    
    @Override
    public String getNombre() {
        return "SJF (Shortest Job First)";
    }
    
}
