package scheduler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SchedulerGUI extends JFrame {
    private JTable processTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> algoBox;
    private JTextField quantumField;
    private JPanel ganttPanel;
    private List<ProcessControlBlock> processes;
    private JLabel avgLabel;

    public SchedulerGUI() {
        setTitle("Process Scheduler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        JButton loadButton = new JButton("Load CSV");
        JButton runButton = new JButton("Run Scheduling");

        loadButton.addActionListener(e -> {
            File file = new File("scheduler/Procesos.csv");
            try {
                loadCSV(file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(SchedulerGUI.this, "Error loading file: " + ex.getMessage());
            }
        });

        runButton.addActionListener(e -> runScheduling());

        algoBox = new JComboBox<>(new String[] { "FCFS", "SJF NP", "SJF P", "Round Robin", "HRRN", "Priority NP",
                "Priority P", "MultiLevelQueue" });
        quantumField = new JTextField(5);
        quantumField.setEnabled(false);

        algoBox.addActionListener(e -> {
            String selectedAlgo = algoBox.getSelectedItem().toString();
            // Habilitar el campo Quantum solo para Round Robin y MultiLevelQueue
            quantumField.setEnabled(selectedAlgo.equals("Round Robin") || selectedAlgo.equals("MultiLevelQueue"));
        });

        controlPanel.add(loadButton);
        controlPanel.add(runButton);
        controlPanel.add(new JLabel("Algorithm:"));
        controlPanel.add(algoBox);
        controlPanel.add(new JLabel("Quantum:"));
        controlPanel.add(quantumField);
        add(controlPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[] { "Process", "Arrival", "Burst", "Priority" }, 0);
        processTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(processTable);
        add(tableScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        ganttPanel = new GanttChartPanel();

        JScrollPane ganttScroll = new JScrollPane(ganttPanel);
        ganttScroll.setPreferredSize(new Dimension(800, 400));
        bottomPanel.add(ganttScroll, BorderLayout.CENTER);

        avgLabel = new JLabel("Averages: Waiting Time: N/A, Turnaround Time: N/A");
        bottomPanel.add(avgLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCSV(File file) throws IOException {
        processes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            tableModel.setRowCount(0); // limpiar la tabla
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // saltar encabezado
                    continue;
                }
                String[] cols = line.split(",");
                if (cols.length < 4)
                    continue; // validar que existan los 4 datos
                ProcessControlBlock pcb = new ProcessControlBlock(
                        cols[0].trim(),
                        Integer.parseInt(cols[1].trim()),
                        Integer.parseInt(cols[2].trim()),
                        Integer.parseInt(cols[3].trim()));
                processes.add(pcb);
                tableModel.addRow(
                        new Object[] { pcb.getPid(), pcb.getArrivalTime(), pcb.getBurstTime(), pcb.getPriority() });
            }
        }
    }

    // Helper para clonar la lista original de PCB
    private List<ProcessControlBlock> cloneProcesses() {
        return processes.stream()
                .map(p -> new ProcessControlBlock(
                        p.getPid(),
                        p.getArrivalTime(),
                        p.getBurstTime(),
                        p.getPriority()))
                .collect(Collectors.toList());
    }

    private void runScheduling() {
        if (processes == null || processes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No processes loaded!");
            return;
        }

        String selectedAlgo = algoBox.getSelectedItem().toString();
        List<GanttBlock> schedule = new ArrayList<>();
        double totalWaiting = 0;
        double totalTurnaround = 0;
        int count = processes.size();

        try {
            List<GanttEntry> ganttEntries;
            switch (selectedAlgo) {
                case "FCFS": {
                    FCFS fcfs = new FCFS(cloneProcesses());
                    fcfs.execute();
                    ganttEntries = fcfs.getGantt();
                    break;
                }
                case "SJF NP": {
                    SJFSchedulerNonPreemptive sjfNP = new SJFSchedulerNonPreemptive(cloneProcesses());
                    sjfNP.execute();
                    ganttEntries = sjfNP.getGantt();
                    break;
                }
                case "SJF P": {
                    SJFSchedulerPreemptive sjfP = new SJFSchedulerPreemptive(cloneProcesses());
                    sjfP.execute();
                    ganttEntries = sjfP.getGantt();
                    break;
                }
                case "Round Robin": {
                    int q = Integer.parseInt(quantumField.getText().trim());
                    RoundRobinScheduler rr = new RoundRobinScheduler(cloneProcesses(), q);
                    rr.execute();
                    ganttEntries = rr.getGantt();
                    break;
                }
                case "HRRN": {
                    HRRNScheduler hrrn = new HRRNScheduler(cloneProcesses());
                    hrrn.execute();
                    ganttEntries = hrrn.getGantt();
                    break;
                }
                case "Priority NP": {
                    PrioritySchedulerNonPreemptive prioNP = new PrioritySchedulerNonPreemptive(cloneProcesses());
                    prioNP.execute();
                    ganttEntries = prioNP.getGantt();
                    break;
                }
                case "Priority P": {
                    PrioritySchedulerPreemptive prioP = new PrioritySchedulerPreemptive(cloneProcesses());
                    prioP.execute();
                    ganttEntries = prioP.getGantt();
                    break;
                }
                case "MultiLevelQueue": {
                    int quantum = Integer.parseInt(quantumField.getText().trim());
                    List<SchedulerFactory> queues = Arrays.asList(
                            // prioridad 1 → Round Robin con el quantum que introduzca usuario
                            procs -> new RoundRobinScheduler(procs, quantum),
                            // prioridad 2 → FCFS
                            FCFS::new,
                            // prioridad 3 → SJF no preemptivo
                            SJFSchedulerNonPreemptive::new
                    );

                    MultiLevelQueueScheduler mlq = new MultiLevelQueueScheduler(cloneProcesses(), queues);
                    mlq.execute();
                    ganttEntries = mlq.getGantt();
                    totalWaiting = mlq.averageWaitingTime() * count;
                    totalTurnaround = mlq.averageTurnaroundTime() * count;
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown algorithm selected.");
            }
            // Conversión de GanttEntry a GanttBlock
            for (GanttEntry entry : ganttEntries) {
                schedule.add(new GanttBlock(entry.getPid(), entry.getStart(), entry.getEnd()));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error during scheduling: " + e.getMessage());
            return;
        }

        double avgWaiting = totalWaiting / count;
        double avgTurnaround = totalTurnaround / count;
        avgLabel.setText(
                String.format("Averages: Waiting Time: %.2f, Turnaround Time: %.2f", avgWaiting, avgTurnaround));

        ((GanttChartPanel) ganttPanel).setSchedule(schedule);
        ganttPanel.repaint();
    }

    // Clase para representar cada bloque del Gantt
    class GanttBlock {
        String processName;
        int startTime;
        int endTime;

        public GanttBlock(String processName, int startTime, int endTime) {
            this.processName = processName;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    // Panel personalizado para dibujar el diagrama de Gantt
    class GanttChartPanel extends JPanel {
        private List<GanttBlock> schedule;

        public void setSchedule(List<GanttBlock> schedule) {
            this.schedule = schedule;
            // Redimensionar automáticamente la altura del panel basado en la cantidad de
            // procesos
            int rowCount = (int) schedule.stream().map(block -> block.processName).distinct().count();
            int blockHeight = 30;
            int spacing = 10;
            int panelHeight = rowCount * (blockHeight + spacing) + 60;
            setPreferredSize(new Dimension(1000, panelHeight));
            revalidate();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (schedule == null || schedule.isEmpty()) {
                g.drawString("No scheduling to display.", 10, 20);
                return;
            }

            int blockHeight = 30;
            int verticalSpacing = 10;
            int yStart = 30;

            Set<String> uniqueProcesses = new LinkedHashSet<>();
            for (GanttBlock block : schedule) {
                uniqueProcesses.add(block.processName);
            }

            Map<String, Integer> processRow = new HashMap<>();
            int row = 0;
            for (String pid : uniqueProcesses) {
                processRow.put(pid, row++);
            }

            int totalTime = schedule.get(schedule.size() - 1).endTime;
            int panelWidth = getWidth();
            int scale = Math.max(1, panelWidth / (totalTime + 1));

            for (GanttBlock block : schedule) {
                int x = block.startTime * scale;
                int width = (block.endTime - block.startTime) * scale;
                int y = yStart + processRow.get(block.processName) * (blockHeight + verticalSpacing);

                g.setColor(Color.YELLOW);
                g.fillRect(x, y, width, blockHeight);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, width, blockHeight);
                g.drawString(block.processName, x + 5, y + blockHeight / 2 + 5);
            }

            for (Map.Entry<String, Integer> entry : processRow.entrySet()) {
                int y = yStart + entry.getValue() * (blockHeight + verticalSpacing);
                g.drawString(entry.getKey(), 0, y + blockHeight / 2);
            }

            for (int t = 0; t <= totalTime; t++) {
                int x = t * scale;
                g.drawString(Integer.toString(t), x, yStart - 5);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SchedulerGUI().setVisible(true));
    }
}
