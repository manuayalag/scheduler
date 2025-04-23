package scheduler;

import java.util.List;

public abstract class SchedulerAlgorithm {
    protected List<ProcessControlBlock> processes;
    protected List<GanttEntry> gantt;

    public SchedulerAlgorithm(List<ProcessControlBlock> procs) {
        this.processes = procs;
    }

    // Ejecuta el algoritmo y llena la lista de Gantt
    public abstract void execute();

    // Promedio de tiempos de espera
    public double averageWaitingTime() {
        return processes.stream().mapToInt(ProcessControlBlock::getWaitingTime).average().orElse(0);
    }

    // Promedio de turnaround
    public double averageTurnaroundTime() {
        return processes.stream().mapToInt(ProcessControlBlock::getTurnaroundTime).average().orElse(0);
    }

    public List<GanttEntry> getGantt() {
        return gantt;
    }
}
