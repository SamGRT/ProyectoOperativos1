// Testing/PruebaCorreccionesIO.java
package Testing;

import Simulacion.Simulador;

public class PruebaCorreccionesIO {
    public static void main(String[] args) {
        System.out.println("=== PRUEBA CORRECCIONES I/O ===");
        
        Simulador simulador = new Simulador();
        
        try {
            System.out.println("Iniciando simulación con E/S real...");
            simulador.iniciarSimulacion();
            
            // Monitorear por 10 segundos
            for (int i = 0; i < 10; i++) {
                Thread.sleep(1000);
                int bloqueados = simulador.getCPU().getManejadorExcepciones().getProcesosBloqueados();
                System.out.printf("[%d seg] Ciclo: %d | Procesos en E/S: %d%n", 
                    i + 1, 
                    simulador.getCPU().getCiclosReloj(),
                    bloqueados);
            }
            
            System.out.println("\nDeteniendo simulación...");
            simulador.detenerSimulacion();
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("=== PRUEBA COMPLETADA ===");
    }
}
