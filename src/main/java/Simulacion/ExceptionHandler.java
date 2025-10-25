package Simulacion;

import model.Proceso;
import utils.Logger;
import utils.Semaforo;
import Edd.Cola;
import model.Status;
/**
 *
 * @author sarazo
 */
public class ExceptionHandler implements Runnable {
    
    private Semaforo semaforoCola;
    private Logger logger;
    private boolean ejecutando;
    private Thread hiloExcepciones;
    private ProcessManager processManager;
            
    public ExceptionHandler(ProcessManager processManager) {
        
        this.semaforoCola  = new Semaforo(1);
        this.logger = Logger.getInstancia();
        this.ejecutando = false;
        this.processManager = processManager;
    }
    
    public void iniciar() {
        if (!ejecutando) {
            ejecutando = true;
            hiloExcepciones = new Thread(this, "HiloExcepciones");
            hiloExcepciones.start();
            logger.log("Manejador de excepciones iniciado");
        }
    }
    
    public void detener() {
        ejecutando = false;
        if(hiloExcepciones != null) {
            hiloExcepciones.interrupt();
        }
        logger.log("Manejador de excepciones detenido");
    }
    
    public void manejarExcepcionIO (Proceso proceso) {
        try {
            semaforoCola.adquirir();
            
            if (proceso != null && !proceso.End()) {
                if(proceso.getCiclosPendientes() <= 0) {
                    logger.log(String.format("ADVERTENCIA: Proceso %s tiene ciclos pendientes = %d", proceso.getName(), proceso.getCiclosPendientes()));
                }
                proceso.setProcessState(Status.Blocked);
                
                logger.log(String.format("EXCEPCION I/O: Proceso %s bloqueado por E/S (Duración: %d ciclos)", proceso.getName(), proceso.getCiclosPendientes()));
            }
            
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Error al manejar excepcion: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        while (ejecutando && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(Clock.getInstance().getCycleDuration());
                procesarExcepcionesIO();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void procesarExcepcionesIO() {
        try {
            semaforoCola.adquirir();
            
            // Obtener la cola actual de bloqueados del ProcessManager
            Cola colaBloqueados = processManager.getC_Blocked();
            
            if (colaBloqueados != null && !colaBloqueados.isEmpty()) {
                logger.log(String.format("[Ciclo %d] Procesando E/S: %d procesos en cola de bloqueados", 
                    Clock.getInstance().getCurrentCycle(), colaBloqueados.size()));
                
                int procesosProcesados = 0;
                int procesosDesbloqueados = 0;
                int procesosAunBloqueados = 0;
                
                // Estrategia: Procesar cada elemento y volver a encolar solo los que siguen bloqueados
                // Usamos un contador para saber cuántos procesos procesar en esta iteración
                int procesosAProcesar = colaBloqueados.size();
                
                for (int i = 0; i < procesosAProcesar; i++) {
                    Proceso proceso = colaBloqueados.desencolar();
                    if (proceso != null) {
                        procesosProcesados++;
                        boolean eSCompletada = procesarCicloES(proceso);
                        
                        if (!eSCompletada) {
                            // E/S aún en progreso, volver a encolar
                            colaBloqueados.encolar(proceso);
                            procesosAunBloqueados++;
                            logger.log(String.format("[Ciclo %d] E/S en progreso: %s - Ciclos restantes: %d", 
                                Clock.getInstance().getCurrentCycle(), proceso.getName(), proceso.getCiclosPendientes()));
                        } else {
                            // E/S completada - ya fue movido a Ready por processManager.unblockProcess()
                            procesosDesbloqueados++;
                            logger.log(String.format("[Ciclo %d] E/S COMPLETADA: Proceso %s listo para continuar", 
                                Clock.getInstance().getCurrentCycle(), proceso.getName()));
                        }
                    }
                }
                
                logger.log(String.format("[Ciclo %d] E/S procesadas: %d procesos, %d desbloqueados, %d aún bloqueados", 
                    Clock.getInstance().getCurrentCycle(), procesosProcesados, procesosDesbloqueados, procesosAunBloqueados));
            }
            
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private boolean procesarCicloES(Proceso proceso) {
        if (proceso.getCiclosPendientes() > 0) {
            // Decrementar contador de E/S
            int nuevosCiclos = proceso.getCiclosPendientes() - 1;
            proceso.setCiclosPendientes(nuevosCiclos);
            
            // Verificar si la E/S ha terminado
            if (nuevosCiclos <= 0) {
                // IMPORTANTE: Llamar a unblockProcess para mover el proceso a Ready
                processManager.unblockProcess(proceso);
                proceso.resetAfterIO(); 
                return true; // E/S completada
            }
            return false; // E/S aún en progreso
        }
        return true; // No hay E/S pendientes
    }

    
    private void notificarProcesoListo(Proceso proceso) {
        logger.log(String.format("PROCESO LISTO: %s disponible para planificación", proceso.getName()));
    }
    
 
    
    public int getProcesosBloqueados() {
        return processManager.getC_Blocked().size();
    }
    
    public boolean isEjecutando() {
        return ejecutando;
    }
}
