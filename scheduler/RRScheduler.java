package scheduler;

import java.util.*;

public class RRScheduler extends SchedulerAlgorithm {
    private int quantum;

    public RRScheduler(List<ProcessControlBlock> procs, int quantum) {
        super(new ArrayList<>(procs));
        this.quantum = quantum;
        this.gantt = new ArrayList<>();
    }

    @Override
    public void execute() {
        Queue<ProcessControlBlock> queue = new LinkedList<>();
        List<ProcessControlBlock> arrivalList = new ArrayList<>(processes);
        arrivalList.sort(Comparator.comparingInt(ProcessControlBlock::getArrivalTime));
        int time = 0, idx = 0;
        while (idx < arrivalList.size() || !queue.isEmpty()) {
            while (idx < arrivalList.size() && arrivalList.get(idx).getArrivalTime() <= time)
                queue.add(arrivalList.get(idx++));
            if (queue.isEmpty()) {
                time = arrivalList.get(idx).getArrivalTime();
                continue;
            }
            ProcessControlBlock cur = queue.poll();
            if (cur.getRemainingTime() == cur.getBurstTime()) cur.setStartTime(time);
            int exec = Math.min(quantum, cur.getRemainingTime());
            cur.setRemainingTime(cur.getRemainingTime() - exec);
            int start = time;
            time += exec;
            gantt.add(new GanttEntry(cur.getPid(), start, time));
            while (idx < arrivalList.size() && arrivalList.get(idx).getArrivalTime() <= time)
                queue.add(arrivalList.get(idx++));
            if (cur.getRemainingTime() > 0) queue.add(cur);
            else {
                cur.setFinishTime(time);
                cur.setTurnaroundTime(cur.getFinishTime() - cur.getArrivalTime());
                cur.setWaitingTime(cur.getTurnaroundTime() - cur.getBurstTime());
            }
        }
    }
}
