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
            proceso.setProcessState(Status.Ready);
            
            int instruccionesRestantes = proceso.getTotal_Instructions() - proceso.getPC();
            
            Cola colaReady = processManager.getC_Ready();
            Cola nuevaCola = new Cola();
            boolean inserted = false;
            
            // CORRECCIÓN: Insertar ordenado por instrucciones restantes (menor a mayor)
            for (int i = 0; i < colaReady.size(); i++) {
                Proceso p = colaReady.get(i);
                int instP = p.getTotal_Instructions() - p.getPC();
                
                // Insertar antes del primer proceso que tenga MÁS instrucciones
                if (!inserted && instruccionesRestantes <= instP) {
                    nuevaCola.encolar(proceso);
                    inserted = true;
                }
                nuevaCola.encolar(p);
            }
            
            // Si no se insertó (todos tienen menos instrucciones), agregar al final
            if (!inserted) {
                nuevaCola.encolar(proceso);
            }
            
            // CORRECCIÓN: Reemplazar la cola correctamente
            processManager.setC_Ready(nuevaCola); // Necesitas este método en ProcessManager
            
            logger.log(String.format("Proceso %s agregado a cola SJF (Instrucciones restantes: %d)", 
                proceso.getName(), instruccionesRestantes));
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Error SJF al agregar proceso: " + e.getMessage());
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
            
            // Si hay un proceso actual válido, continuar con él
            if (procesoActual != null && 
                procesoActual.getProcessState() == Status.Running && 
                !procesoActual.End()) {
                semaforoCola.liberar();
                return procesoActual;
            }
            
            // Limpiar proceso actual si no es válido
            if (procesoActual != null && 
                (procesoActual.End() || 
                 procesoActual.getProcessState() == Status.Blocked ||
                 procesoActual.getProcessState() == Status.Blocked_Suspended)) {
                
                // CORRECCIÓN: Si está bloqueado, podría volver más tarde
                if (procesoActual.getProcessState() == Status.Blocked) {
                    // El proceso se manejará cuando se desbloquee
                }
                procesoActual = null;
            }
            
            // Obtener el siguiente proceso (SJF siempre toma el primero - el más corto)
            Proceso siguiente = null;
            if (processManager.getC_Ready() != null && !processManager.getC_Ready().isEmpty()) {
                siguiente = processManager.getC_Ready().desencolar();
                
                if (siguiente != null) {
                    siguiente.setProcessState(Status.Running);
                    procesoActual = siguiente;
                    
                    int instRestantes = siguiente.getTotal_Instructions() - siguiente.getPC();
                    logger.log(String.format("Planificador SJF selecciona Proceso %s (Inst. restantes: %d)", 
                        siguiente.getName(), instRestantes));
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
