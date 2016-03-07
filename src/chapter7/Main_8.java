package chapter7;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * LinkedTransferQueue：适用于 “生产者-消费者” 结构的程序中。在这些应用程序中，有一个或多个生产者和消费者，
 * 然后这些参与者都共享一个数据结构。生产者将数据保存到结构中，消费者则从其中取出数据。如果数据结构中没有数据，消费者
 * 线程将被阻塞，直到结构中有可用的数据。如果结构中的数据已满，则生产者将被阻塞，直到结构中有可用的空间，可以存放生产
 * 者将要存放进来的数据。同时，结构中的元素是按照到达的先后顺序进行存储的，所以早到的被优先消费。
 * 
 * PriorityBlockingQueue：在这个结构中，元素按顺序存储。这些元素必须实现 Comparable 接口，并实现接口中
 * 定义的 compareTo() 方法。当插入一个元素，它会与已有元素进行比较，直至找到它的位置。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_8 {
	
	private static class MyPriorityTransferQueue<E> extends PriorityBlockingQueue<E> implements TransferQueue<E> {
		
		private AtomicInteger counter;
		private LinkedBlockingQueue<E> transfered;
		private ReentrantLock lock;
		
		public MyPriorityTransferQueue() {
			this.counter = new AtomicInteger(0);
			this.lock = new ReentrantLock();
			this.transfered = new LinkedBlockingQueue<E>();
		}
		
		/**
		 * 这个方法尝试立即将元素发送到一个正在等待的消费者。如果没有等待中的消费者，该方法返回 false。
		 */
		@Override
		public boolean tryTransfer(E e) {
			
			lock.lock();
			boolean value;
			if (counter.get() == 0) {
				value = false;
			} else {
				super.put(e);
				value = true;
			}
			lock.unlock();
			
			return value;
		}
		
		/**
		 * 这个方法尝试立即将元素发送到一个正在等待的消费者。如果没有等待中的消费者，该方法将元素存储到队列中，
		 * 并等待出现试图获取元素的第一个消费者。但在这之前，线程将被阻塞。
		 */
		@Override
		public void transfer(E e) throws InterruptedException {
			
			lock.lock();
			if (counter.get() != 0) {
				super.put(e);
				lock.unlock();
			} else {
				transfered.add(e);
				lock.unlock();
				
				synchronized (e) {
					e.wait();
				}
			}
		}
		
		/**
		 * 第一个参数：生产和消费的元素；第二个参数：如果没有消费者则等待一段时间；第三个参数：等待时间的单位。
		 * 如果有消费者在等待，它就立即发送元素。否则，将参数指定的时间转换为毫秒，并使用 wait() 方法让线程修改。当消费者取走元素时，
		 * 如果线程仍在休眠，将使用 notify() 方法去唤醒它。
		 */
		@Override
		public boolean tryTransfer(E e, long timeout, TimeUnit unit) throws InterruptedException {
			
			lock.lock();
			if (counter.get() != 0) {
				super.put(e);
				lock.unlock();
				return true;
			} else {
				transfered.add(e);
				long newTimeout = TimeUnit.MICROSECONDS.convert(timeout, unit);
				lock.unlock();
				
				e.wait(newTimeout);

				if (transfered.contains(e)) {
					transfered.remove(e);
					lock.unlock();
					return false;
				} else {
					lock.unlock();
					return true;
				}
			}
		}
		
		@Override
		public boolean hasWaitingConsumer() {
			return (counter.get() != 0);
		}
		
		@Override
		public int getWaitingConsumerCount() {
			return counter.get();
		}
		
		@Override
		public E take() throws InterruptedException {
			
			/*
			 * 获取锁，并增加正在等待的消费者的数量。
			 */
			lock.lock();
			counter.incrementAndGet();
			
			/*
			 * 如果队列中没有元素，则释放锁并尝试用 take() 方法从队列中取得一个元素，并再次获取锁。如果队列中没有元素，该方法将让线程休眠，
			 * 直至有元素可被消费。
			 */
			E value = transfered.poll();
			if (value == null) {
				lock.unlock();
				value = super.take();
				lock.lock();
			/*
			 * 否则，从队列中取出元素，并唤醒可能在等待元素被消费的线程。
			 */
			} else {
				synchronized (value) {
					value.notify();
				}
			}
			
			/*
			 * 减少正在等待的消费者的数量，并释放锁。
			 */
			counter.decrementAndGet();
			lock.unlock();
			return value;
		}
	}
	
	private static class Event implements Comparable<Event> {
		
		private String thread;
		private int priority;
		
		public Event(String thread, int priority) {
			this.thread = thread;
			this.priority = priority;
		}
		
		public String getThread() {
			return thread;
		}
		
		public int getPriority() {
			return priority;
		}
		
		public int compareTo(Event e) {
			if (this.priority > e.getPriority()) {
				return -1;
			} else if (this.priority < e.getPriority()) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	private static class Producer implements Runnable {
		
		private MyPriorityTransferQueue<Event> buffer;
		
		public Producer(MyPriorityTransferQueue<Event> buffer) {
			this.buffer = buffer;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < 100; i++) {
				Event event = new Event(Thread.currentThread().getName(), i);
				buffer.put(event);
			}
		}
	}
	
	private static class Consumer implements Runnable {
		
		private MyPriorityTransferQueue<Event> buffer;
		
		public Consumer(MyPriorityTransferQueue<Event> buffer) {
			this.buffer = buffer;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < 1002; i++) {
				try {
					Event value = buffer.take();
					System.out.printf("Consumer: %s: %d\n", value.getThread(), value.getPriority());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		MyPriorityTransferQueue<Event> buffer = new MyPriorityTransferQueue<Event>();
		
		Producer producer = new Producer(buffer);
		
		Thread[] producerThreads = new Thread[10];
		for (int i = 0; i < producerThreads.length; i++) {
			producerThreads[i] = new Thread(producer);
			producerThreads[i].start();
		}
		
		Consumer consumer = new Consumer(buffer);
		Thread consumerThread = new Thread(consumer);
		consumerThread.start();
		
		System.out.printf("Main: Buffer: Consumer count: %d\n", buffer.getWaitingConsumerCount());
		
		Event myEvent = new Event("Core Event", 0);
		buffer.transfer(myEvent);
		System.out.printf("Main: My Event has ben transfered.\n");
		
		for (int i = 0; i < producerThreads.length; i++) {
			producerThreads[i].join();
		}
		
		TimeUnit.SECONDS.sleep(1);
		
		System.out.printf("Main: Buffer: Consumer count: %d\n", buffer.getWaitingConsumerCount());
		
		myEvent = new Event("Core Event 2", 0);
		buffer.transfer(myEvent);
		
		consumerThread.join();
		
		System.out.printf("Main: End of the program\n");
	}
}