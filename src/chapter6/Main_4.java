package chapter6;

import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * DelayQueue 这个类可以保存带有 “激活时间” 的元素。当调用的方法从队列中 “返回/提取” 元素时，未来的元素日期将被忽略。
 * 这些元素对于这些方法是不可见的。
 * 
 * DelayQueue 是一个无界的 BlockingQueue，其元素只能在到期后从队列中取走。同时，该队列还是有序的，队列中第一个元
 * 素的延迟时间最短（排序是根据 compareTo() 方法的返回值定的，越小越靠前）。注意：不能将 null 元素放置在队列中。
 * 
 * 为了具有调用时的这些行为，存放到 DelayQueue 类中的元素必须继承 Delayed 接口。Delayed 接口使对象成为延迟对象，
 * 它使存放在 DelayQueue 类中的对象具有了激活时间，该接口会强制执行下列两个方法：
 * 
 * compareTo(Delayed o)：Delayed 接口继承了 Comparable 接口，因此有了这个方法。如果当前对象的延迟值小于
 * 参数对象的值，将返回一个小于 0  的值；如果当前对象的延迟值大于参数对象的值，将返回一个大于 0 的值；如果两者的延迟值相
 * 等，则返回 0。
 * 
 * getDelay(TimeUnit unit)：这个方法返回到激活日期的剩余时间，单位由 TimeUnit 指定。该类是一个有下列常量组
 * 成的枚举类型：DAYS、HOURS、MICROSECONDS、MILLSECONDS、MINUTES、NANOSECONDS 和 SECONDS。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_4 {
	
	private static class Event implements Delayed {
		
		/** 这个 “开始时间” 指的就是 “激活时间”。*/
		private Date startDate;
		
		public Event(Date startDate) {
			this.startDate = startDate;
		}
		
		/**
		 * 该方法用来计算 “激活时间” 和 “实际时间” 之间的纳秒数。这两个日期都是 Date 类的对象，并使用日期对象的 
		 * getTime() 方法将日期转化为毫秒数后进行比较。然后通过 getDelay() 方法入参  TimeUnit#convert()
		 * 方法，将时间间隔转化为 event 激活时间的剩余纳秒数。DelayQueue 类本身是使用纳秒工作的。
		 */
		@Override
		public long getDelay(TimeUnit unit) {
			Date now = new Date();
			long diff = startDate.getTime() - now.getTime();
			return unit.convert(diff, TimeUnit.MILLISECONDS);
		}
		
		@Override
		public int compareTo(Delayed o) {
			long result = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
			if (result < 0) {
				return -1;
			} else if (result > 0) {
				return 1;
			}
			return 0;
		}
	}
	
	private static class Task implements Runnable {
		
		/** 存放 task 编号。*/
		private int id;
		/** 声明一个延迟队列。*/
		private DelayQueue<Event> queue;
		
		public Task(int id, DelayQueue<Event> queue) {
			this.id = id;
			this.queue = queue;
		}
		
		@Override
		public void run() {
			Date now = new Date();
			Date delay = new Date();
			delay.setTime(now.getTime() + (id * 1000));
			System.out.printf("Thread %s: %s\n", id, delay);
			
			for (int i = 0; i < 100; i++) {
				Event event = new Event(delay);
				queue.add(event);
			}
		}
	}
	
	/*
	 * 更多信息：
	 * 
	 * DelayQueue 类还提供了其他一些方法：
	 * 
	 * clear()：移除队列中的所有元素。
	 * offer(E e)：E 是 DelayQueue 的反省参数，表示传入参数的类型。这个方法把参数对应的元素插入到队列中。
	 * peek()：返回队列中的第一个元素，但不将其移除。
	 * take()：返回队列中的第一个元素，并将其移除。如果队列为空，线程将被阻塞，直到队列中有可用的元素。
	 */
	public static void main(String[] args) throws InterruptedException {

		DelayQueue<Event> queue = new DelayQueue<Event>();
		
		Thread[] threads = new Thread[5];
		
		for (int i = 0; i < threads.length; i++) {
			Task task = new Task(i + 1, queue);
			threads[i] = new Thread(task);
		}
		
		for (Thread t : threads) {
			t.start();
		}
		
		for (Thread t : threads) {
			t.join();
		}
		
		do {
			int counter = 0;
			Event event;
			
			/*
			 * 当队列长度大于 0 时，使用 poll() 方法提取并移除队列中的第一个元素，如果队列中没有活动的元素时，
			 * 该方法将返回 null，则使当前线程休眠 500 毫秒以等待更多元素被激活。
			 */
			do {
				event = queue.poll();
				
				System.out.println("---------------------" + event);
				if (event != null) {
					counter++;
				} 
			} while (event != null);
			
			System.out.printf("At %s you have read %d events \n", new Date(), counter);
			TimeUnit.MILLISECONDS.sleep(1000);
		} while (queue.size() > 0);
	}
}