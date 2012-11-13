package net.electra.services.ondemand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import net.electra.Processable;
import net.electra.net.Client;
import net.electra.net.ClientAdapter;
import net.electra.services.Servicable;
import net.electra.services.ondemand.events.OnDemandProcessEvent;

public class OnDemandClient extends Servicable<OnDemandService> implements ClientAdapter, Processable
{
	private final LinkedList<OnDemandRequest> requests = new LinkedList<OnDemandRequest>();
	private final Client client;
	
	public OnDemandClient(Client client, OnDemandService service)
	{
		super(service);
		this.client = client;
		client.associate(this);
	}
	
	@Override
	public void process()
	{
		ArrayList<OnDemandRequest> completed = new ArrayList<OnDemandRequest>();
		
		for (OnDemandRequest request : requests)
		{
			/*System.out.println("processing " + request.file().descriptor().index().id() + " - " + request.file().descriptor().id()
					+ ", b: " + request.currentBlock() + "/" + request.totalBlocks() + ", p: " + request.priority() + ", s: " + request.file().buffer().length());*/
			fire(new OnDemandProcessEvent(request));
			
			if (request.currentBlock() >= request.totalBlocks() && !request.file().buffer().hasRemaining())
			{
				completed.add(request);
			}
		}
		
		for (OnDemandRequest complete : completed)
		{
			remove(complete);
		}
	}
	
	public void submit(OnDemandRequest request)
	{
		if (!requests.contains(request))
		{
			requests.add(request);
			Collections.sort(requests);
		}
	}
	
	public void remove(OnDemandRequest request)
	{
		requests.remove(request);
	}

	@Override
	public Client client()
	{
		return client;
	}
}
