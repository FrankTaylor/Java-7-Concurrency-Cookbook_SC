package chapter4;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main_8 {
	
	private static class Task implements Runnable {
		
		private String name;
		
		public Task (String name) {
			this.name = name;
		}
		
		@Override
		public void run() {
			System.out.printf("%s: Starting at: %s\n", this.name, new Date());
			
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.printf("%s: Ending at: %s\n", this.name, new Date());
		}	
	}
	
	public static void main(String[] args) {
		
		/*
		 * scheduleAtFixedRate() 方法有四个参数：
		 * 1、将被周期执行的任务；
		 * 2、第一次执行任务的延期时间；
		 * 3、从一个任务开始到下一个任务开始的间隔时间（在实际中，如果上一任务的执行时长 > 间隔时间，那么即使到了该间隔时间，任务也不会执行，还是会等上一个任务执行完毕后才执行）
		 * 4、时间单位。
		 * 
		 * scheduleWithFixedRate() 方法与 scheduleAtFixedRate() 类似，也有四个参数，
		 * 但第 3 个参数表示，任务上一次执行结束的时间与任务下一次开始执行的时间的间隔。
		 * 
		 * 注意：
		 * 1、scheduleAtFixedRate() 该方法是异步方法，不会阻塞主线程的运行。
		 * 2、在调用 scheduleAtFixedRate() 方法后，一旦调用了 shutdown() 方法，定时任务就结束了。
		 * 可以通过 ScheduledThreadPoolExecutor 类的 setContinueExistingPeriodicTasksAfterShutdownPolicy(true)方法来改变这个行为。
		 * 
		 * 
		 */
		{
			System.out.println("开始执行第一种情况：1线程，2任务");
			
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
			String taskName_1 = "Task 1";
			String taskName_2 = "Task 2";
			
			executor.scheduleAtFixedRate(new Task(taskName_1), 1, 2, TimeUnit.SECONDS);
			executor.scheduleAtFixedRate(new Task(taskName_2), 1, 2, TimeUnit.SECONDS);
			
			/*
			 * 开始执行第一种情况：1线程，2任务
			 * Task 1: Starting at: Sat Aug 08 15:16:20 CST 2015
			 * Task 1: Ending at: Sat Aug 08 15:16:25 CST 2015
			 * Task 2: Starting at: Sat Aug 08 15:16:25 CST 2015
			 * Task 2: Ending at: Sat Aug 08 15:16:30 CST 2015
			 * Task 1: Starting at: Sat Aug 08 15:16:30 CST 2015
			 * Task 1: Ending at: Sat Aug 08 15:16:35 CST 2015
			 * Task 2: Starting at: Sat Aug 08 15:16:35 CST 2015
			 * Task 2: Ending at: Sat Aug 08 15:16:40 CST 2015
			 * ……
			 * 
			 * 与 schedule() 方法差不多。
			 */
		}
		
		{
			System.out.println("开始执行第而种情况：2线程，2任务");
			
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
			String taskName_1 = "Task 1";
			String taskName_2 = "Task 2";
			
			executor.scheduleAtFixedRate(new Task(taskName_1), 1, 2, TimeUnit.SECONDS);
			executor.scheduleAtFixedRate(new Task(taskName_2), 1, 2, TimeUnit.SECONDS);
			
			/*
			 * 开始执行第而种情况：2线程，2任务
			 * Task 1: Starting at: Sat Aug 08 15:18:53 CST 2015
			 * Task 2: Starting at: Sat Aug 08 15:18:53 CST 2015
			 * Task 1: Ending at: Sat Aug 08 15:18:58 CST 2015
			 * Task 1: Starting at: Sat Aug 08 15:18:58 CST 2015
			 * Task 2: Ending at: Sat Aug 08 15:18:58 CST 2015
			 * Task 2: Starting at: Sat Aug 08 15:18:58 CST 2015
			 * Task 1: Ending at: Sat Aug 08 15:19:03 CST 2015
			 * Task 1: Starting at: Sat Aug 08 15:19:03 CST 2015
			 * Task 2: Ending at: Sat Aug 08 15:19:03 CST 2015
			 * Task 2: Starting at: Sat Aug 08 15:19:03 CST 2015
			 * ……
			 * 
			 * 与 schedule() 方法差不多。
			 */
		}
				
		{
			System.out.println("开始执行第而种情况：2线程，1任务");
			
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
			String taskName_1 = "Task 1";
			
			executor.scheduleAtFixedRate(new Task(taskName_1), 1, 2, TimeUnit.SECONDS);
			
			/*
			 * 开始执行第而种情况：2线程，1任务
			 * Task 1: Starting at: Sat Aug 08 15:20:35 CST 2015
			 * Task 1: Ending at: Sat Aug 08 15:20:40 CST 2015
			 * Task 1: Starting at: Sat Aug 08 15:20:40 CST 2015
			 * Task 1: Ending at: Sat Aug 08 15:20:45 CST 2015
			 * Task 1: Starting at: Sat Aug 08 15:20:45 CST 2015
			 * Task 1: Ending at: Sat Aug 08 15:20:50 CST 2015
			 * Task 1: Starting at: Sat Aug 08 15:20:50 CST 2015
			 * Task 1: Ending at: Sat Aug 08 15:20:55 CST 2015
			 * Task 1: Starting at: Sat Aug 08 15:20:55 CST 2015
			 * ……
			 */
		}
		
		{
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
			Task task = new Task("Task");
			ScheduledFuture<?> result = executor.scheduleAtFixedRate(task, 1, 2, TimeUnit.SECONDS);
			
			for (int i = 0; i < 10; i++) {
				// 调用 getDelay() 方法来获取任务下一次将要执行的毫秒数。
				System.out.printf("Main: Delay: %d\n", result.getDelay(TimeUnit.MILLISECONDS));
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			executor.shutdown();
			
			// 将线程休眠5秒，等待周期性的任务全部执行完成。
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.printf("Main: Finished at: %s\n", new Date());
		}
	}
}