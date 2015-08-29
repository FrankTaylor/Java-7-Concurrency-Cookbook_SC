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

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.printf("Main: Configuration has been loaded: %s\n", new Date());
    }
}
