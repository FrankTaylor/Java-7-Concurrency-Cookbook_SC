package chapter7;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 
 * “Lock（锁）” 用来保护代码的临界区，同一时间只能有一个线程执行临界区代码。它提供了两种操作：
 * 1、lock()：在开始访问临界区代码时，调用该方法。如果另一个线程正在运行临界区代码，其他线程将被阻塞，直到被访问临界区的锁唤醒。
 * 2、unlock()：在访问临界区代码末尾时，调用该方法。以允许其他线程来访问这部分临界区代码。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_7 {
	
	/*
	 * 工作原理：
	 * 
	 * Java 并发 API 提供的 AbstractQueuedSynchronizer 用来实现带有 “锁” 或 “信号” 特性的同步机制。
	 * 并对等待访问临界区的阻塞线程队列进行了管理。它有下面两个方法：
	 * 1、tryAcquire()：当访问临界区代码时，调用这个方法。如果访问成功，返回 true；否则返回 false。
	 * 2、tryRelease()：当释放临界区代码的访问时，调用这个方法。如果释放成功，返回 true；否则返回 false。
	 * 
	 * 本例中使用 MyQueuedSynchronizer 继承了 AbstractQueuedSynchronizer，并覆盖了 tryAcquire() 和 tryRelease() 方法，
	 * 在覆盖的时候使用了 AtomicInteger 对临界区代码的访问进行了控制。在 “锁” 可以被获取的时候，变量值为 0，这时允许一个线程持有 “锁”，来访问临界区代码；
	 * “锁” 在不可用的时候，变量值为 1，这时不允许任何线程访问临界区代码。
	 * 
	 * 在 MyQueuedSynchronizer 类中，使用 AtomicInteger 的 compareAndSet() 方法试图把第一个参数的值，设为第二个参数的值。
	 */
	private static class MyQueuedSynchronizer extends AbstractQueuedSynchronizer {
		
		private AtomicInteger state;
		
		public MyQueuedSynchronizer() {
			state = new AtomicInteger(0);
		}
		
		@Override
		protected boolean tryAcquire(int arg) {
			return state.compareAndSet(0, 1);
		}
		
		@Override
		protected boolean tryRelease(int arg) {
			return state.compareAndSet(1, 0);
		}
	}
	
	/*
	 * 工作原理：
	 * 
	 * 本例中使用 MyLock 实现 Lock 接口。并用 MyQueuedSynchronizer 实现 Lock 接口中的方法。
	 * 在 lock() 方法中，调用了 AbstractQueuedSynchronizer 类中的 acquire()，而该方法会先调用 MyQueuedSynchronizer 
	 * 中实现 tryAcquire() 方法，进而完成 “获取锁” 的任务，其实现源码如下：
	 * 
	 * AbstractQueuedSynchronizer 类中的源码：
	 * 
	 * protected boolean tryAcquire(int arg) {
	 *     throw new UnsupportedOperationException();
	 * }
	 * 
	 * public final void acquire(int arg) {
	 *     if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
	 *         selfInterrupt();
	 * }
	 * 
	 * 其他方法的实现也是同样的原理。
	 * 
	 * 基本上可以认为 ReentrantLock 的实现也和 MyLock 是类似的，当然源码的实现要复杂的多了。
	 */
	private static class MyLock implements Lock {
		
		private AbstractQueuedSynchronizer sync;
		
		public MyLock() {
			sync = new MyQueuedSynchronizer();
		}
		
		@Override
		public void lock() {
			sync.acquire(1);
		}
		
		@Override
		public void lockInterruptibly() throws InterruptedException {
			sync.acquireInterruptibly(1);
		}
		
		@Override
		public boolean tryLock() {
			try {
				return sync.tryAcquireNanos(1, 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			return sync.tryAcquireNanos(1, TimeUnit.NANOSECONDS.convert(time, unit));
		}
		
		@Override
		public void unlock() {
			sync.release(1);
		}
		
		@Override
		public Condition newCondition() {
			return sync.new ConditionObject();
		}
	}
	
	private static class Task implements Runnable {
		
		private MyLock lock;
		private String name;
		
		public Task(String name, MyLock lock) {
			this.lock = lock;
			this.name = name;
		}
		
		@Override
		public void run() {
			lock.lock();
			System.out.printf("Task: %s: Task the lock\n", name);
			try {
				TimeUnit.SECONDS.sleep(2);
				System.out.printf("Task: %s: Free the lock\n", name);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}
	}
	
	/*
	 * 更多信息：
	 * 
	 * AbstractQueuedSynchronizer 抽象类提供了两个方法来管理 “锁状态”。getState() 和 setState()。
	 * 这两个方法接收，并返回 “锁状态” 的整型值。
	 * 
	 * Java 并发 API 提供了另一个类来实现同步机制，即 AbstractQueuedLongSynchronizer ，它与 AbstractQueuedSynchronizer 
	 * 抽象类是一样的，只是使用了一个 long 来存储线程状态而已。
	 */
	public static void main(String[] args) {
		
		MyLock lock = new MyLock();

		for (int i = 0; i < 10; i++) {
			Task task = new Task("Task-" + i, lock);
			Thread thread = new Thread(task);
			thread.start();
		}
		
		boolean value;
		do {
			try {
				value = lock.tryLock(1, TimeUnit.SECONDS);
				if (!value) {
					System.out.printf("Main: Trying to get the Lock\n");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				value = false;
			}
		} while (!value);
		
		System.out.printf("Main: Got the lock\n");
		lock.unlock();
		
		System.out.printf("Main: End of the program\n");
	}
}