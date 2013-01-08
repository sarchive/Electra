package net.electra.tasks;

import java.util.concurrent.Phaser;

public class PhasedTask<T extends Task> extends Task
{
	private final Runnable task;
	private final Phaser phaser;
	
	public PhasedTask(Phaser phaser, Runnable task)
	{
		this.task = task;
		this.phaser = phaser;
	}
	
	@Override
	public void run()
	{
		task.run();
		phaser.arriveAndDeregister();
	}
}
