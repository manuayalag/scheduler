package scheduler;

import java.util.*;
import java.util.stream.Collectors;

public class MultiLevelQueueScheduler extends SchedulerAlgorithm {
    private List<SchedulerFactory> factories;

    public MultiLevelQueueScheduler(List<ProcessControlBlock> procs,
                                    List<SchedulerFactory> factories) {
        super(procs);
        this.factories = factories;
        this.gantt     = new ArrayList<>();
    }

    @Override
    public void execute() {
        int currentTime = 0;

        // Por cada nivel de prioridad (i→prioridad = i+1)
        for (int i = 0; i < factories.size(); i++) {
            int level = i + 1;
            // Filtrar los PCB de esta prioridad
            List<ProcessControlBlock> subset = processes.stream()
                .filter(p -> p.getPriority() == level)
                .map(p -> new ProcessControlBlock(
                    p.getPid(), 0,
                    p.getBurstTime(), p.getPriority()))
                .collect(Collectors.toList());

            if (subset.isEmpty()) continue;

            // Creamos el scheduler adecuado y lo ejecutamos
            SchedulerAlgorithm algo = factories.get(i).create(subset);
            algo.execute();

            // Desplazamos sus bloques en el tiempo global
            for (GanttEntry e : algo.getGantt()) {
                gantt.add(new GanttEntry(
                    e.getPid(),
                    e.getStart() + currentTime,
                    e.getEnd()   + currentTime
                ));
            }

            // Avanzamos currentTime al final de este subdiagrama
            int lastEnd = algo.getGantt().stream()
                .mapToInt(GanttEntry::getEnd)
                .max().orElse(0);
            currentTime += lastEnd;
        }
    }
}
