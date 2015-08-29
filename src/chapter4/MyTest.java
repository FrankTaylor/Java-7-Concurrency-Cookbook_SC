package chapter4;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MyTest {
	
	private static class Task implements Callable<Integer> {
		public Integer call() {
			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return 1;
		}
	}
	
	public static void main(String[] args) throws TimeoutException {
		ExecutorService executor = Executors.newCachedThreadPool();
		
//		List<Task> tList = new ArrayList<Task>();
//		tList.add(new Task());
		
		Future<Integer> future = executor.submit(new Task());
		executor.shutdown();
		
		try {
			System.out.println(future.get(1, TimeUnit.MICROSECONDS));;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("11111111111111111");
	}
}