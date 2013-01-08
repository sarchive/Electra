package net.electra.tasks;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;

public class PhasedExecutor
{
	public static void run(ExecutorService executorService, List<CollectionTask<?>> tasks)
	{
		Phaser phaser = new Phaser(1 + tasks.size());
		
		for (CollectionTask<?> task : tasks)
		{
			executorService.submit(new PhasedTask<CollectionTask<?>>(phaser, task));
		}
		
		phaser.arriveAndAwaitAdvance();
	}
}
