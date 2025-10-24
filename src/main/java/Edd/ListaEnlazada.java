package Edd;

/**
 *
 * @author sarazo
 */
public class ListaEnlazada {
    private Nodo cabeza;
    private Nodo cola;
    private int tamaño;
    
    private class Nodo {
        Object dato;
        Nodo siguiente;
        
        Nodo(Object dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }
    
    public ListaEnlazada() {
        this.cabeza = null;
        this.cola = null;
        this.tamaño = 0;
    }
    
    public void agregar(Object dato) {
        Nodo nuevoNodo = new Nodo(dato);
        if (cabeza == null) {
            cabeza = nuevoNodo;
            cola = nuevoNodo;
        } else {
            cola.siguiente = nuevoNodo;
            cola = nuevoNodo;
        }
        tamaño++;
    }
    
    public Object obtener(int index) {
        if (index < 0 || index >= tamaño) {
            return null;
        }
        
        Nodo actual = cabeza;
        for (int i = 0; i < index; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }
    
    public int tamaño() {
        return tamaño;
    }
    
    public boolean estaVacia() {
        return tamaño == 0;
    }
    
    public void eliminar(int index) {
        if (index < 0 || index >= tamaño) {
            return;
        }
        
        if (index == 0) {
            cabeza = cabeza.siguiente;
            if (cabeza == null) {
                cola = null;
            }
        } else {
            Nodo anterior = cabeza;
            for (int i = 0; i < index -1; i++) {
                anterior = anterior.siguiente;
            }
            anterior.siguiente = anterior.siguiente.siguiente;
            
            if (index == tamaño - 1) {
                cola = anterior;
            }
        }
        tamaño--;
    }
    
    public void limpiar() {
        cabeza = null;
        cola = null;
        tamaño = 0;
    }
    
    @Override
    public String toString() {
        if (estaVacia()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        Nodo actual = cabeza;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) {
                sb.append("; ");
            }
            actual = actual.siguiente;
        }
        sb.append("]");
        return sb.toString();
    }
}
