package chapter5;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * 当你在ForkJoinPool中执行ForkJoinTask时，你可以使用同步或异步方式来实现。当你使用同步方式时，
 * 提交任务给池的方法直到提交的任务完成它的执行，才会返回结果。当你使用异步方式时，提交任务给执行者的方法
 * 将立即返回，所以这个任务可以继续执行。
 * 
 * 你应该意识到这两个方法有很大的区别，当你使用同步方法，调用这些方法（比如：invokeAll()方法）的任务
 * 将被阻塞，直到提交给池的任务完成它的执行。这允许ForkJoinPool类使用work-stealing算法，分配
 * 一个新的任务给正在执行睡眠任务的工作线程。反之，当你使用异步方法（比如：fork()方法），这个任务将继续
 * 它的执行，所以ForkJoinPool类不能使用work-stealing算法来提高应用程序的性能。在这种情况下，只
 * 有当你调用join()或get()方法来等待任务的完成时，ForkJoinPool才能使用work-stealing算法。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 上午09:35:33
 * @version 1.0
 */
public class Main_3 {
	
	/** 用来处理文件夹。*/
	private static class FolderProcessor extends RecursiveTask<List<String>> {

		private static final long serialVersionUID = -536157197673565710L;
		
		/** 保存文件夹路径。*/
		private String path;
		/** 保存将要查找的文件扩展名。*/
		private String extension;
		
		/** 构造函数。*/
		public FolderProcessor (String path, String extension) {
			this.path = path;
			this.extension = extension;
		}
		
		@Override
		protected List<String> compute () {
			// 用来存储文件夹中的文件名称。
			List<String> list = new ArrayList<String>();
			// 用来处理文件夹中子文件夹的任务列表。
			List<FolderProcessor> tasks = new ArrayList<FolderProcessor>();
			
			// 获取文件夹的内容。
			File file = new File(path);
			File content[] = file.listFiles();
			
			/*
			 * 如果文件夹中的每一个元素是文件夹，就创建一个文件夹处理任务，然后用fork()方法采用一部方式来执行它。 
			 */
			if (content != null) {
				for (int i = 0; i < content.length; i++) {
					if (content[i].isDirectory()) {
						FolderProcessor task = new FolderProcessor(content[i].getAbsolutePath(), extension);
						
						/*
						 * fork()方法将任务提交到线程池中，如果池中有空闲的工作线程，或创建一个新的工作线程，那么开始执行这个任务。
						 * 由于fork()方法是异步的，因此，主任务可以继续处理文件中的其他内容（此时不能启用work-stealing算法）。
						 */
						task.fork();
						tasks.add(task);
					} else {
						// 否则，就将该文件的扩展名与将要搜索的扩展名相比较，如果一直就把该文件的完整路径放入列表。
						if (content[i].getName().endsWith(extension)) {
							list.add(content[i].getAbsolutePath());
						}
					}
				}
				
				// 如果子任务列表超过50个元素，就在控制台输出一条信息。
				if (tasks.size() > 50) {
					System.out.printf("%s: %d tasks ran. \n", file.getAbsoluteFile(), tasks.size());
				}
				
				// 遍历任务列表中存储的每一个任务，调用join()方法等待任务执行结束，并且返回任务的结果。然后，把结果保存到列表中。
				for (FolderProcessor item : tasks) {
					/*
					 * 一旦主任务处理完指定文件夹里的所有内容，它将调用join()方法等待发送到线程池中的所有子任务执行完成。
					 * （此时将启用work-stealing算法）
					 */
					list.addAll(item.join());
				}
			}
			return list;
		}

	}
	
	public static void main(String[] args) {
		ForkJoinPool pool = new ForkJoinPool();
		
		FolderProcessor system = new FolderProcessor("C:\\Windows", "log");
		FolderProcessor apps = new FolderProcessor("C:\\Program Files", "log");
		FolderProcessor documents = new FolderProcessor("C:\\Documents And Settings", "log");
		
		pool.execute(system);
		pool.execute(apps);
		pool.execute(documents);
		
		// 每1秒在控制台上输出线程池的一些信息，直到这三个任务执行结束。
		do {
			System.out.println("**************************************");
			System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
			System.out.printf("Main: Active Threads: %d\n", pool.getActiveThreadCount());
			System.out.printf("Main: Task Count: %d\n", pool.getQueuedTaskCount());
			System.out.printf("Main: Steal Count: %d\n", pool.getStealCount());
			System.out.println("**************************************");
			
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
		} while ((!system.isDone()) || (!apps.isDone()) || (!documents.isDone()));
		
		pool.shutdown();
		
		List<String> results = system.join();
		System.out.printf("System: %d files found. \n", results.size());
		
		results = apps.join();
		System.out.printf("Apps: %d files found. \n", results.size());
		
		results = documents.join();
		System.out.printf("Documents: %d files found. \n", results.size());
	}
}