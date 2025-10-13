package utils;

/**
 *
 * @author sarazo
 */
public class Semaforo {
    private int permisos;
    
    public Semaforo(int permisosIniciales) {
        if (permisosIniciales < 0) {
            throw new IllegalArgumentException("Los permisos iniciales no pueden ser negativos");
        }
        this.permisos = permisosIniciales;
    }
    
    public synchronized void adquirir() throws InterruptedException {
        while (permisos <= 0) {
            wait();
        }
        permisos--;
    }
    
    public synchronized void liberar() {
        permisos++;
        notifyAll();
    }
    
    public synchronized int permisosDisponibles() {
        return permisos;
    }
}