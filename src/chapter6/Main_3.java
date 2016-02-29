package chapter6;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * PriorityBlockingQueue 是按优先级排序的阻塞并发队列。所有添加进 PriorityBlockingQueue 的元素
 * 都必须实现 Comparable 接口。该接口提供了 compareTo() 方法，入参是同一类型的对象，该方法会返回一个整
 * 型数字，如果当前对象 < 参数传入的对象时，将返回小于 0 的值；如果当前对象 > 参数传入的对象时，返回大于 0 的值，
 * 如果两个对象相等，则返回 0 。
 * 
 * PriorityBlockingQueue 使用 compareTo() 方法来决定插入元素的位置，从小到大一次排列。
 * 同时，PriorityBlockingQueue 是阻塞式的数据结构。当它的方法被调用，且不能立即执行时，那调用这个方法的
 * 线程将被阻塞，直到方法执行成功。
 * 
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_3 {
	
	private static class Event implements Comparable<Event> {
		
		/** 存放创建了 event 对象的线程数。*/
		private int thread;
		/** 存放该对象的优先级。*/
		private int priority;
		
		public Event(int thread, int priority) {
			this.thread = thread;
			this.priority = priority;
		}
		
		@Override
		public int compareTo(Event e) {
			if (priority > e.getPriority()) {
				return -1;
			} else if (priority < e.getPriority()) {
				return 1;
			} else {				
				return 0;
			}
		}
		
		// --- get method ---
		
		public int getThread() {
			return thread;
		}
		
		public int getPriority() {
			return priority;
		}
	}
	
	/*
	 * 更多信息：
	 * 
	 * clear()：移除队列中的所有元素。
	 * take()：返回队列中的第一个元素并将其移除。如果队列为空，线程阻塞直到队列中有可用的元素。
	 * put(E e)：E 是 PriorityBlockingQueue 的泛型参数，表示传入参数的类型。这个方法把参数对应的元素插入到队列中。
	 * peek()：返回队列中的第一个元素，但不将其移除。
	 * poll()：返回队列中的第一个元素，并将其删除。如果队列为空，则返回 null。
	 */
	private static class Task implements Runnable {
		
		/** 用来存放任务编号。*/
		private int id;
		/** 声明优先级阻塞队列。*/
		private PriorityBlockingQueue<Event> queue;
		
		public Task(int id, PriorityBlockingQueue<Event> queue) {
			this.id = id;
			this.queue = queue;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < 1000; i++) {
				Event event = new Event(id, i);
				queue.add(event);
			}
		}
	}
	
	public static void main(String[] args) {
		
		PriorityBlockingQueue<Event> queue = new PriorityBlockingQueue<Event>();
		
		Thread[] taskThreads = new Thread[5];
		
		// 初始这 5 个任务线程。
		for (int i = 0; i < taskThreads.length; i++) {
			Task task = new Task(i, queue);
			taskThreads[i] = new Thread(task);
		}
		
		// 启动这 5 个任务线程。
		for (Thread t : taskThreads) {
			t.start();
		}
		
		// 使用 join() 方法，让 main 线程等待 5 个任务线程结束。
		for (Thread t : taskThreads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.printf("Main: Queue Size: %d\n", queue.size());
		
		for (int i = 0; i < taskThreads.length * 1000; i++) {
			Event event = queue.poll();
			System.out.printf("Thread %s: Priority %d\n", event.getThread(), event.getPriority());
		}
		
		System.out.printf("Main: Queue Size: %d\n", queue.size());
		System.out.printf("Main: End of the program\n");
	}
}