package chapter1;

import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {

    public void uncaughtException(Thread t, Throwable e) {
        System.out.printf("An exception has been captured\n");
        System.out.printf("Thread: %s\n", t.getId());
        System.out.printf("Exceptin: %s: %s\n", e.getClass().getName(), e.getMessage());
        System.out.printf("Stack Trace: \n");
        e.printStackTrace(System.out);
        System.out.printf("Thread status: %s\n", t.getState());
    }

    private class Task implements Runnable {
        @Override
        public void run() {
            int numero = Integer.parseInt("TTT");
        }  
    }

    public static void main(String[] args) {
        ExceptionHandler exceptionHandler = new ExceptionHandler();
        Task task = exceptionHandler.new Task();
        Thread thread = new Thread(task);
        thread.setUncaughtExceptionHandler(exceptionHandler);
        thread.start();
    }
}
