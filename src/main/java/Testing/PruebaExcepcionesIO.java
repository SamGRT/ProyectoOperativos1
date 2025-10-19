// Testing/PruebaExcepcionesIO.java
package Testing;

import Simulacion.Simulador;
import model.Proceso;

public class PruebaExcepcionesIO {
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE EXCEPCIONES I/O ===");
        
        Simulador simulador = new Simulador();
        
        try {
            System.out.println("Iniciando simulación con procesos I/O-bound...");
            simulador.iniciarSimulacion();
            
            // Ejecutar por más tiempo para ver excepciones
            for (int i = 0; i < 10; i++) {
                Thread.sleep(1000);
                System.out.println("\n--- Ciclo " + (i + 1) + " ---");
                System.out.println(simulador.getEstadisticasIO());
            }
            
            System.out.println("\nDeteniendo simulación...");
            simulador.detenerSimulacion();
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("=== PRUEBA DE EXCEPCIONES COMPLETADA ===");
    }
}