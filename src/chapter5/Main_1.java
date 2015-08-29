package chapter5;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * 如果一个应用能被分解成多个子任务，并且组合多个子任务的结果就能够获得最终的答案，那么这个应用就适合用 Fork/Join 模式来解决。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 上午11:58:29
 * @version 1.0
 */
public class Main_1 {
	
	/** 构建一个商品类。 */
	private static class Product {
		private String name;
		private double price;
		
		// --- get and set method
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public double getPrice() {
			return price;
		}
		public void setPrice(double price) {
			this.price = price;
		}
	}
	
	/** 用来生成一个随机产品列表。*/
	private static class ProductListGenerator {
		
		public List<Product> generate (int size) {
			List<Product> ret = new ArrayList<Product>();
			
			// 给所有的产品都分配相同的价格。
			for (int i = 0; i < size; i++) {
				Product product = new Product();
				product.setName("Product " + i);
				product.setPrice(10);
				
				ret.add(product);
			}
			
			return ret;
		}
		
	}
	
	/*
	 * recursive
	 * /rɪ'kɝsɪv/ 
	 * adj. 回归的,递归的
	 */
	/** 用于任务没有返回结果的场景。*/
	private static class Task extends RecursiveAction {

		private static final long serialVersionUID = 5916163458058732831L;
		
		/** 用来保存商品。*/
		private List<Product> products;
		
		/** 这两个属性将决定任务执行时，如何拆分子任务。*/
		private int first;
		private int last;
		
		/** 用来存储产品价格的增加额。*/
        private double increment;
        
        /** 构造器。*/
        public Task (List<Product> products, int first, int last, double increment) {
            this.products = products;
            this.first = first;
            this.last = last;
            this.increment = increment;
        }
        
        /** 实现任务的执行逻辑。*/
		@Override
		protected void compute() {
			
			// 如果last和first的差值小于10，则增加产品的价格（用以模拟一个任务只能更新少于10件产品的价格）。
		    if ((last-first) < 10) {
                updatePrices();
            } else {
            	
            	/*
            	 * 如果last和first的差值大于等于10，就创建两个新的Task任务，
            	 * 一个处理前一半的产品，另一个处理后一半的产品，然后调用ForkJoinPool的
            	 * invokeAll()方法，来执行这两个新的任务。
            	 */
                int middle = (last + first) >>> 1;
                System.out.printf("Task: Pending tasks: %s\n", getQueuedTaskCount());

                Task t1 = new Task(products, first, (middle + 1), increment);
                Task t2 = new Task(products, (middle + 1), last, increment);
                
                /*
                 * 该方法用来执行一个主任务中所创建的多个子任务。这是一个同步调用，这个任务将等待子任务的完成，然后继续执行（也可能是结束）。
                 * 当一个主任务等待他的子任务时，执行这个主任务的工作线程可接收另一个等待执行的任务并开始执行（线程复用）。
                 * 
                 * 正因为有了这个行为，所以说Fork/Join框架提供了一种比Runnable和Callable对象更加高效的任务管理机制。
                 */
                invokeAll(t1, t2);
            }	
		}
		
		/** 用来更新产品的价格。*/
        private void updatePrices () {
            for (int i  = first; i < last; i++) {
                Product product = products.get(i);
                product.setPrice(product.getPrice() * (1 + increment));
            }
        }
		
	}

    public static void main (String[] args) {
    	
    	// 使用 ProductListGenerator 创建一个有10,000个产品的列表。
        ProductListGenerator generator = new ProductListGenerator();
        List<Product> products = generator.generate(10000);

        // 创建一个任务来更新列表中的所有产品。
        Task task = new Task(products, 0, products.size(), 0.20);
        
        // 执行任务。
        ForkJoinPool pool = new ForkJoinPool();
        pool.execute(task);
        
        // 每5毫秒在控制台上输出线程池的一些信息，直到任务执行结束。
        do {
            System.out.printf("Main: Thread Count: %d\n", pool.getActiveThreadCount());
            System.out.printf("Main: Thread Steal: %d\n", pool.getStealCount());
            System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());

            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace(); 
            }
        } while (!task.isDone());
        
        // 关闭线程池。
        pool.shutdown();
        
        // 检查任务是否已经完成，且没有错误。如果是，则在控制台输出信息。
        if (task.isCompletedNormally()) {
            System.out.printf("Main: The process has completed normally.\n"); 
        }
        
        /*
         * 在任务执行后，所有产品的期望价格是12元。在控制输出所有产品的名称和价格，
         * 如果产品的价格不是12元，就将产品信息打印出来，以便确认所有的产品价格都正
         * 确地增加了。
         */
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);

            if (product.getPrice() != 12) {
                System.out.printf("产品 %s: %f\n", product.getName(), product.getPrice()); 
            }
        }
        
        // 在控制台输出信息表示程序执行结束。
        System.out.println("Main: End of the program.\n");

    }
}
