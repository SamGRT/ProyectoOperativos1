/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Simulacion;

import model.Proceso;
import model.Status;
import Edd.Cola;
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

    public ProcessManager() {
        this.C_Ready = new Cola();
        this.C_Blocked = new Cola();
        this.C_Suspended = new Cola();
        this.C_finished =new Cola();
        this.CurrentRunning_Process =null;
    }
    
    // Agregar nuevo proceso al sistema a LISTO 
    public void addProcess(Proceso proceso) {
        proceso.setProcessState(Status.Ready);
        C_Ready.encolar(proceso);
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
    
    // suspender ( creo que Blocked --> Suspended)
    public void SuspendProcess (Proceso proceso){
        proceso.setProcessState(Status.Suspended);
        C_Ready.remove(proceso);
        C_Blocked.remove(proceso);
        C_Suspended.encolar(proceso);
        }
    
    //Reanudar Proceso suspendido (Suspended --> Ready)
    public void Reanudar (Proceso proceso){
        proceso.setProcessState(Status.Ready);
        C_Suspended.remove(proceso);
        C_Ready.encolar(proceso);
    }
    
    //END process (Running --> Finished)
    public void EndProcess(){
        if (CurrentRunning_Process != null) {
            CurrentRunning_Process.setProcessState(Status.Finished);
            C_finished.encolar(CurrentRunning_Process);
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
    
}
