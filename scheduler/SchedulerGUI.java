package scheduler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

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
            File file = new File("src/scheduler/Procesos.csv");
            try {
                loadCSV(file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(SchedulerGUI.this, "Error loading file: " + ex.getMessage());
            }
        });

        runButton.addActionListener(e -> runScheduling());

        algoBox = new JComboBox<>(new String[] { "FCFS", "SJF", "Round Robin"});
        quantumField = new JTextField(5);
        quantumField.setEnabled(false);

        algoBox.addActionListener(e -> {
            quantumField.setEnabled(algoBox.getSelectedItem().toString().equals("Round Robin"));
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

    private void runScheduling() {
        if (processes == null || processes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No processes loaded!");
            return;
        }

        String selectedAlgo = algoBox.getSelectedItem().toString();
        List<GanttBlock> schedule = new ArrayList<>();
        int currentTime = 0;
        double totalWaiting = 0;
        double totalTurnaround = 0;
        List<ProcessControlBlock> procCopy = new ArrayList<>(processes);

        if (selectedAlgo.equals("FCFS")) {
            // Ordenar por tiempo de llegada
            Collections.sort(procCopy, Comparator.comparingInt(ProcessControlBlock::getArrivalTime));
            for (ProcessControlBlock pcb : procCopy) {
                int start = Math.max(currentTime, pcb.getArrivalTime());
                int waiting = start - pcb.getArrivalTime();
                int finish = start + pcb.getBurstTime();
                int turnaround = finish - pcb.getArrivalTime();
                totalWaiting += waiting;
                totalTurnaround += turnaround;
                schedule.add(new GanttBlock(pcb.getPid(), start, finish));
                currentTime = finish;
            }
        } else if (selectedAlgo.equals("SJF")) {
            // Shortest Job First (no preemptivo)
            procCopy.sort(Comparator.comparingInt(ProcessControlBlock::getArrivalTime));
            List<ProcessControlBlock> ready = new ArrayList<>();
            currentTime = procCopy.get(0).getArrivalTime();
            while (!procCopy.isEmpty() || !ready.isEmpty()) {
                while (!procCopy.isEmpty() && procCopy.get(0).getArrivalTime() <= currentTime) {
                    ready.add(procCopy.remove(0));
                }
                if (ready.isEmpty()) {
                    currentTime = procCopy.get(0).getArrivalTime();
                    continue;
                }
                ready.sort(Comparator.comparingInt(ProcessControlBlock::getBurstTime));
                ProcessControlBlock pcb = ready.remove(0);
                int start = currentTime;
                int waiting = start - pcb.getArrivalTime();
                int finish = start + pcb.getBurstTime();
                int turnaround = finish - pcb.getArrivalTime();
                totalWaiting += waiting;
                totalTurnaround += turnaround;
                schedule.add(new GanttBlock(pcb.getPid(), start, finish));
                currentTime = finish;
            }
        } else if (selectedAlgo.equals("Round Robin")) {
            // Round Robin scheduling
            int quantum;
            try {
                quantum = Integer.parseInt(quantumField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid quantum.");
                return;
            }
            Queue<ProcessControlBlock> queue = new LinkedList<>();
            procCopy.sort(Comparator.comparingInt(ProcessControlBlock::getArrivalTime));
            currentTime = procCopy.get(0).getArrivalTime();
            int index = 0;
            // Mapa para mantener el burst restante de cada proceso
            Map<ProcessControlBlock, Integer> remainingBurst = new HashMap<>();
            for (ProcessControlBlock pcb : processes) {
                remainingBurst.put(pcb, pcb.getBurstTime());
            }
            // Encolar procesos que han llegado
            while (index < procCopy.size() && procCopy.get(index).getArrivalTime() <= currentTime) {
                queue.add(procCopy.get(index));
                index++;
            }
            while (!queue.isEmpty()) {
                ProcessControlBlock pcb = queue.poll();
                int rem = remainingBurst.get(pcb);
                int start = currentTime;
                int execTime = Math.min(quantum, rem);
                currentTime += execTime;
                rem -= execTime;
                schedule.add(new GanttBlock(pcb.getPid(), start, currentTime));
                remainingBurst.put(pcb, rem);
                // Encolar procesos nuevos que han llegado durante la ejecución
                while (index < procCopy.size() && procCopy.get(index).getArrivalTime() <= currentTime) {
                    queue.add(procCopy.get(index));
                    index++;
                }
                if (rem > 0) {
                    queue.add(pcb);
                } else {
                    int turnaround = currentTime - pcb.getArrivalTime();
                    int waiting = turnaround - pcb.getBurstTime();
                    totalWaiting += waiting;
                    totalTurnaround += turnaround;
                }
                if (queue.isEmpty() && index < procCopy.size()) {
                    currentTime = procCopy.get(index).getArrivalTime();
                    queue.add(procCopy.get(index));
                    index++;
                }
            }
        }

        // Calcular promedios
        int count = processes.size();
        double avgWaiting = totalWaiting / count;
        double avgTurnaround = totalTurnaround / count;
        avgLabel.setText(
                String.format("Averages: Waiting Time: %.2f, Turnaround Time: %.2f", avgWaiting, avgTurnaround));

        // Update y refresco del diagrama de Gantt
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
