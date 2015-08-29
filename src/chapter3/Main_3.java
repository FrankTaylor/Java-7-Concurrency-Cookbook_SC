package chapter3;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main_3 {
	
	private static class Videoconference implements Runnable {
		
		private final CountDownLatch controller;

        public Videoconference(int number) {
        	
        	/*
        	 * CountDownLatch 对象的内部计数器被初始化后，就不能被再次初始化或者修改。此时，唯一能改变计数器值的方法是 CountDownLatch#countDown() 方法。
        	 * 
        	 * 注意1：CountDownLatch 不是用来保护共享资源或临界区的，它是用来让多个线程同步执行一个或多个任务的。
        	 * 注意2：CountDownLatch 是一次性消费的，一旦计数器为 0 后，就必须重新构造一个对象。
        	 */
            controller = new CountDownLatch(number);
        }
        
        /** 模拟每一个与会者进入视频会议。*/
        public void arrive(String name) {
        	// 打印出与会者到达的信息。
            System.out.printf("%s has arrived.", name);
            
            /*
             * 当 CountDownLatch#countDown() 方法被调用后，计数器将减1。当计数器到达 0 的时候，CountDownLatch 对象将唤起所有在
             * await() 方法上等待的线程。此时，再执行 countDown() 方法将不起任何作用（不会抛出异常，内部计数器将一直为0）。
             */
            controller.countDown();
            System.out.printf("VideoConference: Waiting for %d participants.\n", controller.getCount());
        }

        @Override
        public void run() {
            System.out.printf("VideoConference: Initialization: %d participants.\n", controller.getCount());
            try {
            	/*
            	 * 当计数器不为 0 时，如果调用 CountDownLatch#await() 方法将会阻塞当前线程。
            	 * 
            	 * 该方法还有一个另一个版本：CountDownLatch#await(long time, TimeUnit unit)。
            	 * 当这个版本的方法被调用后，会一直阻塞到（1） 计数器到0；（2）或指定的时间已过期；（3）或当前线程中断。
            	 * 
            	 * 注意：当到达指定的时间后，该方法并不会抛出 InterruptedException 异常，而是继续执行。
            	 */
                controller.await();
                
                // 用来模拟所有与会者都到齐后，开始会议的信息。
                System.out.printf("VideoConference: All the participants have come\n");
                System.out.printf("VideoConference: Let's start...\n");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
	}
	
	/** 用来模拟参与会议的与会者。*/
    private static class Participant implements Runnable {
        
    	private Videoconference conference;
        private String name;

        public Participant(Videoconference conference, String name) {
            this.conference = conference;
            this.name = name;
        }

        @Override
        public void run() {
        	
        	// 线程随机休眠一段时间。
            long duration = (long)(Math.random() * 10);
            try {
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            conference.arrive(name);
        }
    }
    
    public static void main(String[] args) {
    	// 创建视频会议任务，它将等待10个与会者到齐。
    	Videoconference conference = new Videoconference(10);
    	
    	// 创建一个线程来执行视频会议任务。
    	Thread threadConference = new Thread(conference);
    	threadConference.start();
    	
    	// 创建 10个与会者任务，并用线程驱动。
    	for (int i = 0; i < 10; i++) {
    		Participant p = new Participant(conference, "Participant " + i);
    		Thread t = new Thread(p);
    		t.start();
    	}
    }

}
