package chapter7;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
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
	
	private static class MyAbstractQueuedSynchronizer extends AbstractQueuedSynchronizer {
		
		private AtomicInteger state;
		
		public MyAbstractQueuedSynchronizer() {
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
	
	private static class MyLock implements Lock {
		
		private AbstractQueuedSynchronizer sync;
		
		public MyLock() {
			sync = new MyAbstractQueuedSynchronizer();
		}
		
		@Override
		public void lock() {
			sync.acquire(1);
		}
		
		@Override
		public void lockInterruptibly() throws InterruptedException {
			sync.acquireInterruptibly(1);
		}
	}
}