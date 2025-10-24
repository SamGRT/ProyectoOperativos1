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
    private Cola colaBloqueados;
    private Semaforo semaforoCola;
    private Logger logger;
    private boolean ejecutando;
    private Thread hiloExcepciones;
    private ProcessManager processManager;
            
    public ExceptionHandler(ProcessManager processManager) {
        this.colaBloqueados = processManager.getC_Blocked();
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
                colaBloqueados.encolar(proceso);
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
            
            if (!colaBloqueados.isEmpty()) {
                logger.log(String.format("Procesando E/S: %d procesos en cola de bloqueados", 
                    colaBloqueados.size()));
                
                Cola nuevaCola = new Cola();
                int procesosProcesados = 0;
                int procesosDesbloqueados = 0;
                
                while (!colaBloqueados.isEmpty()) {
                    Proceso proceso = colaBloqueados.desencolar();
                    if (proceso != null) {
                        procesosProcesados++;
                        boolean eSCompletada = procesarCicloES(proceso);
                        
                        if (!eSCompletada) {
                            // E/S aún en progreso, volver a encolar
                            nuevaCola.encolar(proceso);
                            logger.log(String.format("E/S en progreso: %s - Ciclos restantes: %d", 
                                proceso.getName(), proceso.getCiclosPendientes()));
                        } else {
                            // E/S completada - ya fue a ready por unblock process
                           procesosDesbloqueados++;
                            logger.log(String.format("E/S COMPLETADA: Proceso %s listo para continuar", 
                            proceso.getName()));
                        }
                    }
                }
                
            
                
                
                logger.log(String.format("E/S procesadas: %d procesos, %d aún bloqueados", 
                    procesosProcesados, procesosDesbloqueados, colaBloqueados.size()));
            }
            
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
 private boolean procesarCicloES(Proceso proceso) {
        if (proceso.getCiclosPendientes() > 0) {
            proceso.setCiclosPendientes(proceso.getCiclosPendientes() - 1);
            
            if (proceso.getCiclosPendientes() <= 0) {
              //  E/S COMPLETADA - mover de vuelta a Ready
                if (processManager != null) {
                    processManager.unblockProcess(proceso);
                }
                return true;
            }
            return false;
        }
        return true;
    }

    
    private void notificarProcesoListo(Proceso proceso) {
        logger.log(String.format("PROCESO LISTO: %s disponible para planificación", proceso.getName()));
    }
    
    public Cola getColaBloqueados() {
        return colaBloqueados;
    }
    
    public int getProcesosBloqueados() {
        return colaBloqueados.size();
    }
    
    public boolean isEjecutando() {
        return ejecutando;
    }
}
