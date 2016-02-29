package chapter6;

import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 并发队列允许不同的线程在同一时间添加或移除队列中的数据，而不会造成数据不一致。
 * 
 * 阻塞并发队列和非阻塞并发队列的主要区别是：阻塞式并发队列在插入和删除操作时，如果队列已满或为空时，
 * 操作不会立即被执行，而是将调用这个操作的线程阻塞，直到队列操作可以成功执行。
 * 
 * 在使用阻塞并发队列中具有阻塞特点的方法时，由于调用它们的线程可能会被阻塞，因此需要调用者自行捕获
 * InterruptedException 异常，以便在阻塞的线程被中断时，可以做相应的动作。
 * 
 * Java 引入了 LinkedBlockingDeque 类来实现阻塞是队列。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_2 {
	
	private static class Client implements Runnable {
		
		private LinkedBlockingDeque<String> requestList;
		
		public Client(LinkedBlockingDeque<String> requestList) {
			this.requestList = requestList;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < 3; i++) {
				
				for (int j = 0; j < 5; j++) {
					StringBuilder request = new StringBuilder();
					request.append(i);
					request.append(":");
					request.append(j);
					
					try {
						/*
						 * 使用 put() 方法向队列中插入元素。如果队列已满（队列生成时指定了固定的容量），调用这个方法
						 * 的线程将被阻塞，直到队列中有了可用的空间。
						 */
						requestList.put(request.toString());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					System.out.printf("Client: %s at %s. \n", request, new Date());
				}
				
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			System.out.printf("Client: End. \n");
		}
	}
	
	/*
	 * 更多信息：
	 * 
	 * LinkedBlockingDeque 类也提供了其他存取元素的方法，这些方法不会引起阻塞，而是抛出异常或返回 null。
	 * 
	 * takeFirst() 和 takeLast()：分别返回队列中第一个和最后一个元素，返回的元素会从队列中移除。如果队列为空，
	 * 调用方法的线程将被阻塞，直到队列中有可用的元素出现。
	 * 
	 * getFirst() 和 getLast()：分别返回队列中第一个和最后一个元素，返回的元素不会从队列中移除。如果队列为空，
	 * 则抛出 NoSuchElementException 异常。
	 * 
	 * peek()、peekFirst() 和 peekLast()：分别返回队列中第一个和最后一个元素，返回的元素不会从队列中移除。如果队列为空，则返回 null。
	 * 
	 * poll()、pollFirst() 和 pollLast()：分别返回队列中第一个和最后一个元素，返回的元素将会从队列中移除。如果队列为空，则返回 null。
	 * 
	 * add()、addFirst() 和 addLast()：分别将元素添加到队列中第一位和最后一位。如果队列已满（队列生成时指定了固定的容量），
	 * 这些方法将抛出 IllegalStateException 异常。
	 */
	public static void main(String[] args) {
		LinkedBlockingDeque<String> list = new LinkedBlockingDeque<String>(3);
		
		Client client = new Client(list);
		Thread thread = new Thread(client);
		thread.start();
		
		try {
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 3; j++) {
					/*
					 * 使用 take() 方法从队列中取出元素。如果队列为空，调用这个方法的线程将被阻塞，直到队列不为空（即，有可用的元素）。 
					 */
					String request = list.take();
					System.out.printf("Main: Request: %s at %s. Size: %d\n", request, new Date(), list.size());
				}
				
				TimeUnit.MILLISECONDS.sleep(300);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.printf("Main: End of the program. \n");
	}
	
}