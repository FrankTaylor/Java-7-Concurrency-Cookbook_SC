package chapter4;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main_7 {
	
	private static class Task implements Callable<String> {
		
		private String name;
		
		public Task (String name) {
			this.name = name;
		}
		
		@Override
		public String call() throws Exception {
			System.out.printf("%s: Starting at: %s\n", this.name, new Date());
			
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.printf("%s: Ending at: %s\n", this.name, new Date());
			return "Hello World";
		}
		
	}
	
	public static void main(String[] args) {
		
		/*
		 * 虽然可以通过 ThreadPoolExecutor 类来创建定时执行器，但是在 Java 并发 API 中则推荐利用 Executors 工厂类来创建。
		 * 
		 * 虽然 ScheduledThreadPoolExecutor 是 ThreadPoolExecutor 的子类，因而继承了 ThreadPoolExecutor 类所有的特性。
		 * 但是，推荐仅在开发定时任务程序时采用 ScheduledThreadPoolExecutor 类。
		 * 
		 * 注意：在执行 shutdown() 方法后，调用 setExecuteExistingDelayedTasksAfterShutdownPolicy(false) 方法，待处理的任务将不会被执行。
		 */
		
		/*
		 * 该 schedule() 方法有三个参数：
		 * 1、即将执行的任务；
		 * 2、第一次执行任务的延期时间；
		 * 3、时间单位。
		 * 
		 * 注意：schedule() 该方法是异步方法，不会阻塞主线程的运行，但只要定时执行一次后任务就结束了，而不是一直循环执行。
		 * 
		 */
		
		{
			System.out.println("开始执行第一种情况：1线程，2任务");
			ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);
			
			String taskName_1 = "Task 1";
			String taskName_2 = "Task 2";
			
			executor.schedule(new Task(taskName_1), 1, TimeUnit.SECONDS);
			executor.schedule(new Task(taskName_2), 2, TimeUnit.SECONDS);
			
			executor.shutdown();
			
			try {
				executor.awaitTermination(1, TimeUnit.DAYS);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
			
			/*
			 * 开始执行第一种情况：1线程，2任务
			 * Task 1: Starting at: Sat Aug 08 15:05:17 CST 2015
			 * Task 1: Ending at: Sat Aug 08 15:05:22 CST 2015
			 * Task 2: Starting at: Sat Aug 08 15:05:22 CST 2015
			 * Task 2: Ending at: Sat Aug 08 15:05:27 CST 2015
			 * Main: Ends at: Sat Aug 08 15:05:27 CST 2015
			 *  从结果上看有以下几点启发：
			 * 1、从接收任务的时间上看 schedule() 方法的确是异步的；
			 * 2、假设任务执行要花费 5 秒，那么  “第一个任务的执行开始时间为 = 接收任务的时间 + delay”，“第一个任务的执行结束时间为 = 任务开始时间 + 5秒”，
			 * 从接收第一个任务到其执行结束总共要花费6秒，而第二个任务早已经过了 “delay（这里是2秒）”，从输出上看，第二个任务却是在 6 秒后执行的。这就是说，
			 * 在 “schedule()方法中，下一个任务的执行时间 = 接收该任务的时间 + (上一任务耗时 >= delay) ? 上一任务耗时 : (delay - 上一任务耗时 )” 。
			 */
		}
		
		{
			System.out.println("开始执行第二种情况：2线程，2任务");
			ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(2);
			
			String taskName_1 = "Task 1";
			String taskName_2 = "Task 2";
			
			executor.schedule(new Task(taskName_1), 1, TimeUnit.SECONDS);
			executor.schedule(new Task(taskName_2), 2, TimeUnit.SECONDS);
			
			executor.shutdown();
			
			try {
				executor.awaitTermination(1, TimeUnit.DAYS);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
			
			/*
			 * 开始执行第二种情况：2线程，2任务
			 * Task 1: Starting at: Sat Aug 08 15:07:42 CST 2015
			 * Task 2: Starting at: Sat Aug 08 15:07:43 CST 2015
			 * Task 1: Ending at: Sat Aug 08 15:07:47 CST 2015
			 * Task 2: Ending at: Sat Aug 08 15:07:48 CST 2015
			 * Main: Ends at: Sat Aug 08 15:07:48 CST 2015
			 * 
			 */
		}
		
		{
			System.out.println("开始执行第三种情况：2线程，1任务");
			ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(2);
			
			String taskName_1 = "Task 1";
			
			executor.schedule(new Task(taskName_1), 1, TimeUnit.SECONDS);
			
			executor.shutdown();
			
			try {
				executor.awaitTermination(1, TimeUnit.DAYS);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
			
			/*
			 * 开始执行第三种情况：2线程，1任务
			 * Task 1: Starting at: Sat Aug 08 15:10:01 CST 2015
			 * Task 1: Ending at: Sat Aug 08 15:10:06 CST 2015
			 * Main: Ends at: Sat Aug 08 15:10:06 CST 2015
			 * 
			 */
		}
		
		System.out.printf("Main: Ends at: %s\n", new Date());
	}
}