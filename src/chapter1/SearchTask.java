package chapter1;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SearchTask implements Runnable {
    
    private Result result = new Result();
    
    private class Result {
        private String name;
        public void setName(String name) {
            this.name = name;
        }
    }
    
    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        System.out.printf("Thread %s: Start\n", name);
        try {
            doTask();
            result.setName(name);
        } catch (InterruptedException e) {
            System.out.printf("Thread %s: Interrupted\n", name);
            return;
        }
        System.out.printf("Thread %s: End\n", name);
    }
    
    /**
     * 采用休眠的方式，来模拟执行线程的休眠时间。
     * 
     * @throws InterruptedException
     */
    private void doTask() throws InterruptedException {
    	// 随机生成两位数。
        Random random = new Random(new Date().getTime());
        int value = (int)(random.nextDouble() * 100);
        // 设置此线程的休眠时间。
        System.out.printf("Thread %s: sleep %d 秒 \n", Thread.currentThread().getName(), value);
        TimeUnit.SECONDS.sleep(value);
    }
    
    /**
     * 如果线程组中的活动线程数超过4个，“当前的执行线程（Main）”就持续等待。
     * 
     * @param threadGroup ThreadGroup
     */
    private static void waitFinish(ThreadGroup threadGroup) {
        while (threadGroup.activeCount() > 4) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ThreadGroup threadGroup = new ThreadGroup("Searcher");
        SearchTask searchTask = new SearchTask();

        // 创建一个线程，且执行，同时把该线程放入线程组。
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(threadGroup, searchTask);
            thread.start();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // 输出线程组的信息。
        System.out.printf("Number of Threads: %d\n", threadGroup.activeCount());
        System.out.printf("Information about the Thread Group\n");
        threadGroup.list();

        // 获得线程组中的线程引用。
        Thread[] threads = new Thread[threadGroup.activeCount()];
        threadGroup.enumerate(threads);
        // 输出线程组中，每根线程的“名称”与“状态”。
        for (int i = 0; i < threadGroup.activeCount(); i++) {
            System.out.printf("Thread %s: %s\n", threads[i].getName(), threads[i].getState());
        }
        
        waitFinish(threadGroup);
        
        // 中断线程组中的线程。
        threadGroup.interrupt();    
    }

}

