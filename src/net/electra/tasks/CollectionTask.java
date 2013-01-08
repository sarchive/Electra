package net.electra.tasks;

import java.util.List;

public class CollectionTask<T> extends Task
{
	protected final CollectionTaskProcessor<T> processor;
	protected final List<T> collection;
	
	public CollectionTask(List<T> collection, CollectionTaskProcessor<T> processor)
	{
		this.collection = collection;
		this.processor = processor;
	}
	
	@Override
	public void run()
	{
		for (T element : collection)
		{
			processor.process(element);
		}
	}
}
