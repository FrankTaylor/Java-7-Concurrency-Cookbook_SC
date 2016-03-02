package chapter6;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 当一个线程在对 “原子变量” 操作时，如果其他线程也试图对同一 “原子变量” 执行操作。该 “原子变量” 的实现类使用一套
 * 称为 “CAS 原子操作” 机制来保证对其的并发访问。
 * 
 * CAS 原子操作的大致执行过程：执行写入操作时，先获取其变量的值，然后在本地修改变量的值，再试图用已修改的值去替换之
 * 前的值。如果之前的值没有被其他线程改变，就执行这个替换操作。否则，重新执行这个操作。
 * 
 * “原子变量” 不使用 “锁” 或 “其他同步机制” 来保护对其值的并发访问。所有操作都是基于 “CAS 原子操作” 的。它保证
 * 了多线程在同一时刻操作同一 “原子变量” 而不会产生数据不一致的错误，并且它的性能优于使用同步机制保护的普通变量。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_7 {
	
	private static class Account {
		
		/** 声明一个 Long 型原子变量，来存放账户余额。*/
		private AtomicLong balance;
		
		public Account() {
			balance = new AtomicLong();
		}
		
		public long getBalance() {
			return balance.get();
		}
		
		public void setBalance(long balance) {
			this.balance.set(balance);
		}
		
		public void addAmount(long amount) {
			this.balance.getAndAdd(amount);
		}
		
		public void subtractAmount(long amount) {
			this.balance.getAndAdd(-amount);
		}
	}
	
	/** 模拟公司付款的任务。*/
	private static class Company implements Runnable {
		
		private Account account;
		
		public Company(Account account) {
			this.account = account;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				account.addAmount(1000);
			}
		}
	}
	
	/** 模拟银行扣钱的任务。*/
	private static class Bank implements Runnable {
		
		private Account account;
		
		public Bank(Account account) {
			this.account = account;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				account.subtractAmount(1000);
			}
		}
	}
	
	/*
	 * 更多信息：
	 * 
	 * Java 还提供了其他原子类，AtomicBoolean、AtomicInteger 和 AtomicReference 等。
	 */
	public static void main(String[] args) throws InterruptedException {
		
		Account account = new Account();
		account.setBalance(1000);
		
		Thread companyThread = new Thread(new Company(account));
		Thread bankThread = new Thread(new Bank(account));
		
		System.out.printf("Account : Initial Balance: %d\n", account.getBalance());
		
		companyThread.start();
		bankThread.start();
		
		companyThread.join();
		bankThread.join();
		
		System.out.printf("Account : Final Balance: %d\n", account.getBalance());
	}
}