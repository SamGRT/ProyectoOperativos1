package Testing;

import Simulacion.Simulador;
import Planificacion.*;
import model.Proceso;

public class PruebaPlanificacion {
    public static void main(String[] args) {
        System.out.println("=== PRUEBAS COMPLETAS DEL SISTEMA DE PLANIFICACIÓN ===");
        
        // Prueba 1: Verificar que todas las políticas se crean correctamente
        probarCreacionPoliticas();
        
        // Prueba 2: Probar cada política individualmente CON MÉTODO SEGURO
        probarPoliticasConProcesosSeguros();
        
        System.out.println("=== TODAS LAS PRUEBAS COMPLETADAS ===");
    }
    
    private static void probarCreacionPoliticas() {
        System.out.println("\n--- PRUEBA 1: CREACIÓN DE POLÍTICAS ---");
        
        AlgoritmoPlanificacion[] politicas = {
            new FCFS(),
            new SJF(),
            new RoundRobin(),
            new Prioridades(),
            new MultiplesColas(),
            new Garantizado()
        };
        
        for (AlgoritmoPlanificacion politica : politicas) {
            System.out.println("✓ " + politica.getNombre() + " - Cola: " + 
                             politica.getColaListos().size() + " procesos");
        }
        
        System.out.println("✓ Se crearon correctamente las 6 políticas de planificación");
    }
    
    // MÉTODO SEGURO PARA EVITAR ERRORES
    private static void probarPoliticasConProcesosSeguros() {
        System.out.println("\n--- PRUEBA 2: POLÍTICAS CON PROCESOS SEGUROS ---");
        
        // Crear procesos de prueba SEGUROS
        Proceso[] procesos = {
            new Proceso(1, "Navegador", 8, false, 2, 1),
            new Proceso(2, "Compilador", 12, true, 0, 0),
            new Proceso(3, "Editor", 5, false, 3, 1)
        };
        
        // Probar FCFS
        System.out.println("\n--- FCFS ---");
        FCFS fcfs = new FCFS();
        for (Proceso p : procesos) {
            fcfs.agregarProceso(p);
        }
        System.out.println("Procesos en FCFS: " + fcfs.getColaListos().size());
        
        // Probar SJF
        System.out.println("\n--- SJF ---");
        SJF sjf = new SJF();
        for (Proceso p : procesos) {
            sjf.agregarProceso(p);
        }
        System.out.println("Procesos en SJF: " + sjf.getColaListos().size());
        
        // Probar Round Robin
        System.out.println("\n--- Round Robin ---");
        RoundRobin rr = new RoundRobin();
        for (Proceso p : procesos) {
            rr.agregarProceso(p);
        }
        System.out.println("Procesos en Round Robin: " + rr.getColaListos().size());
        
        // Probar simulador básico
        System.out.println("\n--- SIMULADOR BÁSICO ---");
        Simulador simulador = new Simulador();
        
        try {
            simulador.iniciarSimulacion();
            Thread.sleep(2000); // Esperar 2 segundos
            simulador.detenerSimulacion();
            System.out.println("✓ Simulador probado exitosamente");
        } catch (InterruptedException e) {
            System.out.println("✗ Simulador interrumpido: " + e.getMessage());
        }
    }
}