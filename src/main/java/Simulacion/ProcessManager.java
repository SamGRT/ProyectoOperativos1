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
    private Cola C_Suspended_Ready;
    private Cola C_Suspended_Blocked;
    private Cola C_finished;
    
    private Proceso CurrentRunning_Process;
    private FCFS planificador;

    public ProcessManager() {
        this.C_Ready = new Cola();
        this.C_Blocked = new Cola();
        this.C_Suspended_Ready = new Cola();
         this.C_Suspended_Blocked= new Cola();
        this.C_finished =new Cola();
        this.CurrentRunning_Process =null;
        this.planificador = null;
    }
    
    //Constructor con planificador integrado
    public ProcessManager(FCFS planificador) {
        this.C_Ready = new Cola();
        this.C_Blocked = new Cola();
        this.C_Suspended_Ready = new Cola();
        this.C_Suspended_Blocked = new Cola();
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
    
 //Ready --> Suspended_Ready (por falta de memoria)
    public void SuspendReadyProcess(Proceso proceso){
        if (C_Ready.contiene(proceso)) {
            proceso.setProcessState(Status.Ready_Suspended);
            C_Ready.remove(proceso);
            C_Suspended_Ready.encolar(proceso);
        }
    
    }
    // blocked --> Suspended_Blocked
    public void SuspendBlockedProcess(Proceso proceso){
        if (C_Blocked.contiene(proceso)) {
            proceso.setProcessState(Status.Blocked_Suspended);
            C_Blocked.remove(proceso);
            C_Suspended_Blocked.encolar(proceso);
            
        }
    }
    
    //Suspended_Ready ---> Ready (memoria disponible)
    public void SuspendR_toReady(Proceso proceso){
        if (C_Suspended_Ready.contiene(proceso)) {
            proceso.setProcessState(Status.Ready);
            C_Suspended_Ready.remove(proceso);
            C_Ready.encolar(proceso);
            
            if (planificador != null) {
                planificador.agregarProceso(proceso);
            }
        }
    }
    
    // Suspended_Blocked ----> Blocked (memoria disponible)
    public void SuspendB_toBlocked(Proceso proceso){
        if (C_Suspended_Blocked.contiene(proceso)) {
            proceso.setProcessState(Status.Blocked);
            C_Suspended_Blocked.remove(proceso);
            C_Blocked.encolar(proceso);
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
        C_Suspended_Ready.remove(proceso);
        C_Suspended_Blocked.remove(proceso);
        
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

    public Cola getC_Suspended_Ready() {
        return C_Suspended_Ready;
    }

    public void setC_Suspended_Ready(Cola C_Suspended_Ready) {
        this.C_Suspended_Ready = C_Suspended_Ready;
    }

    public Cola getC_Suspended_Blocked() {
        return C_Suspended_Blocked;
    }

    public void setC_Suspended_Blocked(Cola C_Suspended_Blocked) {
        this.C_Suspended_Blocked = C_Suspended_Blocked;
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
        return C_Ready.size() + C_Blocked.size() + 
               C_Suspended_Ready.size() + C_Suspended_Blocked.size() + 
               C_finished.size() + (CurrentRunning_Process != null ? 1 : 0);
    }
    
    // Setter para el planificador
    public void setPlanificador(FCFS planificador) {
        this.planificador = planificador;
    }
    
    // Método para obtener estadísticas básicas
public String getEstadisticas() {
        return String.format(
            "Procesos - Listos: %d, Bloqueados: %d, Suspendidos(Listos): %d, Suspendidos(Bloqueados): %d, Terminados: %d, Ejecutando: %s",
            C_Ready.size(), 
            C_Blocked.size(), 
            C_Suspended_Ready.size(), 
            C_Suspended_Blocked.size(), 
            C_finished.size(),
            (CurrentRunning_Process != null ? CurrentRunning_Process.getName() : "Ninguno")
        );
    }

// Método para verificar si un proceso está en alguna cola
    public boolean contieneProceso(Proceso proceso) {
        return C_Ready.contiene(proceso) || 
               C_Blocked.contiene(proceso) || 
               C_Suspended_Ready.contiene(proceso) || 
               C_Suspended_Blocked.contiene(proceso) || 
               C_finished.contiene(proceso) ||
               CurrentRunning_Process == proceso;
    }
    
    @Override
    public String toString() {
        return "ProcessManager{" +
                "Listos=" + C_Ready.size() +
                ", Bloqueados=" + C_Blocked.size() +
                ", Suspendidos(Listos)=" + C_Suspended_Ready.size() +
                ", Suspendidos(Bloqueados)=" + C_Suspended_Blocked.size() +
                ", Terminados=" + C_finished.size() +
                ", Ejecutando=" + (CurrentRunning_Process != null ? CurrentRunning_Process.getName() : "Ninguno") +
                '}';
    }
}
