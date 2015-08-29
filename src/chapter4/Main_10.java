package chapter4;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class Main_10 {
	
    private static class ExecutableTask implements Callable<String> {

        private String name;

        public String getName() {
            return name;
        }

        public ExecutableTask(String name) {
            this.name = name;
        }

        @Override
        public String call() throws Exception {
            try {
                long duration = (long)(Math.random() * 10);
                System.out.printf("%s: Waiting %d seconds for results.\n", this.name, duration);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                /*
                 * 由于在取消任务时，可能会抛出 InterruptedException 异常（在休眠是中断），
                 * 为了输出控制台输出内容的美观，因此这里先把异常吞掉。
                 */
            }

            return "Hello, world. I'm " + name;
        }
    }

    private static class ResultTask extends FutureTask<String> {

        private String name;
        
        public ResultTask(Callable<String> callable) {
            super(callable);
            this.name = ((ExecutableTask)callable).getName();
        }
        
        /*
         * 当任务执行结束，isDone() 方法的返回值为true时，FutureTask类就会调用done()方法。
         * 虽然在该方法中无法改变任务的结果值，也无法改变任务的状态，但是可以通过任务来关系系统资源、输出日志信息，发送通知等。
         * 
         * @see java.util.concurrent.FutureTask#done()
         */
        @Override
        protected void done() {
            if (isCancelled()) {
                System.out.printf("%s: Has been canceled\n", name);
            } else {
                System.out.printf("%s: Has finished\n", name);
            }
        }
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();
        
        // 创建一个数组来存储5个ResultTask对象。
        ResultTask resultTasks[] = new ResultTask[5];
        
        // 创建任务，并调用submit()方法将任务发送给执行器。
        for (int i = 0; i < 5; i++) {
            ExecutableTask executableTask = new ExecutableTask("Task " + i);
            resultTasks[i] = new ResultTask(executableTask);
            executor.submit(resultTasks[i]);
        }

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 取消已经发送给执行器的所有任务。
        for (int i = 0; i < resultTasks.length; i++) {
            resultTasks[i].cancel(true);
        }
        
        // 通过调用ResultTask#get()方法，在控制台上输出尚未被取消的任务结果。
        for (int i = 0; i < resultTasks.length; i++) {
            try {
                if (!resultTasks[i].isCancelled()) {
                    System.out.printf("%s\n", resultTasks[i].get());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            executor.shutdown();
        }
    }
}
