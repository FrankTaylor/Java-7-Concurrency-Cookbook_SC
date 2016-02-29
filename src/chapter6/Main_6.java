package chapter6;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Java 7 中新引入的 ThreadLocalRandom 类是线程本地变量。可做到让每个生成随机数的线程都有一个不同的生成器，
 * 但都在同一个类中被管理。相比使用共享的 Random 对象为所有线程生成随机数来说，这种机制具有更好的性能。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_6 {
	
	private static class TaskLocalRandom implements Runnable {
		
		public TaskLocalRandom() {
			ThreadLocalRandom.current();
		}
		
		@Override
		public void run() {
			String name = Thread.currentThread().getName();
			for (int i = 0; i < 10; i++) {
				System.out.printf("%s: %d\n", name, ThreadLocalRandom.current().nextInt(10));
			}
		}
	}
	
	/*
	 * 工作原理：
	 * 
	 * ThreadLocalRandom#current() 是一个静态方法，返回与当前线程关联的 ThreadLocalRandom 对象，
	 * 可以使用该对象生成随机数。但如果调用这个方法的线程还没有关联 ThreadLocalRandom 对象，就会生成一个新的。
	 * 因此，在本例中构造器中使用 current() 返回的对象，与在 run() 方法中返回的对象不一致。
	 * 
	 */
	public static void main(String[] args) {
		
		Thread[] threads = new Thread[3];
		
		for (int i = 0; i < 3; i++) {
			TaskLocalRandom task = new TaskLocalRandom();
			threads[i] = new Thread(task);
			threads[i].start();
		}
	}
}