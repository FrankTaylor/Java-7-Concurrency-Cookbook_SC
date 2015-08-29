package chapter5;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

public class Main_5 {
	
	/** 用来生成一个指定大小的随机整数数组。*/
	private static class ArrayGenerator {
		
		public int[] generateArray (int size) {
			int array[] = new int[size];
			Random r = new Random();
			for (int i = 0; i < size; i++) {
				array[i] = r.nextInt(10);
			}
			
			return array;
		}
	}
	
	private static class SearchNumberTask extends RecursiveTask<Integer> {

		private static final long serialVersionUID = -2904425212812800248L;
		
		private int numbers[];
		private int start, end;
		private int number;
		private TaskManager manager;
		
		/** 当任务找不到数字时返回该常量。*/
		private final static int NOT_FOUND = -1;
		
		/** 构造函数。*/
		public SearchNumberTask(int numbers[], int start, int end, int number, TaskManager manager) {
			this.numbers = numbers;
			this.start = start;
			this.end = end;
			this.number = number;
			this.manager = manager;
		}
		
		@Override
		protected Integer compute() {
			System.out.println("Task: " + start + ":" + end);
			
			/*
			 * 如果end和start的差值大于10，就将这个任务拆分成连个子任务。
			 */
			if ((end - start) > 10) {
				// 创建两个子任务。
				int mid = (start + end) / 2;
				SearchNumberTask task1 = new SearchNumberTask(numbers, start, mid, number, manager);
				SearchNumberTask task2 = new SearchNumberTask(numbers, mid, end, number, manager);
				
				// 把子任务放入列表中。
				manager.addTask(task1);
				manager.addTask(task2);
				
				// 把子任务压入该线程维护的队列中，采用异步的方式执行这两个任务。
				task1.fork();
				task2.fork();
				
				// 如果第一个任务结束后返回的结果不是-1，就返回该结果。否则返回第二个任务执行的结果。
				int returnValue = task1.join();
				if (returnValue != -1) {
					return returnValue;
				}
				
				return task2.join();
			} else {
				
				/*
				 * 否则，遍历任务所需要处理的数组块中的所有元素，将元素中存储的数字和将要寻找的数字进行比较。
				 * 如果它们相等，就在控制台输出找到的信息，并用TaskManager对象来取消所有的任务，然后返
				 * 回已经找到的这个元素的位置。
				 */
				for (int i = start; i < end; i++) {
					if (numbers[i] == number) {
						System.out.printf("Task: Number %d found in position %d\n", number, i);
						manager.cancelTasks(this);
						return i;
					}
					
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) { e.printStackTrace(); }
				}
				
				// 返回-1表示没有找到元素。
				return NOT_FOUND;
			}
		}
		
		public void writeCancelMessage() {
			System.out.printf("Task: Cancelled task from %d to %d", start, end);
		}
	}
	
	/** 用来取消ForkJoinPool中所有的任务。*/
	private static class TaskManager {
		/** 用来保存任务的列表。*/
		private List<ForkJoinTask<Integer>> tasks = new ArrayList<ForkJoinTask<Integer>>();
		/** 把某一任务增加到列表中。*/
		public void addTask(ForkJoinTask<Integer> task) {
			tasks.add(task);
		}
		/** 通过接受一个需要取消剩余任务，然后取消所有的任务。*/
		public void cancelTasks(ForkJoinTask<Integer> cancelTask) {
			for (ForkJoinTask<Integer> task : tasks) {
				if (task != cancelTask) {
					/*
					 * cancel()方法允许取消一个仍没有被执行的任务。如果任务已经开始执行，那么调用cancel()也无法取消。
					 * 该方法接收一个boolean参数，从正常思维理解，如果传true给cancel(true)方法，即使任务正在运行
					 * 也应该能被取消。但是API文档上支出，这个属性没有起到作用。
					 */
					task.cancel(true);
					((SearchNumberTask)task).writeCancelMessage();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		
		// 创建一个容量为1000的数字型数组。
		ArrayGenerator generator = new ArrayGenerator();
		int array[] = generator.generateArray(1000);
		
		// 创建一个取消任务执行的对象。
		TaskManager manager = new TaskManager();
		
		// 启动线程来执行数字查找任务。
		ForkJoinPool pool = new ForkJoinPool();
		
		SearchNumberTask task = new SearchNumberTask(array, 0, 1000, 5, manager);
		pool.execute(task);
		
		pool.shutdown();
		
		// 用1天的时间来等待任务结束。
		try {
			pool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) { e.printStackTrace(); }
		
		System.err.printf("Main: The program has finished\n");
	}
}