// Simulacion/Planificador.java (ACTUALIZADA)
package Simulacion;

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
    
    public Planificador(CPU cpu, ProcessManager processManager) {
        this.algoritmoActual = new FCFS(); // Por defecto FCFS
        this.cpu = cpu;
        this.ejecutando = false;
        this.logger = Logger.getInstancia();
        this.semaforoCambio = new Semaforo(1);
    }
    
    public void cambiarAlgoritmo(AlgoritmoPlanificacion nuevoAlgoritmo) {
        // Mover procesos del algoritmo anterior al nuevo
        if (algoritmoActual != null) {
            Edd.Cola colaAnterior = algoritmoActual.getColaListos();
            for (int i = 0; i < colaAnterior.size(); i++) {
                Proceso p = colaAnterior.get(i);
                if (p != null && !p.End()) {
                    nuevoAlgoritmo.agregarProceso(p);
                }
            }
        }
        
        this.algoritmoActual = nuevoAlgoritmo;
        logger.log("Algoritmo cambiado a: " + nuevoAlgoritmo.getNombre());
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
            // ESPERAR antes de verificar de nuevo
            Thread.sleep(Clock.getInstance().getCycleDuration() / 2);
            
         //SOLO seleccionar nuevo proceso si la CPU está libre
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
