package chapter4;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main_1 {
	
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
    	 * 创建缓存线程池的执行器。
    	 * 
    	 * 使用 newCachedThreadPool() 方法创建的 ThreadPoolExecutor 会在运行过程中碰到线程数量的问题。
    	 * 在执行任务时，会首先检查池中是否有闲置的线程，如果没有就新创建一个工作线程来执行任务。
    	 * 
    	 * “线程重用” 的优点是减少了创建新线程的时间。但当发送大量的任务给执行器，且每个任务需要执行的时间较长时，就会
    	 * 因为创建的线程太多，而升高系统的负载。
    	 */
    	ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newCachedThreadPool();
    	
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
        
        /*
         * 如果执行器没有任务可以执行，且没有关闭执行器，它将继续等待新任务的到来，而不会结束执行。
         * Java应用程序不会结束，直到所有非守护线程结束它们的运行。（如果不调用shutdown方法，程序结束的时间将延长很多）
         * 
         * 调用执行器 shutdown() 方法后，执行器将不再接收新的任务，在完成所有待运行的任务后结束执行。
         * 
         * 在调用 shutdown()方法之后，如果尝试再发送一个任务给执行器，任务将被拒绝，并且执行器也会抛出
         * RejectedExecutionException异常。
         */
        executor.shutdown();
        
        // 如果调用了 shutdown() 方法，那么这个方法将返回 true。
        System.out.println("isShutdown(): " + executor.isShutdown());
        /*
         * 如果调用了 shutdown()或shutdownNow()方法，并且执行器“完成了关闭的过程”，该方法才返回 true。
         * 
         * 注意：这个关闭过程有两层意思：
         * 1、调用 shutdown() 或 shutdownNow() 方法主动关闭线程池；
         * 2、池中完成任务执行，或完成强制取消任务。
         */
        System.out.println("isTerminated:" + executor.isTerminated());
        
        /*
         * 调用执行器 shutdownNow() 方法后，执行器将不再接收新的任务，如果有待执行任务，就把它们移除队列。
         * 如果有正在执行的任务，就尝试停止该任务。
         */
        // executor.shutdownNow();
        
        /*
         * 调用执行器 awaitTermination() 方法后会阻塞当前线程，直到指定的时间，或执行器中的任务完成。
         * 
         * 注意：即便池中任务完成了，但如果在之前的代码中没有调用 shutdown() 来关闭执行器，该方法会继续阻
         * 塞当前线程，直到指定的时间。 
         */
        try {
			executor.awaitTermination(100, TimeUnit.SECONDS);
			System.out.println("isTerminated:" + executor.isTerminated());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("执行已完成");
    }
}
