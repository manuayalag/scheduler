package scheduler;

import java.util.*;

public class MultiLevelQueueScheduler extends SchedulerAlgorithm {
    private List<SchedulerAlgorithm> queues;

    public MultiLevelQueueScheduler(List<ProcessControlBlock> procs, List<SchedulerAlgorithm> queues) {
        super(procs);
        this.queues = queues;
        this.gantt = new ArrayList<>();
    }

    @Override
    public void execute() {
        for (int i = 0; i < queues.size(); i++) {
            SchedulerAlgorithm algo = queues.get(i);
            List<ProcessControlBlock> subset = new ArrayList<>();
            for (ProcessControlBlock p : processes) {
                if (p.getPriority() == i+1) subset.add(p);
            }
            try {
                SchedulerAlgorithm inst = algo.getClass()
                    .getConstructor(List.class)
                    .newInstance(subset);
                inst.execute();
                gantt.addAll(inst.getGantt());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        gantt.sort(Comparator.comparingInt(GanttEntry::getStart));
    }
}
