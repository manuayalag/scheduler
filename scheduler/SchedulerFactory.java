
package scheduler;

import java.util.List;

@FunctionalInterface
public interface SchedulerFactory {
    SchedulerAlgorithm create(List<ProcessControlBlock> procs);
}
