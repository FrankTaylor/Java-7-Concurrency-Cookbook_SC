package chapter4;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main_4 {
	
	private static class FactorialCalculator implements Callable<Integer> {
		
		private Integer number;
		
		public FactorialCalculator(Integer number) {
		    this.number = number;	
		}
		
		@Override
		public Integer call() throws Exception {
            
            int result = 1;
            if ((number == 0) || (number == 1)) {
                result =  1;
            } else {
            	// 算阶乘。
                for (int i = 2; i <= number; i++) {
                    result *= i;
                    TimeUnit.MILLISECONDS.sleep(20);
                }
            }

            System.out.printf("%s: %d\n", Thread.currentThread().getName(), result);
			return result;
		}
	}

    public static void main(String[] args) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(2);

        List<Future<Integer>> resultList = new ArrayList<Future<Integer>>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Integer number = random.nextInt(10);
            FactorialCalculator calculator = new FactorialCalculator(number);
            
            /*
             * 调用执行器的 submit() 方法，将任务发送给执行器。该方法返回一个 Future 对象来管理任务和得到最后的结果。
             * 注意：该方法是异步的，调用后不会阻塞主线程。
             * 
             * Future对象可以用于以下两个主要目的：
             * 1、控制任务的状态：1.1、取消任务；1.2、检查任务状态。
             * 2、获取返回的结果：get() 方法阻塞直到任务完成并返回结果。如果 get() 方法在等待结果时线程中断了，
             *   将抛出 InterruptedException 异常。如果 call() 方法中抛出了异常，那么 get() 方法将抛出  ExecutionException 异常。
             *   
             *   get(long timeout, TimeUnit unit) 在调用这个方法时，如果任务还未执行完，则会阻塞到指定的时间。如果超过了指定的时间，而任
             *   务还未执行完成，那么这个方法将返回 TimeoutException 。
             */
            Future<Integer> result = executor.submit(calculator);
            // 把 Future 对象放入列表。
            resultList.add(result);
        }

        do {
        	// 返回执行器已经完成的任务数。
            System.out.printf("Main: number of Completed Tasks: %d\n", executor.getCompletedTaskCount());
            
            // 通过调用 Future.isDone() 来输出任务是否完成的信息。
            for (int i = 0; i < resultList.size(); i++) {
                Future<Integer> result = resultList.get(i);
                System.out.printf("Main: Task %d: %s\n", i, result.isDone());
            }

            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) { e.printStackTrace(); }
            
            // 如果任务没有全部完成就一直循环。
        } while (executor.getCompletedTaskCount() < resultList.size());

        System.out.println("Main: Results\n");
        for (int i = 0; i < resultList.size(); i++) {
            Future<Integer> result = resultList.get(i);
            Integer number = null;
            try {
            	/*
            	 * 调用 get() 方法会阻塞当前主线程，直到任务完成并返回结果。
            	 * 在调用 get(1, TimeUnit.SECONDS) 方法时，如果到了规定时间，任务还未执行完成，将抛出 TimeoutException 异常。
            	 * 
            	 * 注意：即使前面的代码没有调用 shutdown()方法，只要任务执行完成，就可以得到结果。
            	 * 
            	 */
                number = result.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
				e.printStackTrace();
			}
            System.out.printf("Main: Task %d: %d\n", i, number);
        }

        executor.shutdown();
    }
}
