package chapter5;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * 虽然运行这个程序时将抛出异常，但程序不会停止。但当任务抛出运行异常时，会影响它的父任务以及父任务的父任务，以此类推。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 下午02:47:40
 * @version 1.0
 */
public class Main_4 {
	
	private static class Task extends RecursiveTask<Integer> {

		private static final long serialVersionUID = 3486095793959894492L;
		private int array[];
		private int start, end;
		
		public Task(int array[],  int start, int end) {
			this.array = array;
			this.start = start;
			this.end = end;
		}
		
		@Override
		protected Integer compute() {
			System.out.printf("Task: Start from %d to %d\n", start, end);
			// 当把任务切分为 start = 0， end = 6时，将抛出异常，且将任务休眠1秒。
			if ((end - start) < 10) {
				if ((3 > start) && (3 < end)) {
					throw new RuntimeException("This task throws an Exception: Task from " + start + " to " + end);
				}
				
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) { e.printStackTrace(); }
			} else {
				int mid = (end + start) / 2;
				
				Task task1 = new Task(array, start, mid);
				Task task2 = new Task(array, mid, end);
				
				invokeAll(task1, task2);
			}
			
			System.out.printf("Task: End from %d to %d\n", start, end);
			
			return 0;
		}
	}
	
	public static void main(String[] args) {
		int array[] = new int[100];
		Task task = new Task(array, 0, array.length);
		
		ForkJoinPool pool = new ForkJoinPool();
		pool.execute(task);
		pool.shutdown();
		
		// 用1天的时间来等待任务结束。
		try {
			pool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) { e.printStackTrace(); }
		
		/*
		 * 可使用 isCompletedAbnormally() 方法来检查主任务或子任务是否抛出了异常，或已经被取消了。
		 * 且可以通过 getException() 方法来获取异常。
		 */
		if (task.isCompletedAbnormally()) {
			System.out.printf("Main: An exception has ocurred\n");
			System.out.printf("Main: %s\n", task.getException());
		}
		
		System.err.printf("Main: Result: %d", task.join());
	}
}