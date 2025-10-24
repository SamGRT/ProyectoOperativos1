/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package gui;

import model.Proceso;
import model.Status;
import Simulacion.ProcessManager;
import Simulacion.Planificador;
import Simulacion.CPU;
import Simulacion.Clock;
import Planificacion.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import Edd.Cola;
import Edd.ListaEnlazada;
import java.awt.BorderLayout;
import utils.MetricsCalculator;
import Simulacion.ExceptionHandler;

/**
 *
 * @author Samantha
 */
public class mainWindow extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(mainWindow.class.getName());

    // Componentes de simulación
    private ProcessManager processManager;
    private Planificador planificador;
    private CPU cpu;
    private Timer simulacionTimer;
    private MetricsCalculator metricsCalculator;
   
    private boolean simulationRunning = false;
    private int currentCycleDuration = 500; 
    
   
    private javax.swing.JPanel colaListosPanel; //paneles de pcb listos
    private javax.swing.JPanel colaBloqueadosPanel;
    private javax.swing.JPanel colaSuspendedReadyPanel;
    private javax.swing.JPanel colaSuspendedBlockedPanel;
    private javax.swing.JPanel colaFinishedPanel;
    private javax.swing.JPanel cpuPanel;
    
    //Componentes para métricas
    private javax.swing.JPanel metricsPanel;
    private javax.swing.JLabel throughputLabel;
    private javax.swing.JLabel utilizacionLabel;
    private javax.swing.JLabel equidadLabel;
    private javax.swing.JLabel tiempoRespuestaLabel;
    private javax.swing.JTextArea metricsTextArea;
    private SimpleChartPanel throughputChart;
    private SimpleChartPanel utilizacionChart;
    private SimpleChartPanel equidadChart;
    private SimpleChartPanel tiempoRespuestaChart;
    private ExceptionHandler exceptionHandler;
    
    /**
     * Creates new form mainWindow
     */
    public mainWindow() {
        initComponents();
        this.colaListosPanel = new javax.swing.JPanel();
        this.colaListosPanel.setLayout(new javax.swing.BoxLayout(colaListosPanel, javax.swing.BoxLayout.X_AXIS));
        Cola_Listos.setViewportView(colaListosPanel);
        
        this.colaBloqueadosPanel = new javax.swing.JPanel();
        this.colaBloqueadosPanel.setLayout(new javax.swing.BoxLayout(colaBloqueadosPanel, javax.swing.BoxLayout.X_AXIS));
        Cola_Blocked.setViewportView(colaBloqueadosPanel);
        
         // para cpu       
        jPanelCpu.setLayout(new BorderLayout());
        
        
  
        initializeSimulation();
        initializeMetricsPanel();
        setupEventListeners();
        startSimulation() ;
        updateGUI();
        
    }
    
     private void initializeSimulation() {
        // Inicializar componentes de simulación
        this.processManager = new ProcessManager();
        this.cpu = new CPU(processManager);
        this.planificador = new Planificador(cpu,processManager);
        //conectar manejador de excepciones
        this.exceptionHandler = new ExceptionHandler(processManager);
    exceptionHandler.iniciar();
        // Configurar timer para la simulación
        simulacionTimer = new Timer(currentCycleDuration, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarCicloSimulacio();
            }
        });
       // Configurar duración del ciclo en el reloj
        Clock.getInstance().setCycleDuration(currentCycleDuration);
    }
     
     private void initializeMetricsPanel() {
         this.metricsCalculator = new MetricsCalculator(processManager);
         
         //Crear panel principal de métricas
         metricsPanel = new javax.swing.JPanel();
         metricsPanel.setLayout(new java.awt.BorderLayout());
         
         //Panel superior con métricas en tiempo real
         javax.swing.JPanel metricsSummaryPanel = new javax.swing.JPanel();
         metricsSummaryPanel.setLayout(new java.awt.GridLayout(2, 4));
         metricsSummaryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Métricas en Tiempo Real"));
         
         throughputLabel = new javax.swing.JLabel("Throughput: 0.0");
         utilizacionLabel = new javax.swing.JLabel("Utilización CPU: 0.0%");
         equidadLabel = new javax.swing.JLabel("Equidad: 0.0%");
         tiempoRespuestaLabel = new javax.swing.JLabel("TiempoRespuesta: 0.0");
         
         metricsSummaryPanel.add(new javax.swing.JLabel("Throughput:"));
         metricsSummaryPanel.add(throughputLabel);
         metricsSummaryPanel.add(new javax.swing.JLabel("Utilización CPU:"));
         metricsSummaryPanel.add(utilizacionLabel);
         metricsSummaryPanel.add(new javax.swing.JLabel("Equidad:"));
         metricsSummaryPanel.add(equidadLabel);
         metricsSummaryPanel.add(new javax.swing.JLabel("Tiempo Respuesta:"));
         metricsSummaryPanel.add(tiempoRespuestaLabel);
         
         //Área de texto para métricas detalladas
         metricsTextArea = new javax.swing.JTextArea();
         metricsTextArea.setEditable(false);
         metricsTextArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
         javax.swing.JScrollPane metricsScrollPane = new javax.swing.JScrollPane(metricsTextArea);
         metricsScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Historial Detallado"));
         metricsScrollPane.setPreferredSize(new java.awt.Dimension(400, 200));
         
         //Panel de gráficas con pestañas
         javax.swing.JTabbedPane chartsTabbedPane = new javax.swing.JTabbedPane();
         
         throughputChart = new SimpleChartPanel("Throughput", java.awt.Color.BLUE);
         utilizacionChart = new SimpleChartPanel("Utilización CPU", java.awt.Color.GREEN);
         equidadChart = new SimpleChartPanel("Equidad", java.awt.Color.ORANGE);
         tiempoRespuestaChart = new SimpleChartPanel("Tiempo Respuesta", java.awt.Color.RED);
         
         chartsTabbedPane.addTab("Throughput", throughputChart);
         chartsTabbedPane.addTab("Utilización CPU", utilizacionChart);
         chartsTabbedPane.addTab("Equidad", equidadChart);
         chartsTabbedPane.addTab("Tiempo Respuesta", tiempoRespuestaChart);
         
         //Timer para actualizar gráficas
         Timer chartsTimer = new Timer(2000, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (metricsCalculator != null) {
                     ListaEnlazada historial = metricsCalculator.getUltimasMetricas(50);
                     throughputChart.setDatos(historial);
                     utilizacionChart.setDatos(historial);
                     equidadChart.setDatos(historial);
                     tiempoRespuestaChart.setDatos(historial);
                 }
             }
         });
         chartsTimer.start();
         
         //Organizar layout
         metricsPanel.add(metricsSummaryPanel, java.awt.BorderLayout.NORTH);
         metricsPanel.add(metricsScrollPane, java.awt.BorderLayout.CENTER);
         metricsPanel.add(chartsTabbedPane, java.awt.BorderLayout.SOUTH);
         
         jTabbedPane1.remove(metrics);
         jTabbedPane1.addTab("Gráficas y Métricas", metricsPanel);
     }
  
  /*   private void debugEstadoCompleto() {
    System.out.println("=== DEBUG COMPLETO DEL SISTEMA ===");
    System.out.println("Reloj: " + Clock.getInstance().getCurrentCycle());
    System.out.println("CPU ejecutando: " + (cpu.isEjecutando() ? "SÍ" : "NO"));
    
    // Ver proceso en CPU
    Proceso cpuProcess = cpu.getProcesoActual();
    Proceso pmProcess = processManager.getCurrentRunning_Process();
    System.out.println("Proceso en CPU: " + (cpuProcess != null ? cpuProcess.getName() : "NULL"));
    System.out.println("Proceso en PM: " + (pmProcess != null ? pmProcess.getName() : "NULL"));
    
    // Ver colas del ProcessManager
    System.out.println("Cola Ready (PM): " + processManager.getC_Ready().size() + " procesos");
    System.out.println("Cola Blocked (PM): " + processManager.getC_Blocked().size() + " procesos");
    
    // Si tienes acceso al ExceptionHandler, mostrar también su cola
    System.out.println("Cola Blocked (EH): " + exceptionHandler.getProcesosBloqueados() + " procesos");
    
    System.out.println("=== FIN DEBUG ===");
}
*/
      public void startSimulation() {
        if (!simulationRunning) {
            simulationRunning = true;
            
            
            Clock.getInstance().start();
            cpu.iniciar();
            planificador.iniciar();
            simulacionTimer.start();
            
            // Actualizar estado en la GUI
            updateSimulationStatus(true);
            
            System.out.println("Simulación iniciada automáticamente");
        }
    }
      private void setupEventListeners() {
        // Configurar valores por defecto
        duracionCiclo_input.setValue(currentCycleDuration);
    }
      
      private void ejecutarCicloSimulacio() {
        if (simulationRunning && Clock.getInstance().isRunning()) {
            // El CPU ya se ejecuta en su propio hilo, solo actualizamos la GUI
            updateGUI();
        }
    }
      
      private void updateGUI() {
        // Actualizar colas de procesos
       
        System.out.println(processManager.debugEstadoCompleto());
        updateReadyC();
        updateBlockedC();
        updateSuspendedC() ;
        updateFinishedC();
        updateRunningProcess();
        updateClockDisplay();
        updateSchedulerInfo();
        updateMetricsDisplay();
        
    }
    private void updateSchedulerInfo() {
    if (planificador != null && planificador.getAlgoritmoActual() != null) {
        String algoritmo = planificador.getAlgoritmoActual().getNombre();
    }
}
    
    private void updateSimulationStatus(boolean running) {
    // Actualizar algún indicador visual del estado
    if (running) {
        // Cambiar color o texto para indicar que está corriendo
        jLabel1.setForeground(java.awt.Color.GREEN);
        jLabel1.setText("CPU: Ejecutando");
    } else {
        jLabel1.setForeground(java.awt.Color.RED);
        jLabel1.setText("CPU: Detenido");
    }
}
    private void updateReadyC() {
        Cola C_Ready = processManager.getC_Ready();
        this.colaListosPanel.removeAll();
       
        if (C_Ready != null && !C_Ready.isEmpty()) {
            for (int i = 0; i < C_Ready.size(); i++) {
            Proceso proceso = C_Ready.get(i);
                if (proceso != null  && proceso.getProcessState() == Status.Ready) {
                    ProcessCard pp = new ProcessCard(proceso);
                    colaListosPanel.add(pp);
                    colaListosPanel.add(javax.swing.Box.createHorizontalStrut(5));
                }
            }
                    
        } else {
         javax.swing.JLabel emptyLabel = new javax.swing.JLabel("COLA DE LISTOS: Vacía");
        emptyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        emptyLabel.setForeground(java.awt.Color.GRAY);
        colaListosPanel.add(emptyLabel);
        }
        
        colaListosPanel.revalidate(); 
        colaListosPanel.repaint();
      
    }
   
     private void updateBlockedC() {
        Cola C_Blocked = processManager.getC_Blocked();
        this.colaBloqueadosPanel.removeAll();
    
        if (C_Blocked != null && !C_Blocked.isEmpty()) {
             for (int i = 0; i < C_Blocked.size(); i++) {
            Proceso proceso = C_Blocked.get(i);
            if (proceso != null && proceso.getProcessState() == Status.Blocked) {
                ProcessCard pp = new ProcessCard(proceso);
                colaBloqueadosPanel.add(pp);
                colaBloqueadosPanel.add(javax.swing.Box.createHorizontalStrut(5));
            }
        }
        } else {
           javax.swing.JLabel emptyLabel = new javax.swing.JLabel("COLA BLOQUEADOS: Vacía");
            emptyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            emptyLabel.setForeground(java.awt.Color.GRAY);
            colaBloqueadosPanel.add(emptyLabel);
        }
       colaBloqueadosPanel.revalidate();
        colaBloqueadosPanel.repaint();
    }
     
     private void updateSuspendedC() {
        // Similar a los métodos anteriores para colas suspendidas
        StringBuilder Text = new StringBuilder("SUSPENDIDOS LISTOS:");
        Text.append(System.lineSeparator());
        if (processManager.getC_Suspended_Ready() != null && !processManager.getC_Suspended_Ready().isEmpty()) {
            Text.append(processManager.getC_Suspended_Ready().toString()).append(System.lineSeparator());
        } else {
            Text.append("Vacía");
            Text.append(System.lineSeparator());
        }
        
        StringBuilder suspendedBlockedText = new StringBuilder("SUSPENDIDOS BLOQUEADOS:\n");
        if (processManager.getC_Suspended_Blocked() != null && !processManager.getC_Suspended_Blocked().isEmpty()) {
            suspendedBlockedText.append(processManager.getC_Suspended_Blocked().toString()).append(System.lineSeparator());
        } else {
            suspendedBlockedText.append("Vacía");
            Text.append(System.lineSeparator());
        }
    }
      private void updateFinishedC() {
        StringBuilder Text = new StringBuilder("PROCESOS TERMINADOS:");
         Text.append(System.lineSeparator());
        if (processManager.getC_finished() != null && !processManager.getC_finished().isEmpty()) {
            Text.append(processManager.getC_finished().toString()).append(System.lineSeparator());
        } else {
              Text.append("Vacía");
             Text.append(System.lineSeparator());
        }
    }
     
      private void updateRunningProcess() {
        Proceso runningProcess = processManager.getCurrentRunning_Process();
        jPanelCpu.removeAll();
        
        if (runningProcess != null && runningProcess.getProcessState() == Status.Running) {
            ProcessCard cpuCard = new ProcessCard(runningProcess);
            jPanelCpu.add(cpuCard, java.awt.BorderLayout.CENTER);
        } else {
            javax.swing.JLabel idleLabel = new javax.swing.JLabel("CPU: Libre - Esperando procesos");
            idleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            idleLabel.setForeground(java.awt.Color.GRAY);
            jPanelCpu.add(idleLabel, java.awt.BorderLayout.CENTER);
        }
        jPanelCpu.revalidate();
        jPanelCpu.repaint();
    }
      
   
    private void updateClockDisplay() {
        clock.setText(Clock.getInstance().getCurrentCycle() + " ciclos");
    }
    
    private void updateMetricsDisplay() {
        if (metricsCalculator != null) {
            metricsCalculator.capturarMetricas();
            MetricsCalculator.MetricData metricas = metricsCalculator.getMetricasActuales();

            // Actualizar labels
            throughputLabel.setText(String.format("Throughput: %.2f proc/seg", metricas.throughput));
            utilizacionLabel.setText(String.format("Utilización CPU: %.1f%%", metricas.utilizacionCPU));
            equidadLabel.setText(String.format("Equidad: %.1f%%", metricas.equidad));
            tiempoRespuestaLabel.setText(String.format("Tiempo Respuesta: %.1f ciclos", metricas.tiempoRespuestaPromedio));

            // Actualizar área de texto con historial usando ListaEnlazada
            StringBuilder sb = new StringBuilder();
            sb.append("Ciclo\tThroughput\tUtilización\tEquidad\tT. Respuesta\n");
            sb.append("------------------------------------------------------------\n");

            ListaEnlazada historial = metricsCalculator.getUltimasMetricas(20); // Últimos 20 registros

            for (int i = 0; i < historial.tamaño(); i++) {
                MetricsCalculator.MetricData data = (MetricsCalculator.MetricData) historial.obtener(i);
                sb.append(String.format("%d\t%.2f\t%.1f%%\t%.1f%%\t%.1f\n", 
                    data.ciclo, data.throughput, data.utilizacionCPU, data.equidad, data.tiempoRespuestaPromedio));
            }

            metricsTextArea.setText(sb.toString());
        }
    }
    
     private void mostrarError(String mensaje) {
        javax.swing.JOptionPane.showMessageDialog(this, mensaje, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }
     
     private void mostrarMensaje(String mensaje) {
        javax.swing.JOptionPane.showMessageDialog(this, mensaje, "Informacion", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void crearProceso() {
        try {
            String nombre = NameInput.getText().trim();
            if (nombre.isEmpty()) {
                mostrarError("El nombre del proceso no puede estar vacío");
                return;
            }
            
            int instrucciones = (Integer) Instrucciones_Input2.getValue();
            String tipo = (String) jComboBox1.getSelectedItem();
            int ciclosExcepcion = (Integer) Instrucciones_Input3.getValue();
            int ciclosSatisfacer = (Integer) Instrucciones_Input1.getValue();
            
            // Validaciones
            if (instrucciones == 0) {
                mostrarError("La cantidad de instrucciones debe ser mayor a 0");
                return;
            }
            
            if (tipo.equals("I/O bound") && (ciclosExcepcion == 0 || ciclosSatisfacer == 0)) {
                mostrarError("Para procesos I/O bound, los ciclos de excepción deben ser mayores a 0");
                return;
            }
            
            // Crear proceso
            boolean isCPUbound = tipo.equals("CPU bound");
            
            Proceso proceso = new Proceso( nombre, instrucciones, isCPUbound, ciclosExcepcion, ciclosSatisfacer);
            
            // Agregar proceso al manager
            processManager.addProcess(proceso); //se agrega el proceso a la cola de listos
            
            // Si el planificador está corriendo, notificarle
            if (planificador != null && planificador.isEjecutando()) {
                planificador.agregarProceso(proceso);
            }
            
            // Limpiar formulario
            limpiarFormulario();
            
            // Actualizar GUI
            updateGUI();
            
            mostrarMensaje("Proceso '" + nombre + "' creado exitosamente");
           
            
        } catch (Exception e) {
            mostrarError("Error al crear proceso: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void limpiarFormulario() {
        NameInput.setText("");
        Instrucciones_Input2.setValue(0);
        Instrucciones_Input3.setValue(0);
        Instrucciones_Input1.setValue(0);
        jComboBox1.setSelectedIndex(0);
    }
    
     private void cambiarPoliticaPlanificacion() {
        String politicaSeleccionada = (String) planificacion_input.getSelectedItem();
        try {
            AlgoritmoPlanificacion nuevoAlgoritmo = null;
            
            switch (politicaSeleccionada) {
                case "Round Robin":
                    nuevoAlgoritmo = new RoundRobin();
                    break;
                case "FCFS":
                    nuevoAlgoritmo = new FCFS();
                    break;
                case "SJF":
                    nuevoAlgoritmo = new SJF();
                    break;
                case "Prioridades":
                    nuevoAlgoritmo = new Prioridades();
                    break;
                case  "Garantizado":
                    nuevoAlgoritmo = new Garantizado();
                    break;
                case  "Multiples Colas":
                    nuevoAlgoritmo = new MultiplesColas();
                    break;
                default:
                    mostrarError("Política no implementada: " + politicaSeleccionada);
                    return;
            }
            
            planificador.cambiarAlgoritmo(nuevoAlgoritmo);
            mostrarMensaje("Política cambiada a: " + politicaSeleccionada);
            
        } catch (Exception e) {
            mostrarError("Error al cambiar política: " + e.getMessage());
            e.printStackTrace();
        }
    }
     
     private void cambiarDuracionCiclo() {
        try {
            int nuevaDuracion = (Integer) duracionCiclo_input.getValue();
            if (nuevaDuracion <= 0) {
                mostrarError("La duración del ciclo debe ser mayor a 0");
                return;
            }
            
            this.currentCycleDuration = nuevaDuracion;
            
            
            Clock.getInstance().setCycleDuration(nuevaDuracion);
            
            // Reiniciar timer de simulación si está corriendo
            if (simulacionTimer != null) {
                simulacionTimer.setDelay(nuevaDuracion);
            }
            
            mostrarMensaje("Duración del ciclo actualizada a: " + nuevaDuracion + "ms");
            
        } catch (Exception e) {
            mostrarError("Error al cambiar duración del ciclo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
   
    
      
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        Simulador = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanelCpu = new javax.swing.JPanel();
        secc1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        create = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        ciclosSatisfacerE_label = new javax.swing.JLabel();
        nombreLabel = new javax.swing.JLabel();
        InstLabel = new javax.swing.JLabel();
        tipoLabel = new javax.swing.JLabel();
        ciclosparaE_label = new javax.swing.JLabel();
        NameInput = new javax.swing.JTextField();
        Instrucciones_Input1 = new javax.swing.JSpinner();
        Instrucciones_Input2 = new javax.swing.JSpinner();
        ciclosparaE_label3 = new javax.swing.JLabel();
        ciclosparaE_label4 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        Instrucciones_Input3 = new javax.swing.JSpinner();
        jPanel5 = new javax.swing.JPanel();
        clock = new javax.swing.JLabel();
        ciclosparaE_label6 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        planificacion_input = new javax.swing.JComboBox<>();
        ciclosparaE_label5 = new javax.swing.JLabel();
        guardarPlanning1 = new javax.swing.JButton();
        ciclosparaE_label8 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        ciclosparaE_label1 = new javax.swing.JLabel();
        ciclosparaE_label2 = new javax.swing.JLabel();
        duracionCiclo_input = new javax.swing.JSpinner();
        duracionCiclo_reset = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        Cola_SuspReady = new javax.swing.JScrollPane();
        Cola_Listos = new javax.swing.JScrollPane();
        Cola_Blocked = new javax.swing.JScrollPane();
        Cola_SuspBlock = new javax.swing.JScrollPane();
        metrics = new javax.swing.JPanel();
        logs = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(0, 204, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        jPanel2.setPreferredSize(new java.awt.Dimension(240, 595));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("CPU");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, 240, 60));

        javax.swing.GroupLayout jPanelCpuLayout = new javax.swing.GroupLayout(jPanelCpu);
        jPanelCpu.setLayout(jPanelCpuLayout);
        jPanelCpuLayout.setHorizontalGroup(
            jPanelCpuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanelCpuLayout.setVerticalGroup(
            jPanelCpuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jPanel2.add(jPanelCpu, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, 180, 220));

        jPanel1.add(jPanel2, java.awt.BorderLayout.LINE_END);

        secc1.setBackground(new java.awt.Color(0, 153, 153));
        secc1.setPreferredSize(new java.awt.Dimension(260, 595));
        secc1.setLayout(new java.awt.BorderLayout());

        jPanel3.setBackground(new java.awt.Color(0, 153, 153));
        jPanel3.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(255, 255, 255)));
        jPanel3.setPreferredSize(new java.awt.Dimension(260, 275));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jLabel2.setFont(new java.awt.Font("Segoe UI Black", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("CREAR PROCESO");
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel2.setInheritsPopupMenu(false);
        jPanel3.add(jLabel2, java.awt.BorderLayout.NORTH);

        jPanel6.setBackground(new java.awt.Color(0, 153, 153));
        jPanel6.setPreferredSize(new java.awt.Dimension(260, 35));

        create.setBackground(new java.awt.Color(204, 204, 204));
        create.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        create.setForeground(new java.awt.Color(0, 0, 0));
        create.setText("Crear");
        create.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        create.setPreferredSize(new java.awt.Dimension(95, 23));
        create.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createActionPerformed(evt);
            }
        });
        jPanel6.add(create);

        jPanel3.add(jPanel6, java.awt.BorderLayout.PAGE_END);

        jPanel7.setBackground(new java.awt.Color(0, 153, 153));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ciclosSatisfacerE_label.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        ciclosSatisfacerE_label.setForeground(new java.awt.Color(255, 255, 255));
        ciclosSatisfacerE_label.setText("Ciclos para Satisfacer ");
        jPanel7.add(ciclosSatisfacerE_label, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, -1, -1));

        nombreLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        nombreLabel.setForeground(new java.awt.Color(255, 255, 255));
        nombreLabel.setText("Nombre:");
        jPanel7.add(nombreLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 20, -1, -1));

        InstLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        InstLabel.setForeground(new java.awt.Color(255, 255, 255));
        InstLabel.setText("Cant. Instrucciones");
        jPanel7.add(InstLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, -1, -1));

        tipoLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tipoLabel.setForeground(new java.awt.Color(255, 255, 255));
        tipoLabel.setText("Tipo");
        jPanel7.add(tipoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 80, -1, -1));

        ciclosparaE_label.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        ciclosparaE_label.setForeground(new java.awt.Color(255, 255, 255));
        ciclosparaE_label.setText("Excepcion");
        jPanel7.add(ciclosparaE_label, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 180, -1, -1));

        NameInput.setBackground(new java.awt.Color(51, 51, 51));
        NameInput.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        NameInput.setPreferredSize(new java.awt.Dimension(68, 24));
        NameInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NameInputActionPerformed(evt);
            }
        });
        jPanel7.add(NameInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 20, 100, -1));

        Instrucciones_Input1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        Instrucciones_Input1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 9999, 1));
        jPanel7.add(Instrucciones_Input1, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 160, -1, -1));

        Instrucciones_Input2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        Instrucciones_Input2.setModel(new javax.swing.SpinnerNumberModel(0, 0, 9999, 1));
        jPanel7.add(Instrucciones_Input2, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, -1, -1));

        ciclosparaE_label3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        ciclosparaE_label3.setForeground(new java.awt.Color(255, 255, 255));
        ciclosparaE_label3.setText("Ciclos para la ");
        jPanel7.add(ciclosparaE_label3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 110, -1, -1));

        ciclosparaE_label4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        ciclosparaE_label4.setForeground(new java.awt.Color(255, 255, 255));
        ciclosparaE_label4.setText("Excepcion");
        jPanel7.add(ciclosparaE_label4, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 130, -1, -1));

        jComboBox1.setBackground(new java.awt.Color(51, 51, 51));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CPU bound", "I/O bound", " " }));
        jPanel7.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 80, -1, -1));

        Instrucciones_Input3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        Instrucciones_Input3.setModel(new javax.swing.SpinnerNumberModel(0, 0, 9999, 1));
        jPanel7.add(Instrucciones_Input3, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 110, -1, -1));

        jPanel3.add(jPanel7, java.awt.BorderLayout.CENTER);

        secc1.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        jPanel5.setBackground(new java.awt.Color(0, 204, 204));
        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        jPanel5.setPreferredSize(new java.awt.Dimension(260, 170));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        clock.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        clock.setForeground(new java.awt.Color(0, 0, 0));
        clock.setText("0");
        jPanel5.add(clock, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 70, 130, 60));

        ciclosparaE_label6.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        ciclosparaE_label6.setForeground(new java.awt.Color(0, 0, 0));
        ciclosparaE_label6.setText("RELOG GLOBAL");
        jPanel5.add(ciclosparaE_label6, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 20, -1, -1));

        secc1.add(jPanel5, java.awt.BorderLayout.PAGE_END);

        jPanel9.setLayout(new java.awt.BorderLayout());

        jPanel10.setBackground(new java.awt.Color(0, 153, 153));
        jPanel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        jPanel10.setPreferredSize(new java.awt.Dimension(135, 175));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        planificacion_input.setBackground(new java.awt.Color(0, 0, 0));
        planificacion_input.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Round Robin", "FCFS", "SJF", "Prioridades", "Garantizado", "Multiples Colas" }));
        jPanel10.add(planificacion_input, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, -1, -1));

        ciclosparaE_label5.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        ciclosparaE_label5.setForeground(new java.awt.Color(0, 0, 0));
        ciclosparaE_label5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ciclosparaE_label5.setText("Politica de ");
        ciclosparaE_label5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel10.add(ciclosparaE_label5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, -1, -1));

        guardarPlanning1.setBackground(new java.awt.Color(204, 204, 204));
        guardarPlanning1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        guardarPlanning1.setForeground(new java.awt.Color(0, 0, 0));
        guardarPlanning1.setText("Guardar ");
        guardarPlanning1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        guardarPlanning1.setPreferredSize(new java.awt.Dimension(95, 23));
        guardarPlanning1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarPlanning1ActionPerformed(evt);
            }
        });
        jPanel10.add(guardarPlanning1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, -1));

        ciclosparaE_label8.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        ciclosparaE_label8.setForeground(new java.awt.Color(0, 0, 0));
        ciclosparaE_label8.setText("Planificacion");
        jPanel10.add(ciclosparaE_label8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        jPanel9.add(jPanel10, java.awt.BorderLayout.LINE_END);

        jPanel8.setBackground(new java.awt.Color(0, 153, 153));
        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ciclosparaE_label1.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        ciclosparaE_label1.setForeground(new java.awt.Color(0, 0, 0));
        ciclosparaE_label1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ciclosparaE_label1.setText(" Duracion del");
        ciclosparaE_label1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel8.add(ciclosparaE_label1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, -1, -1));

        ciclosparaE_label2.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        ciclosparaE_label2.setForeground(new java.awt.Color(0, 0, 0));
        ciclosparaE_label2.setText("ciclo");
        jPanel8.add(ciclosparaE_label2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 60, -1, -1));

        duracionCiclo_input.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        duracionCiclo_input.setModel(new javax.swing.SpinnerNumberModel(0, 0, 9999, 1));
        jPanel8.add(duracionCiclo_input, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        duracionCiclo_reset.setBackground(new java.awt.Color(204, 204, 204));
        duracionCiclo_reset.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        duracionCiclo_reset.setForeground(new java.awt.Color(0, 0, 0));
        duracionCiclo_reset.setText("Reset");
        duracionCiclo_reset.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        duracionCiclo_reset.setPreferredSize(new java.awt.Dimension(95, 23));
        duracionCiclo_reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duracionCiclo_resetActionPerformed(evt);
            }
        });
        jPanel8.add(duracionCiclo_reset, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 90, -1));

        jPanel9.add(jPanel8, java.awt.BorderLayout.CENTER);

        secc1.add(jPanel9, java.awt.BorderLayout.CENTER);

        jPanel1.add(secc1, java.awt.BorderLayout.LINE_START);

        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel7.setBackground(new java.awt.Color(255, 255, 255));
        jLabel7.setFont(new java.awt.Font("Segoe UI Black", 1, 10)); // NOI18N
        jLabel7.setText("COLA SUSPENDIDOS/LISTOS");
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 540, 200, 20));

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setFont(new java.awt.Font("Segoe UI Black", 1, 10)); // NOI18N
        jLabel4.setText("COLA LISTOS");
        jPanel4.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 90, 20));

        jLabel5.setBackground(new java.awt.Color(255, 255, 255));
        jLabel5.setFont(new java.awt.Font("Segoe UI Black", 1, 10)); // NOI18N
        jLabel5.setText("COLA BLOQUEADOS");
        jPanel4.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 180, 120, 20));

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Segoe UI Black", 1, 10)); // NOI18N
        jLabel6.setText("COLA SUSPENDIDOS/BLOQUEADO");
        jPanel4.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 360, 200, 20));
        jPanel4.add(Cola_SuspReady, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 570, 930, 160));
        jPanel4.add(Cola_Listos, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 30, 930, 140));
        jPanel4.add(Cola_Blocked, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 200, 930, 160));
        jPanel4.add(Cola_SuspBlock, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 380, 930, 160));

        jPanel1.add(jPanel4, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout SimuladorLayout = new javax.swing.GroupLayout(Simulador);
        Simulador.setLayout(SimuladorLayout);
        SimuladorLayout.setHorizontalGroup(
            SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1522, Short.MAX_VALUE)
        );
        SimuladorLayout.setVerticalGroup(
            SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Simulador", Simulador);

        javax.swing.GroupLayout metricsLayout = new javax.swing.GroupLayout(metrics);
        metrics.setLayout(metricsLayout);
        metricsLayout.setHorizontalGroup(
            metricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1522, Short.MAX_VALUE)
        );
        metricsLayout.setVerticalGroup(
            metricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 754, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Gráficas y Métricas", metrics);

        javax.swing.GroupLayout logsLayout = new javax.swing.GroupLayout(logs);
        logs.setLayout(logsLayout);
        logsLayout.setHorizontalGroup(
            logsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1522, Short.MAX_VALUE)
        );
        logsLayout.setVerticalGroup(
            logsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 754, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("logs", logs);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void createActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createActionPerformed
           crearProceso();
    }//GEN-LAST:event_createActionPerformed

    private void duracionCiclo_resetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duracionCiclo_resetActionPerformed
        cambiarDuracionCiclo();
    }//GEN-LAST:event_duracionCiclo_resetActionPerformed

    private void guardarPlanning1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarPlanning1ActionPerformed
        cambiarPoliticaPlanificacion();
    }//GEN-LAST:event_guardarPlanning1ActionPerformed

    private void NameInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NameInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NameInputActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new mainWindow().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane Cola_Blocked;
    private javax.swing.JScrollPane Cola_Listos;
    private javax.swing.JScrollPane Cola_SuspBlock;
    private javax.swing.JScrollPane Cola_SuspReady;
    private javax.swing.JLabel InstLabel;
    private javax.swing.JSpinner Instrucciones_Input1;
    private javax.swing.JSpinner Instrucciones_Input2;
    private javax.swing.JSpinner Instrucciones_Input3;
    private javax.swing.JTextField NameInput;
    private javax.swing.JPanel Simulador;
    private javax.swing.JLabel ciclosSatisfacerE_label;
    private javax.swing.JLabel ciclosparaE_label;
    private javax.swing.JLabel ciclosparaE_label1;
    private javax.swing.JLabel ciclosparaE_label2;
    private javax.swing.JLabel ciclosparaE_label3;
    private javax.swing.JLabel ciclosparaE_label4;
    private javax.swing.JLabel ciclosparaE_label5;
    private javax.swing.JLabel ciclosparaE_label6;
    private javax.swing.JLabel ciclosparaE_label8;
    private javax.swing.JLabel clock;
    private javax.swing.JButton create;
    private javax.swing.JSpinner duracionCiclo_input;
    private javax.swing.JButton duracionCiclo_reset;
    private javax.swing.JButton guardarPlanning1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelCpu;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel logs;
    private javax.swing.JPanel metrics;
    private javax.swing.JLabel nombreLabel;
    private javax.swing.JComboBox<String> planificacion_input;
    private javax.swing.JPanel secc1;
    private javax.swing.JLabel tipoLabel;
    // End of variables declaration//GEN-END:variables
}
