package chapter4;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main_12 {

    private static class Task implements Runnable {
        
        private String name;

        public Task(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            System.out.println("Task " + name + ": Starting");

            try {
                long duration = (long)(Math.random() * 10);
                System.out.printf("Task %s: ReportGenerator: Generating a report during %d seconds\n", name, duration);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.printf("Task %s: Ending\n", name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static void main(String[] args) {
    	
    	ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newCachedThreadPool();
    	
    	/*
    	 * 创建 RejectedExecutionHandler 对象来管理被拒绝的任务。
    	 */
    	RejectedExecutionHandler reHandler = new RejectedExecutionHandler() {
    		@Override
    		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    			// 输出被拒绝任务中toString()方法返回的内容。 
    			System.out.printf("RejectedTaskController: The task %s has been rejected\n", r.toString());
    			
    			// 输出执行器的状态。
    			System.out.printf("RejectedTaskController: %s\n", executor.toString());
    			System.out.printf("RejectedTaskController: Terminating: %s\n", executor.isTerminating());
    			System.out.printf("RejectedTaskController: Terminated: %s\n", executor.isTerminated());
    		}
    	};
    	
    	/*
    	 * 设置用于被拒绝的任务的处理程序。
    	 * 
    	 * 如果该执行器没有设置用来处理被拒绝任务的处理器，一旦在调用 shutdown() 方法后，有接收了一个任务，就将抛出 RejectedException
    	 */
    	executor.setRejectedExecutionHandler(reHandler);
    	
    	// 创建 3个可以加入执行器的 Task 任务并发送给执行器。
    	System.out.printf("Main: Starting.\n");
    	for (int i = 0; i < 3; i++) {
    		Task task = new Task("Task " + i);
    		executor.submit(task);
    	}
    	
    	System.out.printf("Main: Shutting down the Executor. \n");
    	executor.shutdown();
    	
    	// 创建 1个在发送给执行器会被拒绝的 Task 任务。
    	System.out.printf("Main: Sending another Task. \n");
    	Task task = new Task("RejectedTask");
    	executor.submit(task);
    	
    	System.out.println("Main: End.");
    }
}
