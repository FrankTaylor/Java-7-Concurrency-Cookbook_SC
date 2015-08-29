package chapter4;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main_9 {
	
    /* 构造一个无限循环的任务。*/
    private static class Task implements Callable<String> {

        @Override
        public String call() throws Exception {
            while (true) {
                System.out.printf("Task: Test\n");
                Thread.sleep(100);
            }
        }
    }

    public static void main(String[] args) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newCachedThreadPool();

        Task task = new Task();

        System.out.printf("Main: Executing the Task\n");

        Future<String> result = executor.submit(task);
        executor.shutdown();

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.printf("Main: Canceling the task\n");

        /* 取消该任务的执行。
         *
         * 1、如果任务已经完成，或者之前已经被取消，或者由于某种原因而不能被取消，那么方法将返回false，并且任务也不能取消。
         * 2、如果任务在执行器中等待分配线程执行，那么调用 cancel(false) 或 cancel(true) 该任务能被取消，并且不会开始执行。
         * 3、如果任务在执行器中已经在运行了，那么取消任务就依赖于调用 cancel() 方法时所传递的参数。如果调用 cancel(true)
         * 那么该任务将被取消。如果调用 cancel(false) 且任务正在运行，那么该任务就不会被取消。
         *
         * 注意：如果该任务已经被取消，那么使用 Future.get() 方法时将抛出 CancellationException 异常。
         */
        result.cancel(true);

        System.out.printf("Main: cancelled: %s\n", result.isCancelled());
        System.out.printf("Main: Done: %s\n", result.isDone());
        
        
        System.out.printf("Main: the executor has finished\n");
    }

}
