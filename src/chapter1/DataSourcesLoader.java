package chapter1;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DataSourcesLoader implements Runnable {

	@Override
	public void run() {
        System.out.printf("Beginning data sources loading: %s\n", new Date());
        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("Data sources loading has finished: %s\n", new Date());
	}

    private class NetworkConnectionsLoader implements Runnable {

        @Override
        public void run() {
            System.out.printf("Beginning network connections loading: %s\n", new Date());
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.printf("Network connections loading has finished: %s\n", new Date());
        }
    }

	public static void main(String[] args) {
        DataSourcesLoader dsLoader = new DataSourcesLoader();
        Thread thread1 = new Thread(dsLoader, "DataSourceThread");

        NetworkConnectionsLoader ncLoader = dsLoader.new NetworkConnectionsLoader();
        Thread thread2 = new Thread(ncLoader, "NetworkConnectionsLoader");

        thread1.start();
        thread2.start();

        /*
         * 为什么要用join()方法 ：
         * 在很多情况下，主线程生成并起动了子线程，如果子线程里要进行大量的耗时的运算，主线程往往将于子线程之前结束，
         * 但是如果主线程处理完其他的事务后，需要用到子线程的处理结果，也就是主线程需要等待子线程执行完成之后再结束，
         * 这个时候就要用到join()方法了。
         * 
         * join方法的作用，在JDk的API里对于join()方法是： 
         * “等待该线程终止”，这里需要理解的就是该线程是指的主线程等待子线程的终止。也就是在子线程调用了join()方法后
         * 面的代码，只有等到子线程结束了才能执行。
         */
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.printf("Main: Configuration has been loaded: %s\n", new Date());
    }
}
