package chapter7;

import java.util.Date;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 工厂模式（Factory Pattern） 在面向对象编程中是一个应用广泛的设计模式。它是一种创建模式（Creational Pattern），目标是
 * 创建一个类，并通过这个类创建一个或多个类对象。当使用工厂类创建对象时，是调用工厂类中的方法，而非用 new 操作符。
 * 
 * 通过工厂模式，能够将对象创建过程集中，这样做的好处是：改变对象的创建方式将会变得很容易，并且针对限定资源，还可以限制创建对象的数量。
 * 例如，通过工厂模式生成一个类型的 N 个对象，就很容易获得创建这些对象的统计数据。
 * 
 * Java 提供了 ThreadFactory 接口来实现 Thread 对象工厂。Java 并发 API 的一些高级辅助类，像 Executor 框架或 Fork/Join 框架，
 * 都可以使用了线程工厂来创建线程。
 * 
 * 线程工厂在 Java 并发 API 中的另一个应用是 Executors 类。它提供了大量方法来创建不同类型的 Executor 对象。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_3 {
	
	private static class MyThread extends Thread {
		
		private Date creationDate;
		private Date startDate;
		private Date finishDate;
		
		public MyThread(Runnable target, String name) {
			super(target, name);
			setCreationDate();
		}
		
		public void setCreationDate() {
			creationDate = new Date();
		}
		
		public void setStartDate() {
			startDate = new Date();
		}
		
		public void setFinishDate() {
			finishDate = new Date();
		}
		
		public long getExecutionTime() {
			return finishDate.getTime() - startDate.getTime();
		}
		
		@Override
		public void run() {
			setStartDate();
			super.run();
			setFinishDate();
		}
		
		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer
			.append(getName())
			.append(": ")
			.append(" Creation Date: ")
			.append(creationDate)
			.append(" : Running time: ")
			.append(getExecutionTime())
			.append(" Milliseconds.");
			return buffer.toString();
		}
	}
	
	private static class MyThreadFactory implements ThreadFactory {
		
		private int counter;
		private String prefix;
		
		public MyThreadFactory(String prefix) {
			this.prefix = prefix;
			counter = 1;
		}
		
		@Override
		public Thread newThread(Runnable r) {
			MyThread myThread = new MyThread(r, prefix + "-" + counter);
			counter++;
			return myThread;
		}
	}
	
	private static class MyTask implements Runnable {
		
		@Override
		public void run() {
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * 工作原理：
	 * 
	 * ThreadFactory 接口中只有一个 new Thread()，该方法接收一个 Runnable 对象作为参数，
	 * 并返回一个执行该任务的 Thread 对象。
	 * 
	 * 当 new ThreadPoolExecutor() 时，如果不传入 ThreadFactory，那源码中将使用内置的 
	 * DefaultThreadFactory 类来作为默认的 ThreadFactory 实现。
	 */
	public static void main(String[] args) throws InterruptedException {
		
		MyThreadFactory myFactory = new MyThreadFactory("MyThreadFactory");
		
		MyTask task = new MyTask();
		Thread thread = myFactory.newThread(task);
		
		thread.start();
		thread.join();
		
		System.out.printf("Main: Thread information.\n");
		System.out.printf("%s\n", thread);
		System.out.printf("Main: End of the example.\n");
	}
	
}