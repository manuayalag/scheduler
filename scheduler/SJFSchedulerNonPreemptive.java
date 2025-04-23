package scheduler;

import java.util.*;

/**
 * SJF no preemptivo
 */
public class SJFSchedulerNonPreemptive extends SchedulerAlgorithm {
    public SJFSchedulerNonPreemptive(List<ProcessControlBlock> procs) {
        super(new ArrayList<>(procs));
        this.gantt = new ArrayList<>();
    }

    @Override
    public void execute() {
        int time = 0;
        PriorityQueue<ProcessControlBlock> pq = new PriorityQueue<>(Comparator
            .comparingInt(ProcessControlBlock::getBurstTime)
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
            cur.setWaitingTime(time - cur.getArrivalTime());
            int exec = cur.getBurstTime();
            time += exec;
            cur.setFinishTime(time);
            cur.setTurnaroundTime(cur.getFinishTime() - cur.getArrivalTime());
            gantt.add(new GanttEntry(cur.getPid(), cur.getStartTime(), cur.getFinishTime()));
        }
    }
}
