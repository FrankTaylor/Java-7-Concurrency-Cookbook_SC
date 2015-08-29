package chapter4;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main_2 {

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
    	 * 创建具有固定线程数量的线程执行器。
    	 * 
    	 * 使用  newFixedThreadPool(5) 可以创建只有5个工作线程的执行器。如果发送给该执行器的任务数超过“池中可用工作线程的数量”时，该
    	 * 执行器不会因此而再创建额外的工作线程，剩下的待执行任务将被阻塞直到执行器有空闲的线程来处理它们。
    	 * 
    	 * 这个特性避免了，使用 newCachedThreadPool() 创建线程执行器的问题。
    	 */
    	ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(5);
    	
        for (int i = 0; i < 100; i++) {

        	System.out.printf("\n------------ cycle %d start ---------------\n", i);
        	
        	System.out.printf("Server: A new task has arrived\n");
        	
        	/*
        	 * 把任务发给执行器。
        	 * 注意：该方法是异步的，调用后不会阻塞主线程。
        	 */
            executor.execute(new Task(new Date(), "Task " + i));
            
            // 返回执行器线程池中实际的线程数。
            System.out.printf("Server: Pool Size: %d\n", executor.getPoolSize());
            
            // 返回执行器中正在执行任务的线程数。
            System.out.printf("Server: Active Count: %d\n", executor.getActiveCount());
            
            // 返回执行器已经接收了多少个任务。
            System.out.printf("Server: Task Count: %d\n", executor.getTaskCount());
            // 返回执行器已经完成的任务数。
            System.out.printf("Server: Completed Tasks: %d\n", executor.getCompletedTaskCount());
            // 返回曾经同时位于线程池中的最大线程数。
            System.out.printf("Server: Largest Pool Size: %d\n", executor.getLargestPoolSize());
            
            System.out.printf("------------ cycle %d end ---------------\n", i);
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