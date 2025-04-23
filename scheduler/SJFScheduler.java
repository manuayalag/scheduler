package scheduler;

import java.util.*;

public class SJFScheduler extends SchedulerAlgorithm {
    private boolean preemptive;

    public SJFScheduler(List<ProcessControlBlock> procs, boolean preemptive) {
        super(new ArrayList<>(procs));
        this.preemptive = preemptive;
        this.gantt = new ArrayList<>();
    }

    @Override
    public void execute() {
        int time = 0;
        PriorityQueue<ProcessControlBlock> pq = new PriorityQueue<>(Comparator
                .comparingInt(preemptive ? ProcessControlBlock::getRemainingTime : ProcessControlBlock::getBurstTime)
                .thenComparingInt(ProcessControlBlock::getArrivalTime));
        List<ProcessControlBlock> arrivalList = new ArrayList<>(processes);
        arrivalList.sort(Comparator.comparingInt(ProcessControlBlock::getArrivalTime));
        int idx = 0;
        while (idx < arrivalList.size() || !pq.isEmpty()) {
            while (idx < arrivalList.size() && arrivalList.get(idx).getArrivalTime() <= time) {
                pq.add(arrivalList.get(idx)); idx++;
            }
            if (pq.isEmpty()) {
                time = arrivalList.get(idx).getArrivalTime();
                continue;
            }
            ProcessControlBlock cur = pq.poll();
            if (cur.getRemainingTime() == cur.getBurstTime()) cur.setStartTime(time);
            int exec = preemptive ? 1 : cur.getRemainingTime();
            cur.setRemainingTime(cur.getRemainingTime() - exec);
            int start = time;
            time += exec;
            gantt.add(new GanttEntry(cur.getPid(), start, time));
            if (cur.getRemainingTime() > 0) {
                while (idx < arrivalList.size() && arrivalList.get(idx).getArrivalTime() <= time) {
                    pq.add(arrivalList.get(idx)); idx++;
                }
                pq.add(cur);
            } else {
                cur.setFinishTime(time);
                cur.setTurnaroundTime(cur.getFinishTime() - cur.getArrivalTime());
                cur.setWaitingTime(cur.getTurnaroundTime() - cur.getBurstTime());
            }
        }
    }
}
