/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Simulacion;

/**
 *
 * @author Samantha
 */
public class Clock {
    private static Clock instance; // Patrón Singleton - única instancia
    private int currentCycle;
    private int CycleDuration; //milisegundos
    private boolean running;

    public Clock() {
        this.currentCycle = 0;
        this.CycleDuration = 500;  // 1 sec por defecto
        this.running = false;
    }
    
    // Patrón Singleton - asegura una sola instancia del reloj
    public static synchronized Clock getInstance() {
        if (instance == null) {
            instance = new Clock();
        }
        return instance;
    }
    
    public void tick() {
        currentCycle++;
        try {                                                  //Avanza ciclo + pausa
            Thread.sleep(CycleDuration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public int getCurrentCycle() {
        return currentCycle;
    }
    
    public void incrementCycle() {
        currentCycle++;
    }
    
    public void reset() {
        this.currentCycle = 0;
    }

    public long getCycleDuration() {
        return CycleDuration;
    }

    public void setCycleDuration(int CycleDuration) {
        this.CycleDuration = CycleDuration;
    }

    public void start() {
        this.running = true;
    }
    
    public void  stop() {
        this.running = false;
    }
    
    public boolean isRunning() {
        return running;
    }
}
