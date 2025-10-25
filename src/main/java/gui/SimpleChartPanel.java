package gui;

import javax.swing.*;
import java.awt.*;
import utils.MetricsCalculator;
import Edd.ListaEnlazada;

/**
 *
 * @author sarazo
 */
public class SimpleChartPanel extends JPanel {
    private ListaEnlazada datos;
    private String tipoGrafica;
    private Color colorGrafica;
    
    public SimpleChartPanel(String tipoGrafica, Color color) {
        this.tipoGrafica = tipoGrafica;
        this.colorGrafica = color;
        setPreferredSize(new Dimension(400, 200));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    
    public void setDatos(ListaEnlazada datos) {
        this.datos = datos;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (datos == null || datos.tamaño() < 2) {
            g.setColor(Color.GRAY);
            g.drawString("No hay suficientes datos para graficar", 50, 100);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int padding = 40;
        
        //Dibujar ejes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding, padding, padding, height - padding); //EJE Y
        g2d.drawLine(padding, height - padding, width - padding, height - padding); //EJE X
        
        //Calcular escalas
        double maxValor = getMaxValue();
        double minValor = getMinValue();
        double rango = maxValor - minValor;
        if (rango == 0) rango = 1; //Evitar division por cero
        
        System.out.println("Gráfica " + tipoGrafica + " - Min: " + minValor + ", Max: " + maxValor + ", Rango: " + rango);//DEBUG
        
        //Dibujar datos
        g2d.setColor(colorGrafica);
        int puntoAnteriorX = -1, puntoAnteriorY = -1;
        
        for (int i = 0; i < datos.tamaño(); i++) {
            MetricsCalculator.MetricData data = (MetricsCalculator.MetricData) datos.obtener(i);
            if(data == null) continue;
            double valor = getValue(data);
            
            System.out.println("Punto " + i + ": " + valor);//DEBUG
            
            int x = padding + (i * (width - 2 * padding) / Math.max(1, datos.tamaño() - 1));
            int y = height - padding - (int)((valor - minValor) * (height - 2 * padding) / rango);
            
            //Dibujar punto
            g2d.fillOval(x - 2, y - 2, 4, 4);
            
            //Dibujar línea
            if (puntoAnteriorX != -1) {
                g2d.drawLine(puntoAnteriorX, puntoAnteriorY, x, y);
            }
            
            puntoAnteriorX = x;
            puntoAnteriorY = y;
        }
        
        //Título y escalas
        g2d.setColor(Color.BLACK);
        g2d.drawString(tipoGrafica, width / 2 - 30, 20);
        g2d.drawString(String.format("Max: %.1f", maxValor), width - 60, 30);
        g2d.drawString(String.format("Min: %.1f", minValor), width - 60, height - 10);
    }
    
    private double getValue(MetricsCalculator.MetricData data) {
        if (data == null) return 0;
        
        switch (tipoGrafica) {
            case "Throughput": return data.throughput;
            case "Utilizacion CPU": return data.utilizacionCPU;
            case "Equidad": return data.equidad;
            case "Tiempo Respuesta": return data.tiempoRespuestaPromedio;
            default: return 0;
        }
    }
    
    private double getMaxValue() {
        double max = Double.MIN_VALUE;
        for (int i = 0; i < datos.tamaño(); i++) {
            MetricsCalculator.MetricData data = (MetricsCalculator.MetricData) datos.obtener(i);
            double valor = getValue(data);
            if (valor > max) max = valor;
        }
        return max == Double.MIN_VALUE ? 100 : max;
    }
    
    private double getMinValue() {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < datos.tamaño(); i++) {
            MetricsCalculator.MetricData data = (MetricsCalculator.MetricData) datos.obtener(i);
            double valor = getValue(data);
            if (valor < min) min = valor;
        }
        return min == Double.MAX_VALUE ? 0 : min;
    }
}
