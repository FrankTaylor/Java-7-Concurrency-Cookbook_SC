package chapter5;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * 如果任务需要解决的问题大于预先定义的大小，那么就要将这个问题拆分成多个子任务，并使用Fork/Join框架来执行这些任务。
 * 执行完成后，原始任务获取到由所有这些子任务产生的结果，合并这些结果，返回最终的结果。当原始任务在线程池中执行结束后，
 * 将高效地获取到整个问题的最终结果。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 下午02:50:29
 * @version 1.0
 */
public class Main_2 {
	
	/** 用来生成一个字符串矩阵来模拟一个文档。*/
	private static class Document {
		
		/** 用来生成字符串矩阵。*/
		private String words[] = {
				"the", "hello", "goodbye", "packt", "java", 
				"thread", "pool", "random", "class", "main"};
		
		/**
		 * 返回一个字符串矩阵。
		 * 
		 * @param numLines 行数
		 * @param numWords 每一行的词的个数
		 * @param word 准备查找的词
		 * @return String[][]
		 */
		public String[][] generateDocument (int numLines, int numWords, String word) {
			int counter = 0;
			String document[][] = new String[numLines][numWords];
			Random random = new Random();
			
			for (int i = 0; i < numLines; i++) {
				for (int j = 0; j < numWords; j++) {
					// 使用随机数取得数组words中的某一字符串，来填充字符串矩阵document对应的位置上。
					int index = random.nextInt(words.length);
					document[i][j] = words[index];
					/*
					 * 同时计算生成的字符串矩阵中将要查找的词的出现次数。这个值可以用来与后续程序运行查找任务时统计的次数相比较，
					 * 检查两个值是否相同。
					 */
					if (document[i][j].equals(word)) {
						counter++;
					}
				}
			}
			
			System.out.println("Document: The word appears " + counter + " times in the document");
			return document;
		}
	}
	
	/** 用来统计所要查找的词在一行中出现的次数。*/
	private static class LineTask extends RecursiveTask<Integer> {

		private static final long serialVersionUID = -7881524019521140952L;
		
		private String line[];
		private int start, end;
		private String word;
		
		public LineTask (String line[], int start, int end, String word) {
			this.line = line;
			this.start = start;
			this.end = end;
			this.word = word;
		}
		
		/** 统计单词(word)在数组(line)下标start到end之间所出现的次数。*/
		private Integer count (String[] line, int start, int end, String word) {
			int counter = 0;
			for (int i = start; i < end; i++) {
				if (line[i].equals(word)) {
					counter++;
				}
			}
			
			// 为延缓范例的执行，将任务休眠10毫秒。
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
			// 返回计数器的值。
			return counter;
		}
		
		@Override
		protected Integer compute () {
			int result = 0;
			
			// 如果end和start的差值小于100，那么将统计单词出现的次数。
			if ((end - start) < 100) {
				result = count(line, start, end, word);
			} else {
				
				// 否则，把统计任务进行进行拆分。
				int mid = (start + end) / 2;
			    LineTask task1 = new LineTask(line, start, mid, word);
			    LineTask task2 = new LineTask(line, mid, end, word);
			    
			    // 同步调用新创建的子任务。
			    invokeAll(task1, task2);
			    
			    // 将这两个任务返回的值相加。
			    try {
					result = (task1.get() + task2.get());
			    } catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			
			return  result;
		}
		
		
	}
	
	/** 用来计算所要查找的词在行中出现的次数。*/
	private static class DocumentTask extends RecursiveTask<Integer> {

		private static final long serialVersionUID = -7735280419882661212L;
		
		private String document[][];
		private int start, end;
		private String word;
		
		/** 构造函数。*/
		public DocumentTask (String document[][], int start, int end, String word) {
			this.document = document;
			this.start = start;
			this.end = end;
			this.word = word;
		}
		
		/** 统计单词(word)在文档(document)下标start到end之间所出现的次数。*/
		private Integer processLines (String[][] document, int start, int end, String word) {
			// 为任务所要处理的每一行，创建一个统计任务，然后保存在任务列表中。
			List<LineTask> tasks = new ArrayList<LineTask>();
			for (int i = start; i < end; i++) {
				LineTask task = new LineTask(document[i], 0, document[i].length, word);
				tasks.add(task);
			}
			
			// 同步执行统计任务。
			invokeAll(tasks);
			
			// 合计这些任务返回的值。
			int result = 0;
			for (int i = 0; i < tasks.size(); i++) {
				LineTask task = tasks.get(i);
				try {
					result = result + task.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			
			return new Integer(result);
		}
		
		@Override
		protected Integer compute() {
			int result = 0;
			// 如果end和start的差值小于10，那么将按行统计单词出现的次数。
			if ((end - start) < 10) {
				result = processLines(document, start, end, word);
			} else {
				// 否则，把统计任务进行进行拆分。
				int mid = (start + end) / 2;
				
				DocumentTask task1 = new DocumentTask(document, start, mid, word);
				DocumentTask task2 = new DocumentTask(document, mid, end, word);
	
				// 同步调用新创建的子任务。
				invokeAll(task1, task2);
				
				// 将这两个任务返回的值相加。
				try {
					result = (task1.get() + task2.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			
			return result;
		}
	}
	
	public static void main(String[] args) {
		
		// 创建一个 "100 * 1000" 的文档矩阵。
		Document document = new Document();
		String[][] documentArray = document.generateDocument(100, 1000, "the");
		
		// 创建一个任务来统计整个文档。
		DocumentTask task = new DocumentTask(documentArray, 0, 100, "the");
		
		ForkJoinPool pool = new ForkJoinPool();
		pool.execute(task);
		
		// 每1秒在控制台上输出线程池的一些信息，直到任务执行结束。
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
			
		} while (!task.isDone());
		
		// 关闭线程池。
        pool.shutdown();
        
        // 等待任务执行结束。
        try {
        	pool.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) { e.printStackTrace(); }
        
        // 在控制台输出文档中出现要查找的词的次数。
        try {
			System.out.printf("Main: The word appears %d in the document\n", task.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	
}