package Simulacion;

import model.Proceso;
import Planificacion.*;
/**
 *
 * @author sarazo
 */
public class Simulador {
    private CPU cpu;
    private Planificador planificador;
    private ProcessManager gestorProcesos;
    
    public Simulador() {
        this.cpu = new CPU();
        this.planificador = new Planificador(cpu);
        this.gestorProcesos = new ProcessManager();
    }
    
    public void iniciarSimulacion() {
        // Se configuran 500 ms para la duración del ciclo como prueba
        Clock.getInstance().setCycleDuration(500);
        
        cpu.iniciar();
        planificador.iniciar();
        
        crearProcesosPrueba();
    }
    
    public void detenerSimulacion() {
        planificador.detener();
        cpu.detener();
    }
    
    private void crearProcesosPruebaConIO() {
        // CPU-bound - no genera excepciones I/O
        Proceso proceso1 = new Proceso(1, "Proceso-CPU", 5, true, 0, 0);

        // I/O-bound - genera excepción cada 2 ciclos, dura 3 ciclos 
        Proceso proceso2 = new Proceso(2, "Proceso-IO-Rapido", 6, false, 2, 3);

        // I/O-bound - genera excepción cada 3 ciclos, dura 4 ciclos
        Proceso proceso3 = new Proceso(3, "Proceso-IO-Lento", 8, false, 3, 4);

        // Verificar que los parámetros se asignaron correctamente
        System.out.println("=== VERIFICACIÓN DE PROCESOS ===");
        System.out.println("Proceso 1 - " + proceso1.getName() + 
            ": CPU-bound=" + proceso1.isCPUbound() + 
            ", CiclosExcepcion=" + proceso1.getCiclosParaExcepcion() +
            ", CiclosPendientes=" + proceso1.getCiclosPendientes());

        System.out.println("Proceso 2 - " + proceso2.getName() + 
            ": CPU-bound=" + proceso2.isCPUbound() + 
            ", CiclosExcepcion=" + proceso2.getCiclosParaExcepcion() +
            ", CiclosPendientes=" + proceso2.getCiclosPendientes());

        System.out.println("Proceso 3 - " + proceso3.getName() + 
            ": CPU-bound=" + proceso3.isCPUbound() + 
            ", CiclosExcepcion=" + proceso3.getCiclosParaExcepcion() +
            ", CiclosPendientes=" + proceso3.getCiclosPendientes());
        System.out.println("================================");

        planificador.agregarProceso(proceso1);
        planificador.agregarProceso(proceso2);
        planificador.agregarProceso(proceso3);
       }
    
    private void crearProcesosPrueba() {
        //CPU-bound
        Proceso proceso1 = new Proceso(1, "Proceso-CPU", 5, true, 0, 0);
        
        //I/O-bound
        Proceso proceso2 = new Proceso(2, "Proceso-IO", 6, false, 2, 1);
        
        //CPU-bound
        Proceso proceso3 = new Proceso(3, "Proceso-CPU2", 4, true, 0, 0);
        
        planificador.agregarProceso(proceso1);
        planificador.agregarProceso(proceso2);
        planificador.agregarProceso(proceso3);
    }
    
    public String getEstadisticasIO() {
        ExceptionHandler manejador = cpu.getManejadorExcepciones();
        return String.format("Procesos bloqueados por E/S: %d", 
                           manejador.getProcesosBloqueados());
    }
    
    public void cambiarAFCFS() {
        planificador.cambiarAlgoritmo(new FCFS());
    }
    
    public void cambiarASJF() {
        planificador.cambiarAlgoritmo(new SJF());
    }
    
    public void cambiarARoundRobin() {
        planificador.cambiarAlgoritmo(new RoundRobin());
    }
    
    public void cambiarAPrioridades() {
        planificador.cambiarAlgoritmo(new Prioridades());
    }
    
    public void cambiarAMultiplesColas() {
        planificador.cambiarAlgoritmo(new MultiplesColas());
    }
    
    public void cambiarAGarantizado() {
        planificador.cambiarAlgoritmo(new Garantizado());
    }
    
    public CPU getCPU() {
        return cpu;
    }
    
    public Planificador getPlanificador() {
        return planificador;
    }
    
    public ProcessManager getGestorProcesos() {
        return gestorProcesos;
    }
}
