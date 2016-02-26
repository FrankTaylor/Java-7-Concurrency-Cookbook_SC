package chapter6;

import java.util.concurrent.ConcurrentLinkedDeque;

public class Main_1 {
	
	/***
	 * 构造增加元素任务。
	 */
	public static class AddTask implements Runnable {
		
		// 非阻塞并发队列。
		private ConcurrentLinkedDeque<String> list;
		
		public AddTask(ConcurrentLinkedDeque<String> list) {
			this.list = list;
		}
		
		@Override
		public void run() {
			String name = Thread.currentThread().getName();
			for (int i = 0; i < 10000; i++) {
				list.add(name + ": Element " + i);
			}
		}
		
	}
	
	/**
	 * 构造取出元素任务。
	 */
	public static class PollTask implements Runnable {
		
		private ConcurrentLinkedDeque<String> list;
		
		public PollTask(ConcurrentLinkedDeque<String> list) {
			this.list = list;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < 5000; i++) {
				
				/*
				 * ConcurrentLinkedDeque#pollFirst()：移除队列中的第一个元素。
				 * ConcurrentLinkedDeque#pollLast()：移除队列中的最后一个元素。
				 * 
				 * 如果队列为空，这些方法将返回 null。
				 */
				list.pollFirst();
				list.pollLast();
			}
		}
		
	}
	
	/*
	 * 更多信息：
	 * getFirst() 和 getLast()：分别返回队列中第一个和最后一个元素，返回的元素不会从队列中移除。如果队列为空，
	 * 这两个方法抛出 NoSuchElemenetException 异常。
	 * 
	 * peek()、peekFirst() 和 peekLast()：分别返回队列中第一个和最后一个元素，返回的元素不会从队列中移除。
	 * 如果队列为空，这些方法返回 null。
	 * 
	 * remove()、removeFirst() 和 removeLast()：分别返回队列中第一个和最后一个元素，返回的元素将会从
	 * 队列中移除。如果队列为空，这些方法抛出 NoSuchElementException 异常。
	 */
	public static void main(String[] args) {
		ConcurrentLinkedDeque<String> list = new ConcurrentLinkedDeque<String>();
		
		Thread[] threads = new Thread[100];
		
		// 创建 100 个向集合中添加元素的任务，并用线程进行驱动。
		for (int i = 0; i < threads.length; i++) {
			AddTask task = new AddTask(list);
			threads[i] = new Thread(task);
			threads[i].start();
		}
		
		System.out.printf("Main: %d AddTask threads have been launched\n", threads.length);
		
		// 使用 join() 方法等待驱动添加元素任务的线程完成。
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * ConcurrentLinkedDeque#size()：输出队列中元素的数量。需要注意的是，这个方法返回的值可能不是真实的，
		 * 尤其当有线程在添加数据或移除数据时，这个方法需要遍历整个队列来计算元素数量，而遍历过的数据可能已经改变。
		 * 
		 * 仅当没有任何线程修改队列时，才能保证返回的结果是准确的。
		 */
		System.out.printf("Main: Size of the List: %d\n", list.size());
		
		// 创建 100 个向集合中取出元素的任务，并用线程进行驱动。
		for (int i = 0; i < threads.length; i++) {
			PollTask task = new PollTask(list);
			threads[i] = new Thread(task);
			threads[i].start();
		}
		
		System.out.printf("Main: %d PollTask threads have been launched\n", threads.length);
		
		// 使用 join() 方法等待驱动取出元素任务的线程完成。
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.printf("Main: Size of the List: %d\n", list.size());
	}
}