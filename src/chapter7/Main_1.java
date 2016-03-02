package chapter7;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Executor 框架采用将线程的 “创建” 和 “执行” 分离的机制。它基于 Executor 和 ExecutorService 接口，
 * 以及这两个接口的实现类 ThreadPoolExecutor 展开。
 * 
 * Executor 内部使用线程池技术，用来执行提交到执行器的任务，可以传递的任务有如下两种：
 * 1、通过 Runnable 接口实现的任务，该任务不返回结果；
 * 2、通过 Callable 接口实现的任务，该任务返回结果。
 * 只需传递这两种任务到执行器的队列中，执行器便可使用池中的线程或新创建的线程来执行任务。同时，执行器也决定了任务的执行时间。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_1 {
	
	private static class MyExecutor extends ThreadPoolExecutor {
		
		private ConcurrentHashMap<String, Date> startTimes;
		
		public MyExecutor(
				int corePoolSize, int maximumPoolSize, long keepAliveTime, 
				TimeUnit unit, BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
			startTimes = new ConcurrentHashMap<String, Date>();
		}
		
		@Override
		public void shutdown() {
			System.out.printf("MyExecutor: Going to shutdown.\n");
			System.out.printf("MyExecutor: Executed tasks: %d\n", super.getCompletedTaskCount());
			System.out.printf("MyExecutor: Running tasks: %d\n", super.getActiveCount());
			System.out.printf("MyExecutor: Pending tasks: %d\n", super.getQueue().size());
			
			super.shutdown();
		}
		
		@Override
		public List<Runnable> shutdownNow() {
			System.out.printf("MyExecutor: Going to immediately shutdown.\n");
			System.out.printf("MyExecutor: Executed tasks: %d\n", super.getCompletedTaskCount());
			System.out.printf("MyExecutor: Running tasks: %d\n", super.getActiveCount());
			System.out.printf("MyExecutor: Pending tasks: %d\n", super.getQueue().size());
			
			return super.shutdownNow();
		}
		
		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			System.out.printf("MyExecutor: A task is beginning: %s : %s\n", t.getName(), r.hashCode());
			startTimes.put(String.valueOf(r.hashCode()), new Date());
		}
		
		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			Future<?> result = (Future<?>)r;
			try {
				System.out.printf("***********************\n");
				System.out.printf("MyExecutor: A task is finishing.\n");
				System.out.printf("MyExecutor: Result: %s\n", result.get());
				
				Date startDate = startTimes.remove(String.valueOf(r.hashCode()));
				Date finishDate = new Date();
				long diff = finishDate.getTime() - startDate.getTime();
				
				System.out.printf("MyExecutor: Duration: %d\n", diff);
				System.out.printf("***********************\n");
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class SleepTwoSecondsTask implements Callable<String> {
		
		@Override
		public String call() throws Exception {
			TimeUnit.SECONDS.sleep(2);
			return new Date().toString();
		}
	}
	
	/*
	 * 工作原理：
	 * 
	 * MyExecutor 继承了 ThreadPoolExecutor 并覆盖了父类中的 4 个方法：
	 * 1、beforeExecute() 方法将在任务开始前执行。在本例中用于记录任务 “开始时间”。
	 * 2、afterExecute() 方法将在任务结束后执行。在本例中用于计算任务 “结束时间” 和 “间隔时间”，并打印输出。
	 * 3、shutdown()
	 * 4、shutdownNow()
	 * 
	 * 注意：beforeExecute() 和 afterExecute() 在 ThreadPoolExecutor 中没有实现。
	 * 
	 * 在 shutdown() 和 shutdownNow() 方法中，将统计执行器执行的任务统计信息，并输出到控制台：
	 * 1、getTaskCount() 得到执行器接收的任务总数。
	 * 2、getCompletedTaskCount() 得到执行器已完成的任务数量。
	 * 3、getActiveCount() 得到执行器正在执行的任务数量。
	 * 4、getQueue().size() 得到执行器队列中等待执行的任务数量。
	 * 
	 * 在此例中，对于等待执行的任务，执行器将它们存放在我们自定义的 LinkedBlockingDeque 阻塞队列中。
	 */
	public static void main(String[] args) {
		
		MyExecutor myExecutor = new MyExecutor(2, 4, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
		
		// 用于存放即将传递给执行器的任务的返回结果。
		List<Future<String>> results = new ArrayList<Future<String>>();
		
		for (int i = 0; i < 10; i++) {
			SleepTwoSecondsTask task = new SleepTwoSecondsTask();
			Future<String> result = myExecutor.submit(task);
			results.add(result);
		}
		
		// 得到前 5 个任务的执行结果，并输出到控制台。
		try {
			for (int i = 0; i < 5; i++) {
				String result = results.get(i).get();
				System.out.printf("Main: Result for Task %d : %s\n", i, result);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		myExecutor.shutdown();
		
		// 得到后 5 个任务的执行结果，并输出到控制台。
		try {
			for (int i = 5; i < 10; i++) {
				String result = results.get(i).get();
				System.out.printf("Main: Result for Task %d : %s\n", i, result);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		// 使用 awaitTermination() 方法等待执行器的完成。
		try {
			myExecutor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.printf("Main: End of the program.\n");
	}
}