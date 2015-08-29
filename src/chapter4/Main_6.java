package chapter4;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main_6 {
	
	private static class Result {

        private String name;
        private int value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    private static class Task implements Callable<Result> {
        
        private String name;

        public Task(String name) {
            this.name = name;
        }

        @Override
        public Result call() throws Exception {
            System.out.printf("%s: Staring\n", this.name);

            try {
            	long duration = (long)(Math.random() * 10);
            	System.out.printf("%s: Waiting %d seconds for results.\n", this.name, duration);
            	TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) { e.printStackTrace(); }
            
            int value = 0;
            for (int i = 0; i < 5; i++) {
            	value += (int)(Math.random() * 100);
            }
            
            Result result = new Result();
            result.setName(this.name);
            result.setValue(value);
            
            System.out.println(this.name + ": Ends");
            
            return result;
        }
    }
    
    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();

        List<Task> taskList = new ArrayList<Task>();
        for (int i = 0; i < 3; i++) {
            Task task = new Task("Task_" + i);
            taskList.add(task);
        }

        List<Future<Result>> resultList = null;
        try {
        	/*
        	 * invokeAll() 方法将接收一个 Callable对象列表，并返回一个 Future 对象列表。
        	 * 该方法将阻塞当前的执行线程，直到执行器中所有的任务都完成。
        	 * 
        	 * invokeAll(taskList, 1, TimeUnit.SECONDS); 该方法执行所有的任务，如果在给定的超时期满之前任务必须全部成功完成（未抛出异常），则返回其结果。
             * 如果到了规定时间，如果有一个任务未执行完成，将抛出 InterruptedException 异常。
             * 
             * 注意：如果抛出了 InterruptedException 异常。resultList 还是会返回任务数量的列表，但在 resultList.get(i) 获得 Future对象时，将抛出异常。
        	 */
            resultList = executor.invokeAll(taskList, 3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        	e.printStackTrace();
        }
        
        executor.shutdown();

        System.out.println("Main: Printing the results");
        for (int i = 0; i < resultList.size(); i++) {
        	Future<Result> future = resultList.get(i);
        	try {
            	/*
            	 * 调用 get() 方法会阻塞当前主线程，直到任务完成并返回结果。
            	 * 在调用 get(1, TimeUnit.SECONDS) 方法时，如果到了规定时间，任务还未执行完成，将抛出 TimeoutException 异常。
            	 * 
            	 * 注意：即使前面的代码没有调用 shutdown()方法，只要任务执行完成，就可以得到结果。
            	 */
                Result result = future.get();
                System.out.println("------------------>" + result.getName() + ": " + result.getValue());
            } catch (InterruptedException e) {
            	e.printStackTrace();
            } catch (ExecutionException e) {
            	e.printStackTrace();
            }
        }
	}
}
