// Testing/PruebaDepuracionIO.java
package Testing;

import Simulacion.Simulador;

public class PruebaDepuracionIO {
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DEPURACIÓN I/O ===");
        
        Simulador simulador = new Simulador();
        
        try {
            System.out.println("Iniciando simulación con verificación...");
            simulador.iniciarSimulacion();
            
            // Monitorear más detalladamente
            for (int i = 0; i < 15; i++) {
                Thread.sleep(1000);
                int bloqueados = simulador.getCPU().getManejadorExcepciones().getProcesosBloqueados();
                int ciclosCPU = simulador.getCPU().getCiclosReloj();
                int ciclosGlobal = (int) Simulacion.Clock.getInstance().getCurrentCycle();
                
                System.out.printf("[%d seg] CicloCPU: %d | CicloGlobal: %d | Procesos E/S: %d%n", 
                    i + 1, ciclosCPU, ciclosGlobal, bloqueados);
                
                // Mostrar estado cada 3 segundos
                if ((i + 1) % 3 == 0) {
                    String procesoActual = simulador.getCPU().getProcesoActual() != null ? 
                        simulador.getCPU().getProcesoActual().getName() : "Ninguno";
                    System.out.printf("  >> Estado: Proceso=%s, E/S=%d%n", procesoActual, bloqueados);
                }
            }
            
            System.out.println("\nDeteniendo simulación...");
            simulador.detenerSimulacion();
            
            // Estadísticas finales
            System.out.println("\n=== ESTADÍSTICAS FINALES ===");
            System.out.println("Ciclos de CPU: " + simulador.getCPU().getCiclosReloj());
            System.out.println("Ciclos globales: " + Simulacion.Clock.getInstance().getCurrentCycle());
            System.out.println("Procesos en E/S final: " + 
                simulador.getCPU().getManejadorExcepciones().getProcesosBloqueados());
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("=== PRUEBA COMPLETADA ===");
    }
}
