package chapter7;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Fork/Join 框架用于执行可拆分成更小任务的任务，它的主要组件如下：
 * 1、执行由 ForkJoinTask 类实现的任务。
 * 2、通过 fork 操作将一个任务拆分成子任务，通过 join 操作等待这些子任务结束。
 * 3、使用 “工作窃取算法” 对线程池的使用进行优化。当一个任务等待它的子任务时，执行这个任务的线程可以被用来执行其他任务。
 * 
 * Fork/Join 框架的主类是 ForkJoinPool 类。从内部实现来说，它有下面两个元素：
 * 1、一个用于保存 “待执行任务” 的队列；
 * 2、一个用于执行这些任务的线程池。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_5 {
	
	private static class MyWorkerThread extends ForkJoinWorkerThread {
		
		private static ThreadLocal<Integer> taskCounter = new ThreadLocal<Integer>();
		
		protected MyWorkerThread(ForkJoinPool pool) {
			super(pool);
		}
		
		@Override
		protected void onStart() {
			super.onStart();
			System.out.printf("MyWorkThread %d: Initializing task counter.\n", getId());
			taskCounter.set(0);
		}
		
		@Override
		protected void onTermination(Throwable exception) {
			System.out.printf("MyWorkThread %d: %d\n", getId(), taskCounter.get());
		}
		
		public void addTask() {
			int counter = taskCounter.get().intValue();
			counter++;
			taskCounter.set(counter);
		}
	}
	
	private static class MyWorkerThreadFactory implements ForkJoinWorkerThreadFactory {
		
		@Override
		public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
			return new MyWorkerThread(pool);
		}
	}
	
	private static class MyRecursiveTask extends RecursiveTask<Integer> {
		
		private int[] array;
		private int start, end;
		
		public MyRecursiveTask(int[] array, int start, int end) {
			this.array = array;
			this.start = start;
			this.end = end;
		}
		
		private Integer addResults(MyRecursiveTask task1, MyRecursiveTask task2) {
			int value;
			try {
				value = task1.get().intValue() + task2.get().intValue();
			} catch (InterruptedException e) {
				e.printStackTrace();
				value = 0;
			} catch (ExecutionException e) {
				e.printStackTrace();
				value = 0;
			}
			
			try {
				TimeUnit.MICROSECONDS.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return value;
		}
		
		@Override
		protected Integer compute() {
			Integer ret = 0;
			MyWorkerThread thread = (MyWorkerThread)Thread.currentThread();
			thread.addTask();
			for (int i = start; i < end; i++) {
				ret += array[i];
			}
			return ret;
		}
	}
	
	/*
	 * 工作原理：
	 * 
	 * ForkJoinWorkerThread 继承了 Thread 类，并实现了可在 Fork/Join 框架里使用的工作线程。本例中，MyWorkerThread 继承了
	 * ForkJoinWorkerThread 类，并覆盖了 onStart() 和 onTermination() 两个方法。
	 * 1、onStart()：当工作线程开始执行时，该方法会被自动调用。
	 * 2、onTermination()：当工作线程执行完成时，这个方法会被自动调用。
	 * 
	 * 在 MyWorkerThread 类的 addTask() 方法中，使用了 ThreadLocal 来统计每一工作线程执行了多少任务。
	 * 
	 * 与 Java 并发 API 中的所有执行器一样，ForkJoinPool 类也使用了 “工厂模式” 来创建它的线程，所以如果想在 ForkJoinPool 类中使用
	 * 自定义的 MyWorkThread 线程，就必须实现自己的线程工厂。
	 * 
	 * 在 Fork/Join 框架中，这个工厂必须实现 FrokJoinPool.ForkJoinWorkerThreadFactory 接口。
	 * 
	 * 更多信息：
	 * 
	 * 当一个线程正常结束或抛出 Exception 时，ForkJoinWorkerThread 类提供的 onTermination() 方法都会被自动调用。这个方法接收
	 * 一个 Throwable 对象作为参数。如果为 null，说明工作线程正常结束；否则说明线程抛出了异常。
	 *  
	 */
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		
		MyWorkerThreadFactory factory = new MyWorkerThreadFactory();
		ForkJoinPool pool = new ForkJoinPool(4, factory, null, false);
		
		int[] array = new int[100000];
		for (int i = 0; i < array.length; i++) {
			array[i] = 1;
		}
		MyRecursiveTask task = new MyRecursiveTask(array, 0, array.length);
		
		// 使用 execute() 方法将求和任务发送到 pool。
		pool.execute(task);
		
		// 使用 join() 方法等待任务结束。
		task.join();
		
		// 使用 shutdown() 方法关闭 pool 对象。
		pool.shutdown();
		// 使用 awaitTermination() 方法等待执行器结束。
		pool.awaitTermination(1, TimeUnit.DAYS);
		
		System.out.printf("Main: Result: %d\n", task.get());
		System.out.printf("Main: End of the program\n");
	}
}