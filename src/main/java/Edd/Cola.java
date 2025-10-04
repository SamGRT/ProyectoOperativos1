/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Edd;
import model.Proceso;


/**
 *
 * @author Samantha
 */
public class Cola {
    private Proceso[] procesos;
    private int size;
    private int capacity;

    public Cola() {
        this.procesos = new Proceso[capacity];
        this.size = 0;
        this.capacity =10;
    }
    
    public void encolar(Proceso proceso){
        if (size == capacity){
            resize();
        }
            procesos[size++] = proceso;
        }
    
      public boolean isEmpty(){
         return size ==0 ;
     }
      
    public Proceso desencolar(){
        if(isEmpty()) {
            return null;
        }
        Proceso proceso = procesos[0];
        for (int i = 0; i < size; i++) {
            procesos[i]= procesos[i+1];
        }
        procesos[--size] = null;
        return proceso;
    }   
   
    public void resize(){
        int newCapacity = capacity*2;
        Proceso[] newArray = new Proceso[newCapacity];
        
        for (int i = 0; i < size; i++) {
            newArray[i]=procesos[i];
        }
        procesos = newArray;
        capacity = newCapacity;
        
    }
    
    public Proceso peek (){
        if (isEmpty()){
            return null;
        }
        return procesos[0];
    }
    
    public int size() {
            return size;
        }
    
    public Proceso get(int index){
        if (index< 0 || index >= size) {
            return null;
        }
        return procesos[index];
    }
    
    public boolean contiene(Proceso proceso){
        for (int i = 0; i < size; i++) {
            if (procesos[i] != null && procesos[i].equals(proceso)) {
                return true;
            }
        }
        return false;
    }
    
    public void remove(Proceso proceso){
        for (int i = 0; i < size; i++) {
            if (procesos[i] != null && procesos[i].equals(proceso)) {
                for (int j = i; j < size; j++) {
                    procesos[j]=procesos[j+1];
                }
                procesos[--size ] =null;
                return;
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Queue[");
        for (int i = 0; i < size; i++) {
            if (procesos[i] != null) {
                sb.append(procesos[i].getName());
                if (i < size - 1) sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    
    
    
    
}
