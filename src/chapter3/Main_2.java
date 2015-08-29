package chapter3;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main_2 {
	
	/** 该类用于模拟执行打印任务的队列。*/
	private static class PrintQueue {
		/** 声明JDK提供的信号量对象。*/
		private final Semaphore semaphore = new Semaphore(3);
		/** 用来存放打印机的状态，即空闲或正在打印。*/
		private boolean freePrinters[] = {true, true, true};
		/** 声明一个锁对象，用来保护对 freePrinters 数组的访问。*/
		private Lock lockPrinters = new ReentrantLock();
		
		public void printJob(Object document) {
			try {
				semaphore.acquire();
				// 得到可用打印机的编号。
				int assignedPrinter = getPrinter();
				// 随机一个休眠时间来模拟打印任务。
                long duration = (long)(Math.random() * 10);
                System.out.printf("%s: PrintQueue: Printing a Job during %d seconds\n", Thread.currentThread().getName(), duration);
                Thread.sleep(duration);
                
                // 将打印机标记为空闲可用的状态。
                freePrinters[assignedPrinter] = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
                semaphore.release();
                System.out.printf("Current Permits : %d\n", semaphore.availablePermits());
            }
		}
		
		/** 用来获得可用打印机的编号。*/
		private int getPrinter() {
			// 用来保存可用打印机的编号。
			int ret = -1;
			try {
				
				/*
				 * 由于初始的信号量对象 new Semaphore(3) ，根据上下文得知，在同一时刻，最多会有3个线程一起访问 getPrinter() 方法。
				 * 如果不加锁，就可能会出现，多个线程访问同一个下标的情况。
				 */
				lockPrinters.lock();
				
				/*
				 * 在 freePrinters 数组中找到第一个值为 true 的元素，并把该元素的下标保存到ret变量中。
				 * 然后将该元素置为 false，表示被定为的打印机将要执行打印工作。
				 */
				for (int i = 0; i < freePrinters.length; i++) {
					if (freePrinters[i]) {
						ret = i;
						freePrinters[i] = false;
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lockPrinters.unlock();
			}
			return ret;
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
