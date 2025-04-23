package scheduler;

import java.util.*;

public class HRRNScheduler extends SchedulerAlgorithm {
    public HRRNScheduler(List<ProcessControlBlock> procs) {
        super(new ArrayList<>(procs));
        this.gantt = new ArrayList<>();
    }

    @Override
    public void execute() {
        List<ProcessControlBlock> ready = new ArrayList<>();
        List<ProcessControlBlock> arrivalList = new ArrayList<>(processes);
        arrivalList.sort(Comparator.comparingInt(ProcessControlBlock::getArrivalTime));
        int time = 0, idx = 0;
        while (idx < arrivalList.size() || !ready.isEmpty()) {
            while (idx < arrivalList.size() && arrivalList.get(idx).getArrivalTime() <= time)
                ready.add(arrivalList.get(idx++));
            if (ready.isEmpty()) {
                time = arrivalList.get(idx).getArrivalTime();
                continue;
            }
            ProcessControlBlock best = null;
            double maxRR = -1;
            for (ProcessControlBlock p : ready) {
                double rr = (time - p.getArrivalTime() + p.getBurstTime()) / (double) p.getBurstTime();
                if (rr > maxRR) { maxRR = rr; best = p; }
            }
            ready.remove(best);
            best.setStartTime(time);
            best.setWaitingTime(time - best.getArrivalTime());
            int start = time;
            time += best.getBurstTime();
            best.setFinishTime(time);
            best.setTurnaroundTime(best.getFinishTime() - best.getArrivalTime());
            gantt.add(new GanttEntry(best.getPid(), start, time));
        }
    }
}
