package chapter1;

import java.util.Random;

public class MyThreadGroup extends ThreadGroup {

	public MyThreadGroup(String name) {
		super(name);
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		System.out.printf("The thread %s has thrown an Exception\n", t.getId());
		e.printStackTrace(System.out);
		System.out.printf("Terminating the rest of the Threads\n");
		interrupt();
	}
	
	private class Task implements Runnable {
		
		@Override
		public void run() {
			int result;
			Random random = new Random(Thread.currentThread().getId());
			while (true) {
				result = 1000/((int)(random.nextDouble() * 1000));
				System.out.printf("%s : %d\n", Thread.currentThread().getId(), result);
				if (Thread.currentThread().isInterrupted()) {
					System.out.printf("%d : Interrupted\n", Thread.currentThread().getId());
					return;
				}
			}
		}
	}
	
	public static void main(String[] args) {
		MyThreadGroup threadGroup = new MyThreadGroup("MyThreadGroup");
		Task task = threadGroup.new Task();
		for (int i = 0; i < 2; i++) {
			Thread t = new Thread(threadGroup, task);
			t.start();
		}
	}
}