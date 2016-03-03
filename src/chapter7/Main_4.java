package chapter7;

import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 
 * 定时线程池（Scheduled Thread Pool） 是 Executor 框架中基本线程池的扩展，它允许在一段时间后定时执行任务。
 * ScheduledThreadPoolExecutor 类不仅实现了这个功能，还允许执行下列两类任务。
 * 1、延迟任务（Delayed Task）：这类任务在一段时间后仅执行一次。
 * 2、周期性任务（Periodic Task）：这类任务在一段延迟时间后周期性地执行。
 * 
 * “延迟任务” 能够执行 Runnable 和 Callable 这两类任务，而 “周期任务” 仅能执行 Runnable 任务。所有由定时线
 * 程池执行的任务都必须实现 RunnableScheduledFuture 接口。
 * 
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_4 {
	
	private static class MyScheduledTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {
		
		private RunnableScheduledFuture<V> task;
		private ScheduledThreadPoolExecutor executor;
		private long period;
		private long startDate;
		
		public MyScheduledTask(
				Runnable runnable, V result, 
				RunnableScheduledFuture<V> task, ScheduledThreadPoolExecutor executor) {
			super(runnable, result);
			this.task = task;
			this.executor = executor;
		}
		
		@Override
		public boolean isPeriodic() {
			return task.isPeriodic();
		}
		
		@Override
		public int compareTo(Delayed o) {
			return task.compareTo(o);
		}
		
		@Override
		public long getDelay(TimeUnit unit) {
			if (!isPeriodic()) {
				return task.getDelay(unit);
			} else {
				if (startDate == 0) {
					return task.getDelay(unit);
				} else {
					Date now = new Date();
					long delay = startDate - now.getTime();
					return unit.convert(delay, TimeUnit.MILLISECONDS);
				}
			}
		}
		
		@Override
		public void run() {
			if (isPeriodic() && (!executor.isShutdown())) {
				Date now = new Date();
				startDate = now.getTime() + period;
				executor.getQueue().add(this);
			}
			
			System.out.printf("Pre-MyScheduledTask: %s\n", new Date());
			System.out.printf("MyScheduledTask: Is periodic: %s\n", isPeriodic());
			
			super.runAndReset();
			
			System.out.printf("Post-MyScheduledTask: %s\n", new Date());
		}
		
		public void setPeriod(long period) {
			this.period = period;
		}
	}
	
	private static class MyScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
		
		public MyScheduledThreadPoolExecutor(int corePoolSize) {
			super(corePoolSize);
		}
		
		@Override
		protected <V> RunnableScheduledFuture<V> decorateTask(
				Runnable runnable, RunnableScheduledFuture<V> task) {
			
			MyScheduledTask<V> myTask = new MyScheduledTask<V>(runnable, null, task,  this);
			return myTask;
		}
		
		@Override
		public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
			
			ScheduledFuture<?> task = super.scheduleAtFixedRate(command, initialDelay, period, unit);
			MyScheduledTask<?> myTask = (MyScheduledTask<?>)task;
			myTask.setPeriod(TimeUnit.MILLISECONDS.convert(period, unit));
			return task;
		}
	}
	
	private static class Task implements Runnable {
		
		@Override
		public void run() {
			System.out.printf("Task: Begin.\n");
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.printf("Task: End.\n");
		}
	}
	
	/*
	 * 工作原理：
	 * 
	 * MyScheduledTask 继承了 FutureTask 类，并实现了 RunnableScheduledFuture 接口。之所以实现该接口，
	 * 是因为所有在定时执行器中执行的任务都必须实现该接口；之所以继承 FutureTask 类是因为该类提供了 RunnableScheduledFuture
	 * 接口中声明方法的实现。
	 * 
	 * MyScheduledThreadPoolExecutor 继承 ScheduledThreadPoolExecutor，并覆盖了 decorateTask() 方法，
	 * 因为该方法默认返回的是 ScheduledThreadPoolExecutor 实现的缺省定时任务。覆盖该方法后，将返回 MyScheduledTask 任务。
	 * 所以，当实现自定义的定时任务时，必须实现自定义的定时任务执行器。
	 * 
	 * 本例中 decorateTask() 方法使用入参来创建 MyScheduledTask 对象，传入的 Runnable 对象将在任务中执行，执行结果也将被
	 * 任务返回。由于任务不会返回一个结果，因此使用 null 作为返回值。
	 * 
	 * MyScheduledTask 类既可以执行延迟任务，也可以执行周期性任务。因为已经实现了 getDelay() 和 run() 方法，它们具有执行这两种
	 * 任务的所必需的逻辑。
	 * 
	 * 对于 getDelay() 方法，定时执行器调用它，来确定是否需要执行一个任务。这个方法在 “延迟任务” 和 “周期任务” 中表现不一样。像之前提到的，
	 * MyScheduledTask 构造器接收的是 RunnableScheduledFuture 默认实现的对象，用来执行传入的 Runnable 对象，并把它保存为
	 * MyScheduledTask 类的属性，以供 MyScheduledTask 类的其他方法和数据进行访问。
	 * 
	 * 如果执行的是一个 “延迟任务” getDelay() 方法返回传入任务的延迟值，如果执行的是 “周期任务” getDelay() 方法返回 startDate 
	 * 属性与当前时间的时间差。
	 * 
	 * 周期性任务的一个特殊性是，它必须添加到执行器的队列中作为新任务，才能被再次执行。所以，如果正在执行一个周期性任务，就需用 “当前时间” + “任务执行周期”
	 * 的和，重置 startDate 属性，并将这个任务再次加入到执行器队列中。接下来，使用 FutureTask 类提供的 runAndReset() 方法来执行。而在延迟任务中，
	 * 就不需要再次把任务放入到执行器队列中了，因为它仅执行一次。
	 * 
	 * 注意：在执行器已关闭的情况下，周期性任务将不会再加入到执行器队列中。
	 * 
	 * 最后，在 MyScheduledThreadPoolExecutor 中，还覆盖了 scheduleAtFixedRate() 方法。如前所述，对于周期性任务，startDate 属性
	 * 必须被重置，而这还需用到任务的周期，由于没有初始它，因此必须覆盖  scheduleAtFixedRate() 方法，因为它接收任务的周期值，并传入到 MyScheduledTask 中。
	 * 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		MyScheduledThreadPoolExecutor executor = new MyScheduledThreadPoolExecutor(2);
		Task task = new Task();
		System.out.printf("Main: %s\n", new Date());
		
		executor.schedule(task, 1, TimeUnit.SECONDS);
		
		TimeUnit.SECONDS.sleep(3);
		
		task = new Task();
		
		System.out.printf("Main: %s\n", new Date());
		
		executor.scheduleAtFixedRate(task, 1, 3, TimeUnit.SECONDS);
		
		TimeUnit.SECONDS.sleep(10);
		
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.DAYS);
		
		System.out.printf("Main: End of the program.\n");
	}
}