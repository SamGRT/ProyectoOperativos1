package utils;

import model.Proceso;
import Simulacion.ProcessManager;
import Simulacion.Clock;
import Edd.Cola;
import Edd.ListaEnlazada;

/**
 *
 * @author sarazo
 */
public class MetricsCalculator {
    private ProcessManager processManager;
    private ListaEnlazada historialMetricas;
    private int ultimoCiclo;
    private int procesosCompletadosUltimoCiclo;
    
    //Clase interna para datos históricos
    public static class MetricData {
        public int ciclo;
        public double throughput;
        public double utilizacionCPU;
        public double equidad;
        public double tiempoRespuestaPromedio;
        public int procesosCompletados;
        
        public MetricData(int ciclo, double throughput, double utilizacionCPU, double equidad, double tiempoRespuesta, int procesosCompletados) {
            this.ciclo = ciclo;
            this.throughput = throughput;
            this.utilizacionCPU = utilizacionCPU;
            this.equidad = equidad;
            this.tiempoRespuestaPromedio = tiempoRespuesta;
            this.procesosCompletados = procesosCompletados;
        }
        
        @Override
        public String toString() {
            return String.format("Ciclo %d: Throughput=%.2f, CPU=%.1f%%, Equidad=%.1f%%, Tiempo=%.1f", ciclo, throughput, utilizacionCPU, equidad, tiempoRespuestaPromedio);
        }
    }
    
    public MetricsCalculator(ProcessManager processManager) {
        this.processManager = processManager;
        this.historialMetricas = new ListaEnlazada();
        this.ultimoCiclo = 0;
        this.procesosCompletadosUltimoCiclo = 0;
    }
    
    public void capturarMetricas() {
        int cicloActual = Clock.getInstance().getCurrentCycle();
        
        if(cicloActual <= ultimoCiclo) {
            return;
        }
        
        double throughput = calcularThroughput();
        double utilizacionCPU = calcularUtilizacionCPU();
        double equidad = calcularEquidad();
        double tiempoRespuesta = calcularTiempoRespuestaPromedio();
        int procesosCompletados = processManager.getC_finished().size();
        
        MetricData nuevaMetrica = new MetricData(cicloActual, throughput, utilizacionCPU, equidad, tiempoRespuesta, procesosCompletados);
        
        historialMetricas.agregar(nuevaMetrica);
        
        //Limitar historial para evitar uso exclusivo de memoria
        if (historialMetricas.tamaño() > 1000) {
            historialMetricas.eliminar(0);
        }
    }
    
    public double calcularThroughput() {
        long ciclosTotales = Clock.getInstance().getCurrentCycle();
        if (ciclosTotales == 0) return 0;
        
        int procesosCompletados = processManager.getC_finished().size();
        return(double) procesosCompletados / ciclosTotales;
    }
    
    public double calcularUtilizacionCPU() {
        return processManager.getPorcentajeUtilizacionCPU();
    }
    
    public double calcularEquidad() {
        Cola terminados = processManager.getC_finished();
        if (terminados.size() < 2) return 100.0;
        
        //Calcular suma y contador
        double suma = 0;
        int count = 0;
        
        for (int i = 0; i < terminados.size(); i++) {
            Proceso p = terminados.get(i);
            if (p != null) {
                suma += p.getCiclos_ejecutados();
                count++;
            }
        }
        
        if (count == 0) return 100.0;
        
        double media = suma / count;
        double varianza = 0;
        
        //calcular la varianza
        for (int i = 0; i < terminados.size(); i++) {
            Proceso p = terminados.get(i);
            if (p != null) {
                double diferencia = p.getCiclos_ejecutados() - media;
                varianza += diferencia * diferencia;
            }
        }
        
        varianza /= count;
        double desviacion = Math.sqrt(varianza);
        
        //Equidad  inversamente proporcional a la desviacion
        double equidad = Math.max(0, 100 - (desviacion / media * 100));
        return equidad;
    }
    
    public double calcularTiempoRespuestaPromedio() {
        Cola terminados = processManager.getC_finished();
        if (terminados.isEmpty()) return 0;
        
        double sumaTiempos = 0;
        int count = 0;
        
        for (int i = 0; i < terminados.size(); i++) {
            Proceso p = terminados.get(i);
            if (p != null) {
                sumaTiempos += p.getCiclos_ejecutados();
                count++;
            }
        }
        
        return count > 0 ? sumaTiempos / count : 0;
    }
    
    //Método para obtener las últimas N métricas
    public ListaEnlazada getUltimasMetricas(int cantidad) {
        ListaEnlazada ultimas = new ListaEnlazada();
        int totalMetricas = historialMetricas.tamaño();
        int inicio = Math.max(0, totalMetricas - cantidad);
        
        for (int i = inicio; i < totalMetricas; i++) {
            ultimas.agregar(historialMetricas.obtener(i));
        }
        
        return ultimas;
    }
    
    public MetricData getMetricasActuales() {
        if (historialMetricas.estaVacia()) {
            return new MetricData(0, 0, 0, 0, 0, 0);
        }
        return (MetricData) historialMetricas.obtener(historialMetricas.tamaño() - 1);
    }
    
    public ListaEnlazada getHistorialCompleto() {
        return historialMetricas;
    }
    
    public int getTotalMetricas() {
        return historialMetricas.tamaño();
    }
    
    public void reiniciar() {
        historialMetricas.limpiar();
        ultimoCiclo = 0;
        procesosCompletadosUltimoCiclo = 0;
        if (processManager != null) {
            processManager.reiniciarEstadisticasCPU();
        }
    }
}
