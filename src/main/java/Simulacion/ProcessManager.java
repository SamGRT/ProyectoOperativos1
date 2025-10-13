/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Simulacion;

import model.Proceso;
import model.Status;
import Edd.Cola;
import Planificacion.FCFS;
/**
 *
 * @author Samantha
 */
public class ProcessManager {
    
    private Cola C_Ready;
    private Cola C_Blocked;
    private Cola C_Suspended;
    private Cola C_finished;
    
    private Proceso CurrentRunning_Process;
    private FCFS planificador;

    public ProcessManager() {
        this.C_Ready = new Cola();
        this.C_Blocked = new Cola();
        this.C_Suspended = new Cola();
        this.C_finished =new Cola();
        this.CurrentRunning_Process =null;
        this.planificador = null;
    }
    
    //Constructor con planificador integrado
    public ProcessManager(FCFS planificador) {
        this.C_Ready = new Cola();
        this.C_Blocked = new Cola();
        this.C_Suspended = new Cola();
        this.C_finished = new Cola();
        this.CurrentRunning_Process = null;
        this.planificador = planificador;
    }
    
    // Agregar nuevo proceso al sistema a LISTO 
    public void addProcess(Proceso proceso) {
        proceso.setProcessState(Status.Ready);
        C_Ready.encolar(proceso);
        
        if (planificador != null) {
            planificador.agregarProceso(proceso);
        }
    }
    
    //Método específico para enviar al planificador
    public void enviarAlPlanificador(Proceso proceso) {
        if (planificador != null) {
            proceso.setProcessState(Status.Ready);
            planificador.agregarProceso(proceso);
            if (!C_Ready.contiene(proceso)) {
                C_Ready.encolar(proceso);
            }
        }
    }
    
   // Ready --> Running o Running --> Ready
    public void setRunningProcess (Proceso proceso){
        if (CurrentRunning_Process  != null) {
            CurrentRunning_Process.setProcessState(Status.Ready);
            C_Ready.encolar(CurrentRunning_Process);
        }
        
        if (proceso != null) {
            proceso.setProcessState(Status.Running);
            C_Ready.remove(proceso);
        }
        CurrentRunning_Process = proceso;
    }
    
    // Running --> BLocked
    public void  BlockCurrentProcess(){
        if (CurrentRunning_Process != null) {
            CurrentRunning_Process.setProcessState(Status.Blocked);
            C_Blocked.encolar(CurrentRunning_Process);
            CurrentRunning_Process = null;
        }
    }
    
    //Blocked --> Ready (cuando se completaI/O)
    public void unblockProcess(Proceso proceso) {
        if (C_Blocked.contiene(proceso)) {
            proceso.setProcessState(Status.Ready);
            C_Blocked.remove(proceso);
            C_Ready.encolar(proceso);
            
            //Se notifica al planificador
            if (planificador != null) {
                planificador.agregarProceso(proceso);
            }
        }
    }
    
    // suspender (Ready/Blocked --> Suspended)
    public void SuspendProcess (Proceso proceso){
        proceso.setProcessState(Status.Suspended);
        C_Ready.remove(proceso);
        C_Blocked.remove(proceso);
        C_Suspended.encolar(proceso);
        }
    
    //Reanudar Proceso suspendido (Suspended --> Ready)
    public void Reanudar (Proceso proceso){
        if (C_Suspended.contiene(proceso)) {
            proceso.setProcessState(Status.Ready);
            C_Suspended.remove(proceso);
            C_Ready.encolar(proceso);
            
            if(planificador != null) {
                planificador.agregarProceso(proceso);
            }
        }
    }
    
    //Suspended --> Blocked_Suspended
    public void suspenderProcesoBloqueado(Proceso proceso) {
        if (C_Blocked.contiene(proceso)) {
            proceso.setProcessState(Status.Blocked_Suspended);
            C_Blocked.remove(proceso);
            C_Suspended.encolar(proceso);
        }
    }
    
    //Blocked_Suspended --> Ready_Suspended
    public void reanudarProcesoSuspendido(Proceso proceso) {
        if (C_Suspended.contiene(proceso) && proceso.getProcessState() == Status.Blocked_Suspended) {
            proceso.setProcessState(Status.Ready_Suspended);
        }
    }
    
    //END process (Running --> Finished)
    public void EndProcess(){
        if (CurrentRunning_Process != null) {
            CurrentRunning_Process.setProcessState(Status.Finished);
            C_finished.encolar(CurrentRunning_Process);
            CurrentRunning_Process = null;
        }
    }
    
    //Termina el proceso desde cualquier estado
    public void terminarProceso(Proceso proceso) {
        proceso.setProcessState(Status.Finished);
        
        C_Ready.remove(proceso);
        C_Blocked.remove(proceso);
        C_Suspended.remove(proceso);
        
        if(!C_finished.contiene(proceso)) {
            C_finished.encolar(proceso);
        }
        
        if (CurrentRunning_Process == proceso) {
            CurrentRunning_Process = null;
        }
    }

    public Cola getC_Ready() {
        return C_Ready;
    }

    public Cola getC_Blocked() {
        return C_Blocked;
    }

    public Cola getC_Suspended() {
        return C_Suspended;
    }

    public Cola getC_finished() {
        return C_finished;
    }

    public Proceso getCurrentRunning_Process() {
        return CurrentRunning_Process;
    }
    
    // Verificar si hay procesos listos
    public boolean HayListos(){
        return !C_Ready.isEmpty();
    }
    
    //obtener siguiente proceso de la cola de listos
    public Proceso Next(){
        return C_Ready.desencolar();
    }
    
    //Método para obtener todos los procesos en el sistema
    public int getTotalProcesos() {
        return C_Ready.size() + C_Blocked.size() + C_Suspended.size() + C_finished.size() + (CurrentRunning_Process != null ? 1 : 0);
    }
    
    // Setter para el planificador
    public void setPlanificador(FCFS planificador) {
        this.planificador = planificador;
    }
    
    // Método para obtener estadísticas básicas
    public String getEstadisticas() {
        return String.format("Procesos - Listos: %d, Bloqueados: %d, Suspendidos: %d, Terminados: %d, Ejecutando: %s",
                C_Ready.size(), C_Blocked.size(), C_Suspended.size(), C_finished.size(),
                (CurrentRunning_Process != null ? CurrentRunning_Process.getName() : "Ninguno"));
    }
    
    @Override
    public String toString() {
        return "ProcessManager{" +
                "Listos=" + C_Ready.size() +
                ", Bloqueados=" + C_Blocked.size() +
                ", Suspendidos=" + C_Suspended.size() +
                ", Terminados=" + C_finished.size() +
                ", Ejecutando=" + (CurrentRunning_Process != null ? CurrentRunning_Process.getName() : "Ninguno") +
                '}';
    }
}
