package chapter7;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 在使用 Executor 框架时，只需实现任务，并将它们提交到执行器中，然后执行器将负责在池中重用或创建线程，并驱动这些线程执行任务。
 * 
 *  执行器内部使用一个 “阻塞式队列” 来存放等待执行的任务，并按任务到达执行器时的顺序进行存放。另一个可行的替代方案是：使用优先级
 *  队列存放任务，这样如果有高优先级的新任务到达执行器，它将优先被执行。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_2 {
	
	private static class MyPriorityTask implements Runnable, Comparable<MyPriorityTask> {
		
		private int priority;
		private String name;
		
		public MyPriorityTask(int priority, String name) {
			this.priority = priority;
			this.name = name;
		}
		
		public int getPriority() {
			return priority;
		}
		
		@Override
		public int compareTo(MyPriorityTask o) {
			if (getPriority() < o.getPriority()) {
				return 1;
			}
			if (getPriority() > o.getPriority()) {
				return -1;
			}
			return 0;
		}
		
		@Override
		public void run() {
			System.out.printf("MyPriorityTask: %s Priority: %d\n", name, priority);
			
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * 工作原理：
	 * 
	 * MyPriorityTask 实现了 Runnable 接口，成为可通过线程驱动的任务；又实现 Comparable 接口，成为可存放在优先队列中的元素。
	 * 被覆盖实现的 compareTo() 方法根据类中成员 priority 的大小来决定元素在队列中的顺序（在此列中，priority 越大优先级越高，
	 * 其实队列是根据 compareTo() 方法值从小到大排序的，程序员可以通过控制其比较的返回值来定义顺序）。
	 */
	public static void main(String[] args) {
		
		ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 1, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
		
		for (int i = 0; i < 4; i++) {
			MyPriorityTask task = new MyPriorityTask(i, "Task " + i);
			executor.execute(task);
		}
		
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (int i = 4; i < 8; i++) {
			MyPriorityTask task = new MyPriorityTask(i, "Task " + i);
			executor.execute(task);
		}
		
		executor.shutdown();
		
		// 使用 awaitTermination() 方法等待执行器的完成。
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.printf("Main: End of the program.\n");
	}
}