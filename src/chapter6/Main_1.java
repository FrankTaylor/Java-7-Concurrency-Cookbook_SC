package chapter6;

import java.util.concurrent.ConcurrentLinkedDeque;

public class Main_1 {
	
	/***
	 * 构造增加元素任务。
	 */
	public static class AddTask implements Runnable {
		
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
				list.pollFirst();
				list.pollLast();
			}
		}
		
	}
	
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