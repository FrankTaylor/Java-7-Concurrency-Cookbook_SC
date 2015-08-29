package chapter4;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main_3 {

	private static class Task implements Runnable {
		
		private Date initDate;
		private String name;
		
		public Task(Date initDate, String name) {
			this.initDate = initDate;
			this.name = name;
		}

		@Override
		public void run() {
		    System.out.printf("%s: Task %s: Create on: %s \n", Thread.currentThread().getName(), name, initDate);
		    System.out.printf("%s: Task %s: Started on: %s \n", Thread.currentThread().getName(), name, new Date());

            try {
                Long duration = (long)(Math.random() * 10);
                System.out.printf("%s: Task %s: Doing a task during %d seconds\n", Thread.currentThread().getName(), name, duration);
                TimeUnit.SECONDS.sleep(duration);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.printf("%s: Task %s: Finished on: %s \n", Thread.currentThread().getName(), name, new Date());
		}
	}

    
    public static void main(String[] args) {

    	/*
    	 * 创建只有一个线程的线程执行器。
    	 * 
    	 * 使用  newSingleThreadExecutor() 方法将创建一个只有1个线程的执行器，该执行器只能在同一时间执行一个任务。
    	 * 
    	 * 注意：用 newSingleThreadExecutor() 方法创建的执行器，不能在强转型为 ThreadPoolExecutor。它的源码为：
    	 * 
    	 * public static ExecutorService newSingleThreadExecutor() {
    	 *     return new FinalizableDelegatedExecutorService 
    	 *         (new ThreadPoolExecutor(1, 1, 0L, 
    	 *             TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()));
    	 *     }
    	 */
    	ExecutorService executor = Executors.newSingleThreadExecutor();
    	
        for (int i = 0; i < 100; i++) {
            executor.execute(new Task(new Date(), "Task " + i));
        }

        executor.shutdown();

        try {
			executor.awaitTermination(100, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("执行已完成");
    }
}