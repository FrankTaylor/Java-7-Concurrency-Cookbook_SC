package chapter4;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main_11 {
	
	private static class ReportGenerator implements Callable<String> {
		
		private String sender;
        private String title;

        public ReportGenerator(String sender, String title) {
            this.sender = sender;
            this.title = title;
        }
		
		@Override
		public String call() throws Exception {
            try {
                long duration = (long)(Math.random() * 10);
                System.out.printf("%s_%s: ReportGenerator: Generating a report during %d seconds\n", this.sender,this.title, duration);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

			return sender + ": " + title;
		}
	}

    private static class ReportRequest implements Runnable {

        private String name;
        private CompletionService<String> service;

        public ReportRequest(String name, CompletionService<String> service) {
            this.name = name;
            this.service = service;
        }

        @Override
        public void run() {
        	/*
        	 * 创建任务，然后通过 CompletionService#submit() 发送给执行器。
        	 */
            ReportGenerator reportGenerator = new ReportGenerator(name, "Report");
            service.submit(reportGenerator);
        }
    }
    
    /** 该任务类将用来获得，ReportGenerator 任务的执行结果。*/
    private static class ReportProcessor implements Runnable {

        private CompletionService<String> service;
        private boolean end = false;

        public ReportProcessor (CompletionService<String> service) {
            this.service = service;
        }

        @Override
        public void run() {
            while (!end) {
                try {
                	
                	/*
                	 * CompletionService#pool() 方法将查看任务队列中是否有已完成的任务，如果有，一个接一个的返回，
                	 * 通过 CompletionService 完成任务的Future对象。当 poll() 方法返回 Future 对象后，它将从队列中删除这个Future对象。
                	 * 
                	 * service.poll(20, TimeUnit.SECONDS) 该方法会阻塞当前线程的执行，直到指定的时间，如果超时，就返回 null。
                	 * 还有一个无参数版的 poll() 方法，如果当前还没有已完成的任务，就立即返回 null。所以，该方法并不会像 get() 方法一样会
                	 * 一直阻塞当前线程。
                	 * 
                	 * CompletionService#take() 方法也没有参数，它会检查队列中是否有Future对象。如果队列为空，它将阻塞线程直到队列中有可用的元素。
                	 * 如果队列中元素，它将返回队列中的第一个元素，并移除这个元素。所以，该方法的行为与 get() 差不多。
                	 * 
                	 */
                    Future<String> result = service.poll();
                    if (result != null) {
                    	// 调用 Future#get()来获取任务的执行结果。
                        String report = result.get();
                        System.out.printf("ReportReceiver: Report Received: %s\n", report);
                    } 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            System.out.printf("ReportSender: End\n");
        }

        public void setEnd(boolean end) {
            this.end = end;
        }
    }

    public static void main(String[] args) {
    	
    	// 创建 CompletionService 对象，注意构造器参数使用的是上一步创建的 executor 对象。
        ExecutorService executor = Executors.newCachedThreadPool();
        /*
         * CompletionService 类可以执行 Callable 或 Runnable 类型的任务。
         */
        CompletionService<String> service = new ExecutorCompletionService<String>(executor);
        
        // 创建 ReportProcessor 任务。
        ReportProcessor processor = new ReportProcessor(service);
        // 创建线程执行 ReportProcessor 任务。
        Thread senderThread = new Thread(processor);
        
        /*
         * 创建 ReportRequest 任务。
         * 注意：该任务的主要目的是，创建一个  ReportGenerator 任务，并交由 CompletionService 执行器来执行。
         */
        ReportRequest faceRequest = new ReportRequest("Face", service);
        ReportRequest onlineRequest = new ReportRequest("Online", service);
        // 创建线程执行 ReportRequest 任务。
        Thread faceThread = new Thread(faceRequest);
        Thread onlineThread = new Thread(onlineRequest);

        // 启动这三个线程。
        System.out.printf("Main: Starting the Threads\n");
        faceThread.start();
        onlineThread.start();
        
        senderThread.start();
        
        // 阻塞当前的主线程，等待所有 ReportRequest 任务的结束，以保证 ReportGenerator 创建任务，然后通过 CompletionService#submit() 发送给执行器。
        try {
            System.out.printf("Main: Waiting for the report generators. \n");
            faceThread.join();
            onlineThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.printf("Main: Shutting down the executor. \n");
        executor.shutdown();
        
        // 等待所有的线程结束。
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 通过标志位来结束 ReportSender 任务的执行。
        processor.setEnd(true);
        System.out.println("Main: Ends");
    }
}
