package chapter6;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * 在使用 “锁” 或 “synchronized” 来保护共享资源时，会存在下列问题：
 * 1、死锁：一个线程被阻塞，并试图获得的锁正被其他线程使用，但其他线程却长期不会释放这个锁。
 * 这种情况使得应用不会继续执行，并且永远不会结束。
 * 2、即使只有一个线程访问共享对象，它仍然需要获取和释放锁。
 * 
 * 为了搞定以上两点，以提供更好的性能，Java 引入了 CAS 机制（Compare-and-Swap Operation）
 * 即 “比较和交互” 操作，该操作使用以下三步修改变量的值：
 * 1、取得变量值，即变量的旧值。
 * 2、在一个临时变量中修改变量的值，即变量的新值。
 * 3、试图用新值替换旧值。如果之前的值没有被其他线程改变，就执行这个替换操作。否则，重新执行这个操作。
 * 
 * 采用 “比较和交互” 机制，就不需要使用同步机制了，不仅可以避免死锁，而且性能更好。
 * 
 * Java 在原子变量（Atomic Variable）中实现了 CAS 机制。提供了比较和交换操作的 compareAndSet() 
 * 方法，类中的其它方法也基于它展开。
 * 
 * Java 也引入了原子数组（Atomic Array）提供对 Integer 或 Long 数组的原子操作。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_8 {
	
	private static class Incrementer implements Runnable {
		
		/** 存放一个整型数字数组。*/
		private AtomicIntegerArray vector;
		
		public Incrementer(AtomicIntegerArray vector) {
			this.vector = vector;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < vector.length(); i++) {
				// 对 AtomicIntegerArray 数组中第 i 元素加 １。
				vector.getAndIncrement(i);
			}
		}
	}
	
	private static class Decrementer implements Runnable {
		
		/** 存放一个整型数字数组。*/
		private AtomicIntegerArray vector;
		
		public Decrementer(AtomicIntegerArray vector) {
			this.vector = vector;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < vector.length(); i++) {
				// 对 AtomicIntegerArray 数组中第 i 元素减 １。
				vector.getAndDecrement(i);
			}
		}
	}
	
	/*
	 * 工作原理：
	 * 
	 * 在本例中使用 AtomicIntegerArray 对象实现了下面两个不同的任务。
	 * Incrementer 任务：使用 getAndIncrement() 方法增加数组中所有元素的值。
	 * Decrementer 任务：使用 getAndDecrement() 方法减少数组中所有元素的值。
	 * 
	 * 在任务的结尾，如果没有不一致的错误，数组中的所有元素值都必须是 0.执行程序猴将会看到，程序只将最后的消息
	 * 打印到控制台，因为所有的元素值都为 0。
	 * 
	 * 更多信息：
	 * 
	 * Java 还提供了另一个原子数组类，即 AtomicLongArray 类，它的方法与 AtomicIntegerArray 类相同。
	 * 
	 * 这些原子数组类还提供了其他方法：
	 * get(int i)：返回数组中由参数指定位置的值。
	 * set(int i, int newValue)：设置由参数指定位置的新值。
	 */
	public static void main(String[] args) throws InterruptedException {
		
		final int THREADS = 100;
		
		// 声明 AtomicIntegerArray 数组对象有 1000 个元素，此时每个元素的值都为 0。
		AtomicIntegerArray vector = new AtomicIntegerArray(1000);

		Incrementer incrementer = new Incrementer(vector);
		Decrementer decrementer = new Decrementer(vector);
		
		Thread[] threadIncrementer = new Thread[THREADS];
		Thread[] threadDecrementer = new Thread[THREADS];
		
		for (int i = 0; i < THREADS; i++) {
			threadIncrementer[i] = new Thread(incrementer);
			threadDecrementer[i] = new Thread(decrementer);
			
			threadIncrementer[i].start();
			threadDecrementer[i].start();
		}
		
		for (int i = 0; i < THREADS; i++) {			
			threadIncrementer[i].join();
			threadDecrementer[i].join();
		}
		
		for (int i = 0; i < vector.length(); i++) {
			if (vector.get(i) != 0) {
				System.out.println("Vector[" + i + "] : " + vector.get(i));
			}
		}
		
		System.out.println("Main: End of the example");
	}
}