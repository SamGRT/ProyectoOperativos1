// Simulacion/Planificador.java (ACTUALIZADA)
package Simulacion;

import Edd.Cola;
import Planificacion.*;
import model.Proceso;
import utils.Logger;
import utils.Semaforo;

/**
 *
 * @author sarazo
 */
public class Planificador implements Runnable {
    private AlgoritmoPlanificacion algoritmoActual;
    private CPU cpu;
    private boolean ejecutando;
    private Thread hiloPlanificador;
    private Logger logger;
    private Semaforo semaforoCambio;
    private ProcessManager processManager; 
    
    public Planificador(CPU cpu, ProcessManager processManager) {
        this.algoritmoActual = new FCFS(processManager); // Por defecto FCFS
        this.cpu = cpu;
        this.ejecutando = false;
        this.logger = Logger.getInstancia();
        this.semaforoCambio = new Semaforo(1);
        this.processManager = processManager;
        
        
    }
    
       public void cambiarAlgoritmo(AlgoritmoPlanificacion nuevoAlgoritmo) {
        try {
            semaforoCambio.adquirir();
            
            System.out.println("[DEBUG Planificador] Cambiando algoritmo a: " + nuevoAlgoritmo.getNombre());
            //establecer el planificador en ProcessManager
            if (processManager != null) {
            processManager.setPlanificador(nuevoAlgoritmo);
            System.out.println("[DEBUG Planificador] Planificador establecido en ProcessManager");
        }
            // Transferir procesos de la cola Ready del ProcessManager al nuevo algoritmo
            if (processManager != null && processManager.getC_Ready() != null) {
                Cola colaReady = processManager.getC_Ready();
                System.out.println("[DEBUG Planificador] Procesos en cola Ready antes del cambio: " + colaReady.size());
                
                // Transferir procesos al nuevo algoritmo
                for (int i = 0; i < colaReady.size(); i++) {
                    Proceso p = colaReady.get(i);
                    if (p != null && !p.End()) {
                        nuevoAlgoritmo.agregarProceso(p);
                        System.out.println("[DEBUG Planificador] Proceso transferido: " + p.getName());
                    }
                }
                
            }
            
            this.algoritmoActual = nuevoAlgoritmo;
            logger.log("Algoritmo cambiado a: " + nuevoAlgoritmo.getNombre());
            
            semaforoCambio.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void iniciar() {
        if (!ejecutando) {
            ejecutando = true;
            hiloPlanificador = new Thread(this, "Hilo-Planificador");
            hiloPlanificador.start();
            logger.log("Planificador iniciado con: " + algoritmoActual.getNombre());
        }
    }
    
    public void detener() {
        ejecutando = false;
        if (hiloPlanificador != null) {
            hiloPlanificador.interrupt();
        }
        logger.log("Planificador detenido");
    }
    
@Override
public void run() {
    while (ejecutando && !Thread.currentThread().isInterrupted()) {
        try {
            synchronized (this) {
                // ? Esperar notificación o timeout (para ciclos normales)
                this.wait(Clock.getInstance().getCycleDuration() / 2);
            }

            // Solo planificar si la CPU está libre
            if (cpu.getProcesoActual() == null) {
                Proceso siguienteProceso = algoritmoActual.obtenerSiguienteProceso();
                if (siguienteProceso != null) {
                    logger.log(String.format("[Ciclo %d] Planificador %s selecciona Proceso %s",
                        Clock.getInstance().getCurrentCycle(),
                        algoritmoActual.getNombre(),
                        siguienteProceso.getName()));

                    cpu.ejecutarProceso(siguienteProceso);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
        }
    }
}



    public void agregarProceso(Proceso proceso) {
        algoritmoActual.agregarProceso(proceso);
    }
    
    public AlgoritmoPlanificacion getAlgoritmoActual() {
        return algoritmoActual;
    }
    
    public boolean isEjecutando() {
        return ejecutando;
    }
}
