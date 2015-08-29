package chapter3;

import java.util.Date;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class Main_6 {

    private static class MyPhaser extends Phaser {
    	
    	/*
    	 * Phaser#onAdvance(int phase, int registeredParties) 方法，它在 phaser 阶段改变的时候会被自动执行。
    	 * 具体来说，就是所有因 arriveAndAwaitAdvance() 方法休眠的线程再被唤醒之前，该方法将被调用。
    	 * 
    	 * 该方法返回 false 时，表示 Phaser 对象还在继续执行中；返回 true 时，表示 Phaser 已经完成执行并且进入了终止态。
    	 * 
    	 * onAdvance()方法有两个虚参：
    	 * （1）phase：当前的阶段数；
    	 * （2）registeredParties：注册的参与者的数量。
    	 * 
    	 * 该方法默认是这样实现的：如果注册的参与者数量为 0 就返回 true，否则就返回 false。
    	 * 但我们可以通过继承 Phaser 类来覆盖这个方法。如果我们希望从一个阶段到另一个阶段过渡的时候执行一些操作，那就值得覆盖其原来的实现。
    	 */
        @Override
        protected boolean onAdvance(int phase, int registeredParties) {
            switch (phase) {
            case 0:
                return studentsArrived();        // 阶段 1 时执行 studentsArrived() 方法。
            case 1:
                return finishFirstExercise();    // 阶段 2 时执行 finishFirstExercise() 方法。
            case 2:
                return finishSecondExercise();   // 阶段 3 时执行 finishSecondExercise() 方法。
            case 3:
                return finishExam();             // 阶段 4 时执行 finishExam() 方法。
            default:
                return true;
            }
            
        }
        
        /** 阶段 0 时执行的方法。*/
        private boolean studentsArrived() {
            System.out.printf("Phaser: The exam are going to start. The students are ready.\n");
            System.out.printf("Phaser: We have %d students.\n", getRegisteredParties());
            return false;
        }

        /** 阶段 1 时执行的方法。*/
        private boolean finishFirstExercise() {
            System.out.printf("Phaser: All the students have finished the first exercise.\n");
            System.out.printf("Phaser: It's time for the second one.\n");
            return false;
        }

        /** 阶段 2 时执行的方法。*/
        private boolean finishSecondExercise() {
            System.out.printf("Phaser: All the students the have finished the second exercise.\n");
            System.out.printf("Phaser: It's time for the third one.\n");
            return false;
        }

        /** 阶段 3 时执行的方法。*/
        private boolean finishExam() {
            System.out.printf("Phaser: All the students have finished the exam.\n");
            System.out.printf("Phaser: Thank you for your time.\n");
            return true;
        }
    }
    
    /** 这个类将模拟学生考试。*/
    private static class Student implements Runnable {
        
        private Phaser phaser;

        public Student(Phaser phaser) {
            this.phaser = phaser;
        }

        @Override
        public void run() {
            System.out.printf("%s: Has arrived to do the exam.%s\n", Thread.currentThread().getName(), new Date());
            
            phaser.arriveAndAwaitAdvance();
            
            System.out.printf("%s: Is going to do the first exercise.%s\n", Thread.currentThread().getName(), new Date());
            doExercise1();
            System.out.printf("%s: Has done the first exercise.%s\n", Thread.currentThread().getName(), new Date());
            
            phaser.arriveAndAwaitAdvance();
            
            System.out.printf("%s: Is going to do the second exercise.%s\n", Thread.currentThread().getName(), new Date());
            doExercise2();
            System.out.printf("%s: Has done the second exercise.%s\n", Thread.currentThread().getName(), new Date());
            
            phaser.arriveAndAwaitAdvance();
            
            System.out.printf("%s: Is going to do the third exercise.%s\n", Thread.currentThread().getName(), new Date());
            doExercise3();
            System.out.printf("%s: Has finished the exam. %s\n", Thread.currentThread().getName(), new Date());
            
            phaser.arriveAndAwaitAdvance();
        }

        private void doExercise1() {
            try {
                long duration = (long)(Math.random() * 10);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void doExercise2() {
            try {
                long duration = (long)(Math.random() * 10);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void doExercise3() {
            try {
                long duration = (long)(Math.random() * 10);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        MyPhaser phaser = new MyPhaser();
        
        /*
         * 创建 5 个学生任务，并通过 register() 方法将他们注册到 Phaser 对象中。
         */
        Student students[] = new Student[5];
        for (int i = 0; i < students.length; i++) {
        	students[i] = new Student(phaser);
        	/*
        	 * 例如：new Phaser(0); 表示 Phaser 对象中没有线程参与者额度。此时执行 phaser.getRegisteredParties() 方法为 0。
        	 * 执行 Phaser#register() 方法后额度（内部计数器） + 1。再次执行 phaser.getRegisteredParties() 方法为 1。
        	 * 
        	 * 注意：此时执行学生任务的线程还未和Phaser对象之间建立关联。
        	 */
            phaser.register();
        }
        
        // 创建 5 根线程来执行这 5 个任务。
        Thread threads[] = new Thread[students.length];
        for (int i = 0; i < students.length; i++) {
            threads[i] = new Thread(students[i], "Student " + i);
            threads[i].start();
        }
        
        // 让主线程等待他们完成。
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // 通过调用 isTerminated() 方法，将返回 Phaser 对象是否处于终止的状态。
        System.out.printf("Main: The phaser has finished: %s.\n", phaser.isTerminated());
    }
}
