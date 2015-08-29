package chapter5;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

/**
 * 要提高应用程序在多核处理器上的执行效率，只能想办法提高应用程序的本身的并行能力。常规的做法就是使用多线程，
 * 让更多的任务同时处理，或者让一部分操作异步执行，这种简单的多线程处理方式在处理器核心数比较少的情况下能够有
 * 效地利用处理资源，因为在处理器核心比较少的情况下，让不多的几个任务并行执行即可。但是当处理器核心数发展很大
 * 的数目，上百上千的时候，这种按任务的并发处理方法也不能充分利用处理资源，因为一般的应用程序没有那么多的并发
 * 处理任务（服务器程序是个例外）。所以，只能考虑把一个任务拆分为多个单元，每个单元分别得执行最后合并每个单元
 * 的结果。一个任务的并行拆分，一种方法就是寄希望于硬件平台或者操作系统，但是目前这个领域还没有很好的成果。另
 * 一种方案就是还是只有依靠应用程序本身对任务经行拆封执行。
 * 
 * 依靠应用程序本身并行拆封任务，如果使用简单的多线程程序的方法，复杂度必然很大。这就需要一个更好的范式或者工
 * 具来代程序员处理这类问题。Java 7也意识到了这个问题，才标准库中集成了由Doug Lea开发的Fork/Join并
 * 行计算框架。通过使用 Fork/Join 模式，软件开发人员能够方便地利用多核平台的计算能力。尽管还没有做到对软
 * 件开发人员完全透明，Fork/Join 模式已经极大地简化了编写并发程序的琐碎工作。对于符合 Fork/Join 模式
 * 的应用，软件开发人员不再需要处理各种并行相关事务，例如同步、通信等，以难以调试而闻名的死锁和 data race 
 * 等错误也就不会出现，提升了思考问题的层次。你可以把 Fork/Join 模式看作并行版本的 Divide and Conquer
 * 策略，仅仅关注如何划分任务和组合中间结果，将剩下的事情丢给 Fork/Join 框架。但是Fork/Join并行计算框架，
 * 并不是银弹，并不能解决所有应用程序在超多核心处理器上的并发问题。
 * 
 * 如果一个应用能被分解成多个子任务，并且组合多个子任务的结果就能够获得最终的答案，那么这个应用就适合用 Fork/Join 模式来解决。
 * 
 * 这段代码中，定义了一个累加的任务，在compute方法中，判断当前的计算范围是否小于一个值，如果是则计算，如果没有，
 * 就把任务拆分为两个子任务，并合并连个子任务的中间结果。程序递归的完成了任务拆分和计算。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 下午12:36:33
 * @version 1.0
 */
public class Calculator extends RecursiveTask<Integer> {

	private static final long serialVersionUID = -626146704381452020L;
	
	private static final int THRESHOLD = 100;
	private int start;
	private int end;
	
	public Calculator (int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	@Override
	protected Integer compute() {
		int sum = 0;
		if ((start - end) < THRESHOLD) {
			for (int i = start; i < end; i++) {
				sum += i;
			}
		} else {
			
			int middle = (start + end) / 2;
			
			Calculator left = new Calculator(start, middle);
			Calculator right = new Calculator(middle + 1, end);
			
			/*
			 * 按照调用fork()的顺序执行两个子任务对象的join()方法。其实，这样就有一个问题，
			 * 在每次迭代中，第一个子任务会被放到线程队列的倒数第二个位置，第二个子任务是最后一
			 * 个位置。当执行join()调用的时候，由于第一个子任务不在队列尾而不能通过执行
			 * ForkJoinWorkerThread的unpushTask()方法取出任务并执行，线程最终只能
			 * 挂起阻塞，等待通知。而Fork Join本来的做法是想通过子任务的合理划分，避免过多的
			 * 阻塞情况出现。这样，这个例子中的操作就违背了Fork Join的初衷，每次子任务的迭代，
			 * 线程都会因为第一个子任务的join()而阻塞，加大了代码运行的成本，提高了资源开销，不
			 * 利于提高程序性能。
			 * 
			 * 除此之外，这段程序还是不能进入Fork Join的过程，因为还有一个低级错误。看下第49、
			 * 50行代码的条件，就清楚了。按照逻辑，start必然是比end小的。这将导致所有任务都将以
			 * 循环累加的方式完成，而不会执行fork()和join()。
			 */
			left.fork();
			right.fork();
			
			sum = left.join() + right.join();
		}
		return sum;
	}
	
	public static void main(String[] args) {
		ForkJoinPool pool = new ForkJoinPool();
		Future<Integer> result = pool.submit(new Calculator(0, 10000));
		
		try {
			System.out.println(result.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		System.out.println((5 + 5) >>> 1);
	}
}