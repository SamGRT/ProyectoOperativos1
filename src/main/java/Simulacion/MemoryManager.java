/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Simulacion;

import model.Proceso;
import Edd.Cola;
import Edd.ListaEnlazada;
/**
 *
 * @author Samantha
 */
public class MemoryManager {
    private final ProcessManager processManager;
    private final int memoriaTotal; // MB
    private int memoriaDisponible;
    private final int umbralSuspension;
    
    public MemoryManager(ProcessManager processManager, int memoriaTotal) {
        this.processManager = processManager;
        this.memoriaTotal = memoriaTotal;
        this.memoriaDisponible = memoriaTotal;
        this.umbralSuspension = 20; // Suspender cuando quede 20% de memoria
    }
    public boolean asignarMemoria(Proceso proceso) {
        int memoriaRequerida = calcularMemoriaProceso(proceso);
        
        if (memoriaRequerida <= memoriaDisponible) {
            memoriaDisponible -= memoriaRequerida;
            proceso.setMemoriaAsignada(memoriaRequerida);
            return true;
        }
        return false;
    }
    
    public void liberarMemoria(Proceso proceso) {
        if (proceso.getMemoriaAsignada() > 0) {
            memoriaDisponible += proceso.getMemoriaAsignada();
            proceso.setMemoriaAsignada(0);
        }
    }
    
    private int calcularMemoriaProceso(Proceso proceso) {
        // Memoria base + memoria por instrucciones
        return 10 + (proceso.getTotal_Instructions() / 10);
    }
    
    public void verificarYGestionarMemoria() {
        double porcentajeDisponible = (double) memoriaDisponible / memoriaTotal * 100;
        
        // Si la memoria está baja, suspender procesos
        if (porcentajeDisponible < umbralSuspension) {
            suspenderProcesos();
        } 
        // Si hay suficiente memoria, reactivar suspendidos
        else if (porcentajeDisponible > 50) {
            reactivarProcesos();
        }
    }
    
    private void suspenderProcesos() {
        System.out.println("[MEMORY] Memoria baja, suspendiendo procesos...");
        
        // Usar tu Cola personalizada para suspender procesos
        suspenderDeCola(processManager.getC_Blocked(), true);
        
        // Si aún se necesita memoria, suspender procesos listos
        double porcentajeNecesario = (double) memoriaDisponible / memoriaTotal * 100;
        if (porcentajeNecesario < umbralSuspension) {
            suspenderDeCola(processManager.getC_Ready(), false);
        }
    }
    
    private void suspenderDeCola(Cola cola, boolean esBloqueado) {
        // Crear una lista temporal de procesos a suspender
        ListaEnlazada procesosASuspender = new ListaEnlazada();
        
        // Primero identificar qué procesos suspender
        for (int i = 0; i < cola.size(); i++) {
            Proceso proceso = cola.get(i);
            if (proceso != null && deberiaSuspender(proceso)) {
                procesosASuspender.agregar(proceso);
            }
        }
        
        // Luego suspender los procesos seleccionados
        for (int i = 0; i < procesosASuspender.tamaño(); i++) {
            Proceso proceso = (Proceso) procesosASuspender.obtener(i);
            if (esBloqueado) {
                processManager.SuspendBlockedProcess(proceso);
            } else {
                processManager.SuspendReadyProcess(proceso);
            }
            liberarMemoria(proceso);
            System.out.println("[MEMORY] Proceso " + proceso.getName() + " suspendido");
            
            // Verificar si ya tenemos suficiente memoria
            if ((double) memoriaDisponible / memoriaTotal * 100 > umbralSuspension + 10) {
                break;
            }
        }
    }
    
    private boolean deberiaSuspender(Proceso proceso) {
        // Estrategia: suspender procesos I/O bound primero, luego los más grandes
        if (!proceso.isCPUbound()) return true; // I/O bound primero
        if (proceso.getTotal_Instructions() > 50) return true; // Procesos grandes
        return Math.random() < 0.4; // 40% probabilidad para otros
    }
    
    private void reactivarProcesos() {
        System.out.println("[MEMORY] Memoria disponible, reactivando procesos...");
        
        // Reactivar suspendidos-bloqueados primero
        reactivarDeCola(processManager.getC_Suspended_Blocked(), true);
        
        // Reactivar suspendidos-listos
        reactivarDeCola(processManager.getC_Suspended_Ready(), false);
    }
    
    private void reactivarDeCola(Cola cola, boolean esBloqueado) {
        ListaEnlazada procesosAReactivar = new ListaEnlazada();
        
        // Identificar procesos que pueden ser reactivados
        for (int i = 0; i < cola.size(); i++) {
            Proceso proceso = cola.get(i);
            if (proceso != null && asignarMemoria(proceso)) {
                procesosAReactivar.agregar(proceso);
            }
        }
        
        // Reactivar los procesos
        for (int i = 0; i < procesosAReactivar.tamaño(); i++) {
            Proceso proceso = (Proceso) procesosAReactivar.obtener(i);
            if (esBloqueado) {
                processManager.SuspendB_toBlocked(proceso);
            } else {
                processManager.SuspendR_toReady(proceso);
            }
            System.out.println("[MEMORY] Proceso " + proceso.getName() + " reactivado");
            
            // No reactivar todos de una vez
            if (i >= 2) break; // Máximo 2 procesos por ciclo
        }
    }
    
    // Getters para métricas
    public int getMemoriaTotal() { return memoriaTotal; }
    public int getMemoriaDisponible() { return memoriaDisponible; }
    public double getPorcentajeMemoriaDisponible() { 
        return (double) memoriaDisponible / memoriaTotal * 100; 
    }
    
    
    
    
    
}
