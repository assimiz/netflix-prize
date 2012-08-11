package graphframework;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import dataframework.User;
import dbanalysis.Rent;

/**
 * Generates a trust graph from specified {@link Rent}s set. There will be a vertex for each user
 * appears in one rent or more. Edges will be added according to specified Policer. Edges are added
 * using brute-force method, i.e. the Policer is being queried for each pair of users (actually it is
 * queried twice for each pair of users since the resulted graph is directed) whether there
 * should or should not be and edge between them.
 * 
 * @author Assaf Mizrachi
 *
 */
public abstract class GeneralTrustGraphGenerator {
	
	private final static Rent POISON_PILL = new Rent(-1, -1, new Date(), -1);
	
	private Iterator<Rent> rents;		

	public GeneralTrustGraphGenerator (Iterator<Rent> rents) {
		this.rents = rents;		
	}
	public TrustGraph generateGraph() {
				
		final BlockingQueue<Rent> queue = new LinkedBlockingQueue<Rent>(10000);
		
		final TrustGraph graph = new TrustGraph();
		
		Thread rentsProducer = new Thread(new RentsProducer(queue, rents));
		
		RentsConsumer consumer = new RentsConsumer(queue, graph);
		Thread graphBuilder = new Thread(consumer);
		
		graphBuilder.start();
		rentsProducer.start();	
		
		try {
			synchronized (graphBuilder) {
				while (!consumer.consumedAll()) {
					graphBuilder.wait();
				}
			}			
		} catch (InterruptedException e) {
			System.out.println("Main thread interuppted. Probably graph is ready.");
		}
		return graph;
	}
	
	private static class RentsProducer implements Runnable {

		private BlockingQueue<Rent> queue;
		
		private Iterator<Rent> rents;
		
		public RentsProducer(BlockingQueue<Rent> queue, Iterator<Rent> rents) {
			this.queue = queue;
			this.rents = rents;
		}
		
		@Override
		public void run() {			
			try {
				int i = 1;
				while (rents.hasNext()) {
//					System.out.println("Producing rent: " + i);
					queue.put(rents.next());
					i++;
				}
				//putting the poison pill to kill consumer
				queue.put(POISON_PILL);
			} catch (InterruptedException e) {
				System.out.println("Rents iterator interrupted.\n");
				e.printStackTrace();
			}
		}
		
	}
	
	private class RentsConsumer implements Runnable {

		private BlockingQueue<Rent> queue;
		
		private TrustGraph graph;
		
		private boolean consumedAll = false;
		
		public RentsConsumer(BlockingQueue<Rent> queue, TrustGraph graph) {
			this.queue = queue;
			this.graph = graph;
		}
		
		@Override
		public void run() {
			try {
				Rent rent = queue.take();
				User user;
				int i = 1;
				int j = 1;
				while (rent != POISON_PILL) {
					System.out.println("Consuming rent: " + i + ". Rent = " + rent);
					user = new User(rent.getUserId());
					if (graph.addVertex(user)) {
						System.out.println("Adding user " + j);
						j++;
					}					
					rent = queue.take();
					i++;
				}
				
				addEdges(graph);
			} catch (InterruptedException e) {
				System.out.println("Rents iterator interrupted while trying to take from queue.");
				e.printStackTrace();
			}		
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			consumedAll = true;
			
			synchronized (this) {	
				System.out.println("Notifying all...");
				this.notifyAll();
			}			
		}
		
		public boolean consumedAll() {
			return consumedAll;
		}
	}
	
	protected abstract void addEdges(TrustGraph graph);
}
