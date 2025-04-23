package scheduler;

import java.util.*;

/**
 * Priority no preemptivo
 */
public class PrioritySchedulerNonPreemptive extends SchedulerAlgorithm {
    public PrioritySchedulerNonPreemptive(List<ProcessControlBlock> procs) {
        super(new ArrayList<>(procs));
        this.gantt = new ArrayList<>();
    }

    @Override
    public void execute() {
        int time = 0;
        PriorityQueue<ProcessControlBlock> pq = new PriorityQueue<>(Comparator
            .comparingInt(ProcessControlBlock::getPriority)
            .thenComparingInt(ProcessControlBlock::getArrivalTime));
        List<ProcessControlBlock> arrivalList = new ArrayList<>(processes);
        arrivalList.sort(Comparator.comparingInt(ProcessControlBlock::getArrivalTime));
        int idx = 0;

        while (idx < arrivalList.size() || !pq.isEmpty()) {
            while (idx < arrivalList.size() && arrivalList.get(idx).getArrivalTime() <= time) {
                pq.add(arrivalList.get(idx++));
            }
            if (pq.isEmpty()) {
                time = arrivalList.get(idx).getArrivalTime();
                continue;
            }
            ProcessControlBlock cur = pq.poll();
            cur.setStartTime(time);
            int exec = cur.getRemainingTime();
            time += exec;
            cur.setRemainingTime(0);
            cur.setFinishTime(time);
            cur.setTurnaroundTime(cur.getFinishTime() - cur.getArrivalTime());
            cur.setWaitingTime(cur.getTurnaroundTime() - cur.getBurstTime());
            gantt.add(new GanttEntry(cur.getPid(), cur.getStartTime(), cur.getFinishTime()));
        }
    }
}