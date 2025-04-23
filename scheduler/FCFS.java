package scheduler;

import java.util.*;

public class FCFS extends SchedulerAlgorithm {
    public FCFS(List<ProcessControlBlock> procs) {
        super(new ArrayList<>(procs));
        this.gantt = new ArrayList<>();
    }

    @Override
    public void execute() {
        processes.sort(Comparator.comparingInt(ProcessControlBlock::getArrivalTime));
        int time = 0;
        for (ProcessControlBlock p : processes) {
            if (time < p.getArrivalTime()) time = p.getArrivalTime();
            p.setStartTime(time);
            p.setWaitingTime(time - p.getArrivalTime());
            time += p.getBurstTime();
            p.setFinishTime(time);
            p.setTurnaroundTime(p.getFinishTime() - p.getArrivalTime());
            gantt.add(new GanttEntry(p.getPid(), p.getStartTime(), p.getFinishTime()));
        }
    }
}
