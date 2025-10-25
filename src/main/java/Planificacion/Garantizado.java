package Planificacion;

import model.Proceso;
import model.Status;
import Edd.Cola;
import Edd.ListaEnlazada;
import Simulacion.ProcessManager;
import utils.Logger;
import utils.Semaforo;

public class Garantizado implements AlgoritmoPlanificacion {
    private ProcessManager processManager;
    private Proceso procesoActual;
    private Semaforo semaforoCola;
    private Logger logger;
    
    private ListaEnlazada registrosProcesos;
    private int ciclosDesdeReinicio;
    
    private class RegistroProceso {
        Proceso proceso;
        int tiempoEjecutado;
        int tiempoAsignado;
        
        RegistroProceso(Proceso proceso) {
            this.proceso = proceso;
            this.tiempoEjecutado = 0;
            this.tiempoAsignado = 0;
        }
    }
    
    public Garantizado(ProcessManager processManager) {
        this.processManager = processManager;
        this.semaforoCola = new Semaforo(1);
        this.logger = Logger.getInstancia();
        this.registrosProcesos = new ListaEnlazada();
        this.ciclosDesdeReinicio = 0;
    }
    
    @Override
    public void agregarProceso(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            
            // Solo agregar si no ha terminado y no está ya registrado
            if (proceso.End() || existeRegistro(proceso)) {
                semaforoCola.liberar();
                return;
            }
            
            proceso.setProcessState(Status.Ready);
            
            // Inicializar seguimiento para nuevo proceso
            registrosProcesos.agregar(new RegistroProceso(proceso));
            
            logger.log(String.format("[DEBUG Garantizado] Proceso %s agregado. Total registros: %d", 
                proceso.getName(), registrosProcesos.tamaño()));
            
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Error Garantizado al agregar proceso: " + e.getMessage());
        }
    }
    
    @Override
public Proceso obtenerSiguienteProceso() {
    try {
        semaforoCola.adquirir();
        
        ciclosDesdeReinicio++;
        
        // CORRECCIÓN: Limpiar registros ANTES de cualquier cálculo
        limpiarRegistrosProcesosTerminados();
        
        // Reiniciar contadores periódicamente
        if (ciclosDesdeReinicio > 50) { // Reducido a 50 para más frecuencia
            reiniciarContadores();
        }
        
        int totalProcesosActivos = getTotalProcesosActivos();
        
        // Verificar SIEMPRE si el proceso actual excedió su tiempo
        if (procesoActual != null && !procesoActual.End() && 
            procesoActual.getProcessState() == Status.Running) {
            
            int tiempoEjecutado = obtenerTiempoEjecutado(procesoActual);
            int tiempoGarantizado = calcularTiempoGarantizado(procesoActual, totalProcesosActivos);
            
            logger.log(String.format("[DEBUG Garantizado] Proceso %s - Ejecutado: %d, Garantizado: %d, TotalProcesos: %d", 
                procesoActual.getName(), tiempoEjecutado, tiempoGarantizado, totalProcesosActivos));
            
            
            if (tiempoEjecutado >= tiempoGarantizado || tiempoEjecutado >= 5) {
                // El proceso ha agotado su tiempo garantizado, mover a fin de cola
                if (!procesoActual.End()) {
                    procesoActual.setProcessState(Status.Ready);
                    //Agregar a la cola del ProcessManager
                    if (processManager != null && !processManager.getC_Ready().contiene(procesoActual)) {
                        processManager.getC_Ready().encolar(procesoActual);
                    }
                    logger.log(String.format("⏰ Proceso %s movido a cola (límite tiempo: %d/%d ciclos)", 
                        procesoActual.getName(), tiempoEjecutado, tiempoGarantizado));
                }
                procesoActual = null;
            } else {
                // El proceso puede continuar ejecutándose
                actualizarTiempoEjecutado(procesoActual, 1);
                semaforoCola.liberar();
                return procesoActual;
            }
        }
        
        // Buscar siguiente proceso con menor relación tiempo-ejecutado/tiempo-garantizado
        Proceso siguiente = encontrarProcesoMasAtrasado(totalProcesosActivos);
        
        if (siguiente != null && !siguiente.End() && siguiente.getProcessState() == Status.Ready) {
            siguiente.setProcessState(Status.Running);
            procesoActual = siguiente;
            //Remover de la cola del ProcessManager
            if (processManager != null && processManager.getC_Ready().contiene(siguiente)) {
                processManager.getC_Ready().remove(siguiente);
            }
            actualizarTiempoEjecutado(siguiente, 1);
            logger.log(String.format(" Garantizado selecciona Proceso %s", siguiente.getName()));
        } else {
            procesoActual = null;
            if (siguiente != null) {
                logger.log(String.format("[DEBUG] Proceso %s descartado - Estado: %s, Terminado: %s", 
                    siguiente.getName(), siguiente.getProcessState(), siguiente.End()));
            }
        }
        
        semaforoCola.liberar();
        return procesoActual;
        
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return null;
    }
}
    
    // CORRECCIÓN CRÍTICA: Método para obtener el número REAL de procesos activos
    private int getTotalProcesosActivos() {
        int count = 0;
        for (int i = 0; i < registrosProcesos.tamaño(); i++) {
            RegistroProceso registro = (RegistroProceso) registrosProcesos.obtener(i);
            if (registro.proceso != null && !registro.proceso.End() && 
                registro.proceso.getProcessState() != Status.Finished) {
                count++;
            }
        }
        return Math.max(1, count); // Nunca devolver 0
    }
    
    //  Método mejorado para calcular tiempo garantizado
    private int calcularTiempoGarantizado(Proceso proceso, int totalProcesosActivos) {
        if (totalProcesosActivos <= 0) return 1;
        
        // Garantizado: cada proceso tiene derecho a 1/n del tiempo
        // Usar un quantum base más pequeño para mejor distribución
        int quantumBase = 5; // Quantum base de 5 ciclos
        int tiempoBase = Math.max(1, quantumBase / totalProcesosActivos);
        
        // Ajustar según tipo de proceso
        if (proceso.isCPUbound()) {
            return tiempoBase;
        } else {
            return Math.max(1, tiempoBase); // I/O-bound obtienen lo mismo por equidad
        }
    }
    
    // CORRECCIÓN: Método mejorado para encontrar proceso más atrasado
    private Proceso encontrarProcesoMasAtrasado(int totalProcesosActivos) {
        if (processManager.getC_Ready().isEmpty()) {
            return null;
        }
        
        Proceso masAtrasado = null;
        double menorRelacion = Double.MAX_VALUE;
        int procesosValidos = 0;
        
        // Buscar en todos los procesos listos
        Cola colaTemp = new Cola();
        
        while (!processManager.getC_Ready().isEmpty()) {
            Proceso candidato = processManager.getC_Ready().desencolar();
            colaTemp.encolar(candidato);
            
            // VERIFICACIÓN CRÍTICA: Solo considerar procesos válidos y listos
            if (candidato != null && !candidato.End() && candidato.getProcessState() == Status.Ready) {
                int tiempoEjecutado = obtenerTiempoEjecutado(candidato);
                int tiempoGarantizado = calcularTiempoGarantizado(candidato, totalProcesosActivos);
                
                if (tiempoGarantizado > 0) {
                    double relacion = (double) tiempoEjecutado / tiempoGarantizado;
                    
                    logger.log(String.format("[DEBUG] Proceso %s - Relación: %.2f (Ejec: %d, Gar: %d, Procesos: %d)", 
                        candidato.getName(), relacion, tiempoEjecutado, tiempoGarantizado, totalProcesosActivos));
                    
                    if (relacion < menorRelacion) {
                        menorRelacion = relacion;
                        masAtrasado = candidato;
                    }
                    procesosValidos++;
                }
            }
        }
        
        // Reconstruir la cola original
        while (!colaTemp.isEmpty()) {
            Proceso p = colaTemp.desencolar();
            if (p != masAtrasado && p != null && !p.End()) {
                processManager.getC_Ready().encolar(p);
            }
        }
        
        if (masAtrasado != null) {
            logger.log(String.format("[DEBUG] Proceso más atrasado: %s con relación %.2f (de %d procesos válidos)", 
                masAtrasado.getName(), menorRelacion, procesosValidos));
        } else {
            logger.log("[DEBUG] No se encontró proceso válido para ejecutar");
        }
        
        return masAtrasado;
    }
    
    // NUEVO: Método CRÍTICO para limpiar registros de procesos terminados
    private void limpiarRegistrosProcesosTerminados() {
        ListaEnlazada registrosAEliminar = new ListaEnlazada();
        
        for (int i = 0; i < registrosProcesos.tamaño(); i++) {
            RegistroProceso registro = (RegistroProceso) registrosProcesos.obtener(i);
            if (registro.proceso == null || registro.proceso.End() || 
                registro.proceso.getProcessState() == Status.Finished) {
                registrosAEliminar.agregar(i);
                logger.log(String.format("[DEBUG] Eliminando registro de proceso terminado: %s", 
                    registro.proceso != null ? registro.proceso.getName() : "null"));
            }
        }
        
        // Eliminar en orden inverso para evitar problemas de índices
        for (int i = registrosAEliminar.tamaño() - 1; i >= 0; i--) {
            int index = (Integer) registrosAEliminar.obtener(i);
            registrosProcesos.eliminar(index);
        }
    }
    
    private void reiniciarContadores() {
        for (int i = 0; i < registrosProcesos.tamaño(); i++) {
            RegistroProceso registro = (RegistroProceso) registrosProcesos.obtener(i);
            if (registro.proceso != null && !registro.proceso.End()) {
                registro.tiempoEjecutado = Math.max(0, registro.tiempoEjecutado - 10);
            }
        }
        ciclosDesdeReinicio = 0;
        logger.log("[DEBUG] Contadores de Garantizado reiniciados");
    }
    
    private void actualizarTiempoAsignado(Proceso proceso, int incremento) {
        for (int i = 0; i < registrosProcesos.tamaño(); i++) {
            RegistroProceso registro = (RegistroProceso) registrosProcesos.obtener(i);
            if (registro.proceso == proceso) {
                registro.tiempoAsignado += incremento;
                return;
            }
        }
    }
    
    private boolean existeRegistro(Proceso proceso) {
        for (int i = 0; i < registrosProcesos.tamaño(); i++) {
            RegistroProceso registro = (RegistroProceso) registrosProcesos.obtener(i);
            if (registro.proceso == proceso) {
                return true;
            }
        }
        return false;
    }
    
    private int obtenerTiempoEjecutado(Proceso proceso) {
        for (int i = 0; i < registrosProcesos.tamaño(); i++) {
            RegistroProceso registro = (RegistroProceso) registrosProcesos.obtener(i);
            if (registro.proceso == proceso) {
                return registro.tiempoEjecutado;
            }
        }
        return 0;
    }
    
    public void notificarCicloCompletado(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            actualizarTiempoEjecutado(proceso, 1);
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void actualizarTiempoEjecutado(Proceso proceso, int incremento) {
        for (int i = 0; i < registrosProcesos.tamaño(); i++) {
            RegistroProceso registro = (RegistroProceso) registrosProcesos.obtener(i);
            if (registro.proceso == proceso) {
                registro.tiempoEjecutado += incremento;
                return;
            }
        }
    }
    
    public void notificarSalidaProceso(Proceso proceso) {
        try {
            semaforoCola.adquirir();
            eliminarRegistroProceso(proceso);
            
            if (procesoActual == proceso) {
                procesoActual = null;
            }
            semaforoCola.liberar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void eliminarRegistroProceso(Proceso proceso) {
        for (int i = 0; i < registrosProcesos.tamaño(); i++) {
            RegistroProceso registro = (RegistroProceso) registrosProcesos.obtener(i);
            if (registro.proceso == proceso) {
                registrosProcesos.eliminar(i);
                logger.log(String.format("[DEBUG] Registro eliminado para proceso: %s", proceso.getName()));
                return;
            }
        }
    }
    public boolean debePreemptar(Proceso proceso) {
    try {
        semaforoCola.adquirir();
        
        if (proceso == null || proceso.End() || proceso.getProcessState() != Status.Running) {
            semaforoCola.liberar();
            return false;
        }
        
        int totalProcesosActivos = getTotalProcesosActivos();
        int tiempoEjecutado = obtenerTiempoEjecutado(proceso);
        int tiempoGarantizado = calcularTiempoGarantizado(proceso, totalProcesosActivos);
        
        boolean debePreemptar = tiempoEjecutado >= tiempoGarantizado;
        
        if (debePreemptar) {
            logger.log(String.format("[PREEMPT] Proceso %s - Ejecutado: %d, Límite: %d, Procesos: %d", 
                proceso.getName(), tiempoEjecutado, tiempoGarantizado, totalProcesosActivos));
        }
        
        semaforoCola.liberar();
        return debePreemptar;
        
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
    }
}
    @Override
    public Cola getColaListos() {
        return processManager != null ? processManager.getC_Ready() : null;
    }
    
    @Override
    public String getNombre() {
        return "Garantizado (Fair Share)";
    }
}