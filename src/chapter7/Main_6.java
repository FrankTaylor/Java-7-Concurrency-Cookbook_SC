package chapter7;

import java.util.Date;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Java 7 在 Fork/Join 框架中提供了一个特殊形式的执行器。这个框架旨在通过 “拆分” 和 “合并” 技术，把任务拆分为更小的任务。
 * 在一个任务中，我们需要检查待解决问题的规模，如果问题规模较大，就需要把问题拆分为若干个小规模任务，并使用 Fork/Join 框架执行
 * 这些任务。如果问题的规模已较小，就直接在任务里搞定问题即可。
 * 
 * 默认情况下，ForkJoinPool 类执行的任务是 ForkJoinTask 类的对象。虽然也可以传递 Runnable 和 Callable 对象到
 * ForkJoinPool 类中，但它们不会利用到 Fork/Join 框架的优势。一般来说，应将 ForkJoinTask 的两种子类传递给 ForkJoinPool 对象：
 * 1、RecursiveAction：用于任务不返回结果的情况。
 * 2、RecursiveTask：用于任务返回结果的情况。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_6 {
	
	private static abstract class MyWorkerTask extends ForkJoinTask<Void> {
		
		private String name;
		
		public MyWorkerTask(String name) {
			this.name = name;
		}
		
		protected  abstract void compute();
		
		@Override
		public Void getRawResult() {
			return null;
		}
		
		@Override
		protected void setRawResult(Void value) {}
		
		@Override
		protected boolean exec() {
			Date startDate = new Date();
			
			compute();
			
			Date finishDate = new Date();
			long diff = finishDate.getTime() - startDate.getTime();
			
			System.out.printf("MyWorkerTask: %s : %d Milliseconds to complete.\n", name, diff);
			return true;
		}
		
		public String getName() {
			return name;
		}
	}
	
	private static class Task extends MyWorkerTask {
		
		private int[] array;
		private int start;
		private int end;
		
		public Task(String name, int[] array, int start, int end) {
			super(name);
			this.array = array;
			this.start = start;
			this.end = end;
		}
		
		@Override
		protected void compute() {
			if ((end - start) > 100) {
				int mid = (end + start) / 2;
				Task task1 = new Task(this.getName() + "1", array, start, mid);
				Task task2 = new Task(this.getName() + "2", array, mid, end);
				invokeAll(task1, task2);
			} else {
				for (int i = start; i < end; i++) {
					array[i]++;
				}
				
				try {
					TimeUnit.MICROSECONDS.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		
		int[] array = new int[10000];
		ForkJoinPool pool = new ForkJoinPool();
		Task task = new Task("Task", array, 0, array.length);
		
		pool.invoke(task);
		pool.shutdown();
		System.out.printf("Main: End of the program.\n");
	}
}