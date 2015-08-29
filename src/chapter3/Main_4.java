package chapter3;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Main_4 {
	
	/** 创建一个矩阵类 MatrixMock，用来生成一个 1~10组成的随机矩阵，线程将从这个矩阵中查找指定的数字。*/
	private static class MatrixMock {
		
		private int data[][];

        public MatrixMock(int size, int length, int number) {
        	// 记录随机生成的数字与要查询数字一致的次数。
            int counter = 0;
            
            data = new int[size][length];
            
            /*
             * 用随机数字为矩阵赋值。没生成一个数字，就用它跟要查找的数字进行比较。如果一致，就将计数器 counter 加 1。
             */
            Random random = new Random();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < length; j++) {
                    data[i][j] = random.nextInt(10);
                    if (data[i][j] == number) {
                        counter++;
                    }
                }
            }
            
            // 在控制台显示随机生成的数字与要查询数字一致的次数。
            System.out.printf("Mock: There are %d ocurrences of number in generated data.\n", counter, number);
        }
        
        /** 返回二维矩阵中，下标是 row 的一维数组。*/
        public int[] getRow(int row) {
            if ((row >= 0) && (row < data.length)) {
                return data[row];
            }
            return null;
        }
	}
	
	/** 用 Results 类保存在矩阵中每行找到指定数字的次数。*/
    private static class Results {
        private int data[];

        public Results(int size) {
            this.data = new int[size];
        }

        public void setData(int position, int value) {
            data[position] = value;
        }

        public int[] getData() {
            return data;
        }
    }
    
    /** 实现任务类 Searcher 。它在随机数矩阵指定的行中查找某个数。*/
    private static class Searcher implements Runnable {
        
    	// firstRow 和 lastRow 决定查找的自己范围。
    	private int firstRow;
        private int lastRow;
        
        private MatrixMock mock;
        private Results results;
        
        // 用于存放要查找的数字。
        private int number;
        
        private final CyclicBarrier barrier;

        public Searcher(int firstRow, int lastRow, MatrixMock mock, Results results, int number, CyclicBarrier barrier) {
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.mock = mock;
            this.results = results;
            this.number = number;
            this.barrier = barrier;
        }

        @Override
        public void run() {
        	
        	// 用来保存每行查找到的次数。
            int counter;
            
            // 将查找范围打印到控制台。
            System.out.printf("%s: Processing lines from %d to %d.\n", Thread.currentThread().getName(), firstRow, lastRow);
            
            // 根据一定的范围，在二维矩阵中，对指定的数字进行查找。并将查找到的次数保存到Results对象的相应位置上。
            for (int i = firstRow; i < lastRow; i++) {
                int row[] = mock.getRow(i);
                counter = 0;
                for (int j = 0; j < row.length; j++) {
                    if (row[j] == number) {
                        counter++;
                    }
                }
                results.setData(i, counter);
            }

            System.out.printf("%s: Lines processed.\n", Thread.currentThread().getName());

            try {
            	/*
            	 * 当 CyclicBarrier 对象内部的计数器没有累计到，在构造器中指定的数值时，调用 CyclicBarrier#await() 方法将阻塞当前线程。
            	 * 
            	 * 例如：当 new CyclicBarrier(10); 时，内部的计数器从 0 开始（注意：CountDownLatch 内部计数器是从构造器指定的值开始）。
            	 * 
            	 * 每当某一线程调用 CyclicBarrier#await() 方法时将发生以下行为：
            	 * 1、该对象内部计数器的值加1（注意：CountDownLatch#countDown()是内部计数器减1）；
            	 * 2、判断当前计数器的值与构造器指定的值是否相同。
            	 *   不同：阻塞当前线程的执行；
            	 *   相同：1、所有因为调用 CyclicBarrier#await() 方法而阻塞的线程同时执行（注意：CountDownLatch是内部计数器为 0 时才同时执行）；
            	 *       2、内部计数器重新置为 0，当再次执行 await() 方法时，内部计数器还可以加1，所以该对象可以重复利用（注意： CountDownLath 不能重复利用）。
            	 * 
            	 * CyclicBarrier#await() 方法，还有另一个版本：CyclicBarrier#await(10, TimeUnit.SECONDS)。该方法与 await() 方法的不同在于，
            	 * 如果该线程阻塞的时长，到了指定的时间后，还未被执行，将抛出 TimeoutException。
            	 * 注意：CountDownLatch#countDown(1, TimeUnit.SECONDS) 方法在到达指定时间后是不抛出异常的。
            	 * 
            	 * 注意：综上所述，CyclicBarrier 和 CountDownLatch 有很多共性，但也有很多差异。
            	 * 其中最不同的是，CyclicBarrier 对象可以调用 CyclicBarrier#reset() 方法，重置该对象回初始状态，并把它内部计数器重置为初始时的值。
            	 * 当重置发生后，所有在因为 await() 方法而阻塞的线程将收到一个 BrokenBarrierException 异常。
            	 * 
            	 * CyclicBarrier 对象有一种特殊的状态——即损坏状态（Broken）。当很多线程在 await() 方法上等待时，如果其中一个线程被中断，则该线程将抛出 
            	 * InterruptedException 异常，而其他的等待线程将抛出 BrokenBarrierException 异常，于是 CyclicBarrier 对象就处于 Broken 状态了。
            	 * 
            	 * CyclicBarrier 类提供了 isBroken() 方法，如果该对象处于损坏状态就返回 true，否则就返回 false。
            	 */
            	barrier.await();
            	
            	/*
            	 * CyclicBarrier#getNumberWaiting() 方法，返回在await()方法上阻塞的线程数目。
            	 * CyclicBarrier#getParties() 方法，返回被 CyclicBarrier 对象同步的任务数。
            	 */
            	System.out.printf("在 await() 方法上阻塞的线程数 = %d; 被 CyclicBarrier 对象同步的任务数 = %d.\n", barrier.getNumberWaiting(), barrier.getParties()); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
    
    /** 创建 Grouper 任务，来统计在矩阵中查找到指定数字的总次数。*/
    private static class Grouper implements Runnable {
        
    	private Results results;
        
        public Grouper(Results results) {
            this.results = results;
        }

        @Override
        public void run() {
        	System.out.printf("Grouper: Processing results...\n");
            
        	// 对找到的次数进行统计。
        	int finalResult = 0;
            int data[] = results.getData();
            for (int number : data) {
                finalResult += number;
            }

            System.out.printf("Grouper: Total result: %d.\n", finalResult);
        }
    }

    public static void main(String[] args) {
        // 二维矩阵的行数和每行的列数。
    	final int ROWS = 10000;
        final int NUMBERS = 1000;
        
        // 需要查询的数字。
        final int SEARCH = 5;
        
        // 构建查询任务的数量。
        final int PARTICIPANTS = 5;
        final int LINES_PARTICIPANT = 2000;
        
        // 构造二维矩阵对象。
        MatrixMock mock = new MatrixMock(ROWS, NUMBERS, SEARCH);
        // 构造查询结果对象。
        Results results = new Results(ROWS);
        // 构造统计任务。
        Grouper grouper = new Grouper(results);
        
        // 构造 CyclicBarrier 对象，该对象内部计数器设置为 5，当因为 await() 方法而阻塞的线程执行后，该对象会创建线程来执行 Grouper 统计任务。
        CyclicBarrier barrier = new CyclicBarrier(PARTICIPANTS, grouper);
        
        // 创建 5 个搜索任务。
        Searcher searchers[] = new Searcher[PARTICIPANTS];
        for (int i = 0; i < PARTICIPANTS; i++) {
        	/*
        	 * 二维矩阵的行数是 10000 行。
        	 * 第一个 Searcher 任务的搜索范围是：[0-2000]
        	 * 第二个 Searcher 任务的搜索范围是：[2000-4000]
        	 * 第三个 Searcher 任务的搜索范围是：[4000-6000]
        	 * 第四个 Searcher 任务的搜索范围是：[6000-8000]
        	 * 第五个 Searcher 任务的搜索范围是：[8000-10000]
        	 * 
        	 */
        	searchers[i] = new Searcher(i * LINES_PARTICIPANT, (i * LINES_PARTICIPANT) + LINES_PARTICIPANT, mock, results, SEARCH, barrier);
            Thread thread = new Thread(searchers[i]);
            thread.start();
        }
        
        System.out.printf("Main: The main thread has finished.\n");
    	
    }

}
