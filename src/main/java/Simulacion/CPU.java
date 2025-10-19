package Simulacion;

import model.Proceso;
import model.Status;
import utils.Logger;
import utils.Semaforo;

/**
 *
 * @author sarazo
 */
public class CPU implements Runnable {
    private Proceso procesoActual;
    private boolean ejecutando;
    private Thread hiloCPU;
    private Semaforo semaforoCPU;
    private Logger logger;
    private int ciclosReloj;
    private ExceptionHandler manejadorExcepciones;
    
    public CPU() {
        this.ejecutando = false;
        this.semaforoCPU = new Semaforo(1);
        this.logger = Logger.getInstancia();
        this.ciclosReloj = 0;
        this.manejadorExcepciones = new ExceptionHandler();
    }
    
    public void iniciar() {
        if (!ejecutando) {
            ejecutando = true;
            hiloCPU = new Thread(this, "Hilo-CPU");
            hiloCPU.start();
            manejadorExcepciones.iniciar();
            logger.log("CPU y manejador de excepciones iniciados");
        }
    }
    
    public void detener() {
        ejecutando = false;
        manejadorExcepciones.detener();
        if (hiloCPU != null) {
            hiloCPU.interrupt();
        }
        logger.log("CPU y manejador de excepciones detenidos");
    }
    
    public void ejecutarProceso(Proceso proceso) {
        try {
            semaforoCPU.adquirir();
            this.procesoActual = proceso;
            if (proceso != null) {
                logger.log(String.format("CPU ejecutando proceso: %s", proceso.getName()));
            }
            semaforoCPU.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Error al ejecutar el proceso: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        Clock.getInstance().start(); // Iniciar el reloj
        while (ejecutando && !Thread.currentThread().isInterrupted()) {
            try {
                semaforoCPU.adquirir();

                // INCREMENTAR CICLO GLOBAL
                Clock.getInstance().incrementCycle();

                if (procesoActual != null) {
                    boolean instruccionEjecutada = procesoActual.executeInstruction();
                    ciclosReloj++;

                    if (instruccionEjecutada) {
                        logger.log(String.format("Proceso %s - PC: %d/%d - MAR: %d",
                                procesoActual.getName(),
                                procesoActual.getPC(),
                                procesoActual.getTotal_Instructions(),
                                procesoActual.getMar()));

                        // VERIFICAR EXCEPCIÓN I/O
                        if (procesoActual.generate_EXC()) {
                            logger.log(String.format("¡EXCEPCIÓN I/O! Proceso %s solicita E/S", 
                                procesoActual.getName()));
                            manejadorExcepciones.manejarExcepcionIO(procesoActual);
                            procesoActual = null; // Liberar CPU
                        }
                    }

                    if (procesoActual != null && procesoActual.End()) {
                        procesoActual.setProcessState(Status.Finished);
                        logger.log(String.format("Proceso %s TERMINADO", procesoActual.getName()));
                        procesoActual = null;
                    }
                }

                semaforoCPU.liberar();
                Thread.sleep(Clock.getInstance().getCycleDuration());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        Clock.getInstance().stop(); // Detener el reloj
    }
    
    public ExceptionHandler getManejadorExcepciones() {
        return manejadorExcepciones;
    }
    
    public Proceso getProcesoActual() {
        return procesoActual;
    }
    
    public int getCiclosReloj() {
        return ciclosReloj;
    }
    
    public boolean isEjecutando() {
        return ejecutando;
    }
}
