package net.electra.tasks;

import java.util.ArrayList;
import java.util.List;

public class CollectionTaskDistributor
{
	public static <T> List<CollectionTask<T>> distribute(List<T> collection, int maxTaskCount, CollectionTaskProcessor<T> processor)
	{
		List<CollectionTask<T>> tasks = new ArrayList<CollectionTask<T>>();
		int maxPerTask = collection.size() / maxTaskCount;
		int amountOfTasks = (collection.size() / maxPerTask) + 1;
		
		if (amountOfTasks > maxTaskCount)
		{
			amountOfTasks = maxTaskCount;
		}
		
		int amountPerTask = collection.size() / amountOfTasks;
		int leftOver = collection.size() % amountOfTasks;
		int carried = 0;
		
		for (int i = 0; i < amountOfTasks; i++)
		{
			int carry = (leftOver > 0 ? 1 : 0);
			leftOver -= carry;
			int start = (i * amountPerTask) + carried;
			int end = start + amountPerTask + carry;
			carried += carry;
			tasks.add(new CollectionTask<T>(collection.subList(start, end), processor));
		}
		
		return tasks;
	}
}
