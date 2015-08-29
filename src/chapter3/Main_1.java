package chapter3;

import java.util.concurrent.Semaphore;

public class Main_1 {
	
	/** 该类用于模拟执行打印任务的队列。*/
	private static class PrintQueue {
		/** 声明JDK提供的信号量对象。*/
		private final Semaphore semaphore = new Semaphore(1);
		
		public void printJob(Object document) {
			try {
				/*
				 * 使用 Semaphore#acquire() 方法来获得信号量。
				 * 注意1：如果获得的信号量 <= 0，则该方法会阻塞当前的线程，直到信号量 > 0。
				 * 注意2：执行该方法后，会将当前信号量 - 1。
				 * 
				 * 该方法还有一个带参数的版本：Semaphore#acquire(100)。
				 * 注意1：执行该方法后，会将当前信号量 - 指定的值（这里是 100）。
				 * 
				 * Semaphore 类还有其他两种 acquire() 方法。
				 * 1、acquireUninterruptibly()：该方法与 acquire() 方法差不多。只不过该方法会忽略线程的中断，不抛出任务异常。
				 * （当信号量内部计数器变为 0 时，会阻塞当前线程，直至信号量 > 0。线程在被阻塞期间如被中断， acquire() 方法将会抛出 InterruptedException 异常。）
				 * 注意1：执行该方法后，会将当前信号量 - 1。同时，该方法也有带参数的版本。
				 * 
				 * 2、tryAcquire()：该方法会尝试获得信号量。如果信号量 > 0，返回true；如果信号量 <=0 返回false。但即使返回 false，该方法也不会阻塞当前线程。
				 * 注意1：执行该方法后，会将当前信号量 - 1。同时，该方法也有带参数的版本。
				 * 注意2：如果当前信号为1，执行 tryAcquire(100)后，会返回false。当前信号值还为 1 。
				 * 
				 * 对于 acquire(int permits)、acquireUninterruptibly(int permits)、tryAcquire(int permits)和release(int permits)
				 * 方法来说，参数声明了线程试图获取或者释放的共享资源的数目，也就是该线程想要在信号量内部计数器上增加或删除的数目。
				 * 对于 “获取信号量” 的方法来说，如果计数器的值少于参数对应的值，那么线程将被阻塞，直到计数器重新累加到，或超过这个值。
				 * 
				 * 在Java语言中，只要涉及到多线程阻塞，并等待同步资源的释放情况时，就会涉及到公平的概念。
				 * 在非公平模式中：一旦同步的资源被释放后，系统将从众多等待的线程中随机调度一个来执行。
				 * 在公平的模式中：一旦同步的资源被释放后，系统选择的是等待时间最长的那个线程。
				 * 
				 * Semaphore 类还有一个构造器支持传递 2 个参数，该构造器的第二个参数的类型是 boolean 型的。如果传入 false 时，那么创建的信号量就是“非公平模式”的，
				 * 如果传入 true 时，那么创建的信号量就是“公平模式”的。
				 * 
				 * 
				 * 还有，由于在成员变量中设置的信号量为1 （new Semaphore(1)），所以该信号只能保护对一个资源的访问。
				 */
				semaphore.acquire();
				
				// 随机一个休眠时间来模拟打印任务。
                long duration = (long)(Math.random() * 10);
                System.out.printf("%s: PrintQueue: Printing a Job during %d seconds\n", Thread.currentThread().getName(), duration);
                Thread.sleep(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				/*
				 * 使用 Semaphore#release() 方法可以使当前的信号量 + 1。
				 * 例如：如果当前信号量是 1，在执行  Semaphore#release() 方法后，当前信号量就变为 2 了。
				 * 
				 * 该方法还有一个带参数的版本：Semaphore#release(100)。
				 * 该方法是指，将当前信号量 + 指定的值（这里是 100）。
				 */
                semaphore.release();
                
                /*
                 * 使用 Semaphore#availablePermits() 方法来返回当前的信号量。
                 */
                System.out.printf("Current Permits : %d\n", semaphore.availablePermits());
            }
		}
	}
	
	/** 创建 Job 任务来模拟将文档发送到打印队列中。*/
    private static class Job implements Runnable {

        private final PrintQueue printQueue;

        public Job(PrintQueue printQueue) {
            this.printQueue = printQueue;
        }

        @Override
        public void run() {
            System.out.printf("%s: Going to print a job\n", Thread.currentThread().getName()); 
            printQueue.printJob(new Object());
            System.out.printf("%s: The document has been printed\n", Thread.currentThread().getName());
        }
    }

    public static void main(String[] args) {
        PrintQueue printQueue = new PrintQueue();
        
        // 创建 10 个线程来执行 10个Job 任务。
        Thread thread[] = new Thread[10];
        for (int i = 0; i < 10; i++) {
            thread[i] = new Thread(new Job(printQueue), "Thread" + i);
        }
        
        // 启动这 10 个线程。
        for (int i = 0; i < 10; i++) {
            thread[i].start();
        }
    }
}
