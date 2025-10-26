/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Samantha
 * this is PCB 
 */
public class Proceso {
    private final int id;
    private final String name;
    private Status ProcessState;
    private int PC; // Requerido: 49, 54
    private int mar;            // Requerido: 58
    private final int Total_Instructions;
    private final boolean isCPUbound;
    private int ciclosParaExcepcion; //Ciclos para generar excepcion
    private int ciclosPendientes; //ciclos para satisfacer excepcion
    private int ciclos_ejecutados; 
    private int ciclosDesdeUltimaExcepcion;
    private static int nextId = 0;  // Generador de IDs únicos
    private int memoriaAsignada;
    /*Lo que coloca el user**/

    public Proceso( String name, int Total_Instructions, boolean isCPUbound, int ciclosParaExcepcion, int ciclosPendientes) {
        this.id = nextId++;
        this.name = name;
        this.ProcessState = Status.New;
        this.PC = 0;
        this.mar = 0;
        this.Total_Instructions = Total_Instructions;
        this.isCPUbound = isCPUbound;
        this.ciclosParaExcepcion = ciclosParaExcepcion;
        this.ciclosPendientes = ciclosPendientes;
        this.ciclos_ejecutados =0;
        this.ciclosDesdeUltimaExcepcion = 0;
    }



    public int getId() {
        return id;
    }

    public void setProcessState(Status ProcessState) {
        this.ProcessState = ProcessState;
    }

    public String getName() {
        return name;
    }

    public Status getProcessState() {
        return ProcessState;
    }

    public int getPC() {
        return PC;
    }

    public int getMar() {
        return mar;
    }

    public int getTotal_Instructions() {
        return Total_Instructions;
    }

    public boolean isCPUbound() {
        return isCPUbound;
    }

    public int getCiclosParaExcepcion() {
        return ciclosParaExcepcion;
    }

    public int getCiclosPendientes() {
        return ciclosPendientes;
    }

    public int getCiclos_ejecutados() {
        return ciclos_ejecutados;
    }
    
    public void setCiclosPendientes(int ciclosPendientes) {
        this.ciclosPendientes = ciclosPendientes;
    }

    public void increment_Cycles() {  
    this.ciclos_ejecutados++;
    this.ciclosDesdeUltimaExcepcion++;
    }

    //Verificar si el proceso termino
   
    public boolean End(){
       return PC >= Total_Instructions ; 
    }
   
    //verificar si se debe generar excepcion para i/o bound
    public boolean generate_EXC(){
       if (isCPUbound || ciclosParaExcepcion == 0) {
           return false; //CPU bound no genera excepciones I/O
                 }
       
       boolean debeGenerar = (ciclosDesdeUltimaExcepcion >= ciclosParaExcepcion) && (ciclos_ejecutados > 0);
       if (debeGenerar) {
           ciclosDesdeUltimaExcepcion = 0; //Reiniciar contador
       }
       return debeGenerar;
    }
   
    public void resetAfterIO() {
        this.ciclosPendientes = 0;
        this.ciclosDesdeUltimaExcepcion = 0;
    }
   
    // Ejecutar una instrucción
 
    public boolean executeInstruction() {
    if (End()) {
        return false;
    }
    
    PC++;    
    mar++;
    ciclos_ejecutados++;
    ciclosDesdeUltimaExcepcion++;
    return true;
}
     public int getMemoriaAsignada() {
         return memoriaAsignada; }
     
    public void setMemoriaAsignada(int memoria) { 
        this.memoriaAsignada = memoria; }
    @Override
    public String toString() {
        return String.format("Process[%s: %s, PC=%d, State=%s]", id, name, PC, ProcessState);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Proceso other = (Proceso) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
    
    
  
   
    

