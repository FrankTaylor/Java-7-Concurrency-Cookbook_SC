package chapter3;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class Main_5 {
	
	/** 创建任务类 FileSearch 用于完成在文件夹中查找过去24小时内修改过指定扩展名的文件。*/
    private static class FileSearch implements Runnable {
    	
        private String initPath;        // 存储需要查找的文件夹。
        private String end;             // 存储需要查找的文件的扩展名。
        private List<String> results;   // 存储查找到的文件的完整路径。
        private Phaser phaser;          // 用来控制任务不同阶段的同步。

        public FileSearch(String initPath, String end, Phaser phaser) {
            this.initPath = initPath;
            this.end = end;
            this.phaser = phaser;
            results = new ArrayList<String>();
        }
        
        /** 该方法对传入的 File 对象中的子文件进行判断，如果是文件夹将递归调用，如果是文件将调用 fileProcess() 方法。*/
        private void directoryProcess(File file) {
            File list[] = file.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    if (list[i].isDirectory()) {
                        directoryProcess(list[i]);
                    } else {
                        fileProcess(list[i]);
                    }
                }
            }
        }
        
        /** 如果文件的后缀是指定的后缀，就把该文件名的绝对路径装入列表。*/
        private void fileProcess(File file) {
            if (file.getName().endsWith(end)) {
                results.add(file.getAbsolutePath());
            }
        }
        
        /** 对结果集中的查找到的文件进行过滤，如果不是过去24小时内修改过的文件就过滤掉。*/
        private void filterResults() {
            List<String> newResults = new ArrayList<String>();
            long actualDate = new Date().getTime();

            for (int i = 0; i < results.size(); i++) {
                File file = new File(results.get(i));
                long fileDate = file.lastModified();
                
                /*
                 * 比较文件的修改时间和当前时间，如果间隔小于1天，就把该文件的完整路径添加到新创建的列表中。
                 * 
                 * 注意：TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS) 这个方法挺有用的。
                 * 例如：TimeUnit.SECONDS.convert(1, TimeUnit.HOURS) 就表示1小时等于多少秒。
                 */
                if (actualDate - fileDate < TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) {
                    newResults.add(results.get(i));
                }
            }
            
            // 把新列表的引用赋值给老列表。
            results = newResults;
        }
        
        /** 对集合内的结果进行检查。*/
        private boolean checkResults() {
            if (results.isEmpty()) {
                System.out.printf("%s: Phase %d: 0 results.\n", Thread.currentThread().getName(), phaser.getPhase());
                System.out.printf("%s: Phase %d: End.\n", Thread.currentThread().getName(), phaser.getPhase());
                
                /*
                 * 调用  Phaser#arriveAndDeregister() 方法时将会把内部计数器的初始值减1。
                 * 例如：new Phaser(3); 时，调用 Phaser#getRegisteredParties() 方法将返回 3，因为该方法用于返回计数器的初始值。
                 * 一旦调用 Phaser#arriveAndDeregister() 方法后，在调用 Phaser#getRegisteredParties() 方法将返回 2。因为初始值被减1了。
                 */
                phaser.arriveAndDeregister();
                return false;
            } else {
                System.out.printf("%s: Phase %d: %d results.\n", Thread.currentThread().getName(), phaser.getPhase(), results.size());
                
                /*
                 * 当 Phaser 对象内部的计数器没有累计到，在构造器中指定的数值时，调用 Phaser#arriveAndDeregister() 方法将阻塞当前线程。
                 * 
                 * 例如：当 new Phaser(10); 时，内部的计数器从 0 开始。
                 * 每当某一线程调用 Phaser#arriveAndDeregister() 方法时将发生以下行为：
                 * 1、该对象内部计数器的值加1；
                 * 2、判断当前计数器的值与构造器指定的值是否相同。
                 *   不同：阻塞当前线程的执行；
                 *   相同：1、所有因为调用 Phaser#arriveAndDeregister() 方法而阻塞的线程同时执行；
                 *       2、内部计数器重新置为 0，当再次执行 arriveAndDeregister() 方法时，内部计数器还可以加1，所以该对象可以重复利用。
                 */
                phaser.arriveAndAwaitAdvance();
                return true;
            }
        }
        
        /** 在控制台输出结果集中的文件绝对路径。*/
        private void showInfo() {
            for (int i = 0; i < results.size(); i++) {
                File file = new File(results.get(i));
                System.out.printf("%s: %s\n", Thread.currentThread().getName(), file.getAbsolutePath());
            }
            phaser.arriveAndAwaitAdvance();
        }

        @Override
        public void run() {
        	
        	// 目的：让多个线程同时执行对文件查找的任务。
            phaser.arriveAndAwaitAdvance();
            System.out.printf("%s: Starting.\n", Thread.currentThread().getName());

            File file = new File(initPath);
            if (file.isDirectory()) {
                directoryProcess(file);
            }
            
            // 目的：让得到结果集的线程同时执行 filterResults() 方法。
            if (!checkResults()) {
                return;
            }

            filterResults();

            // 目的：让得到结果集的线程同时执行 showInfo() 方法和接下来的方法。
            if (!checkResults()) {
                return;
            }

            showInfo();
            phaser.arriveAndDeregister();
            System.out.printf("%s: Work completed.\n", Thread.currentThread().getName());
        }
    }

    public static void main(String[] args) {

    	/*
    	 * 创建 Phaser 对象，并指定参与阶段同步的线程是3个。
    	 * 
    	 * Phaser 对象有两种状态：
    	 * 1、活跃态（Active）：当存在参与同步的线程的时候（初始值大于0），Phaser 就是活跃的。
    	 * 2、终止态（Termination）：当所有参与同步的线程都取消注册的时候，Phaser就处于终止状态。当Phaser是终止态的时候，同步方法 arriveAndAwaitAdvance()
    	 * 方法会立即返回，而且不会做任何同步的操作。
    	 * 
    	 * Phaser 类的一个重大特性就是不必对它的方法进行异常处理。被Phaser对象置于休眠的线程不会响应中断事件，也不会抛出InterruptedException异常。
    	 * 
    	 * 1、Phaser#arrive()：这个方法会通知 Phaser 对象，一个参与者已经完成当前阶段，但它不会等待其他参与者都完成当前阶段。必须小心使用该方法，因为该方法不会与其他线程同步。
    	 * 2、Phaser#awaitAdvance(int phase)：如果传入的阶段参数与当前阶段一致，这个方法会将当前线程置于休眠，直到这个阶段的所有参与者都运行完成。如果传入的阶段参数与当前
    	 * 阶段不一致，这个方法将立即返回。
    	 * 3、Phaser#awaitAdvanceInterruptibly(int phaser)：这个方法与 awaitAdvance(int phase)差不多，不同之处在于，如果在这个方法中休眠的线程被中断，它
    	 * 将抛出 InterruptedException 异常。
    	 * 4、Phaser#getPhase() ：该方法将返回当前执行的阶段。
    	 *   注意：new Phaser(3); arriveAndDeregister(); arriveAndDeregister(); arriveAndDeregister(); 执行了三次后 getPhase() 返回的是一个 -2147483640。
    	 *       如果是三个线程分别执行三次 arriveAndDeregister(); 那 getPhase() 返回的是一个正常的阶段值。
    	 *       所以， Phaser 类一定是需要配合线程使用的。
    	 * 
    	 * 5、Phaser#getRegisteredParties()：该方法将返回在构造器中指定的值。
    	 * 
    	 * Phaser 类提供了两种方法增加注册者的数量：
    	 * 1、register()：该方法将一个新的参与者注册到 Phaser 中，这个新的参与者将被当成没有完成本阶段的线程。
    	 * 2、bulkRegister(int parties)：该方法将指定书目的参与者注册到Phaser中，这些所有新的参与者都将被当成没有完成本阶段的线程。
    	 * 
    	 * Phaser 类只提供了 arriveAndDeregister() 这一个能减少注册者数目的方法。
    	 * 
    	 * 当一个 Phaser 对象没有参与线程时，它将处于终止状态。Phaser 类提供了 forceTermination() 方法来强制 Phaser 对象进行终止状态，这个方法不管Phaser中是否还有
    	 * 注册的参与者。当一个参与线程发生错误时，强制Phaser终止是很有意义的。
    	 * 
    	 * 当 Phaser 对象处于终止状态时，awaitAdvance() 和 awaitAndAwaitAdvance() 方法将立即返回一个负数，而不再是一个正值了。
    	 */
        Phaser phaser = new Phaser(3);
        
        // 创建3个文件查找类FileSearch对象，并为每个对象指定不同的查找目录，且指定查找文件的扩展名为.log文件。
        FileSearch system = new FileSearch("C:\\Windows", "log", phaser);
        FileSearch apps = new FileSearch("C:\\Program Files", "log", phaser);
        FileSearch documents = new FileSearch("C:\\Document And Settings", "log", phaser);
        
        // 创建3个线程，分别启动这3个任务。
        Thread systemThread = new Thread(system, "System");
        systemThread.start();
        Thread appsThread = new Thread(apps, "Apps");
        appsThread.start();
        Thread documentsThread = new Thread(documents, "Documents");
        documentsThread.start();
        
        // 让主线程等待这三个线程的结束。
        try {
            systemThread.join();
            appsThread.join();
            documentsThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    	
        /*
         * 调用 Phaser#isTerminated() 方法，查看Phaser对象是否已经结束。
         * 当Phaser对象不存在参与同步的线程时，Phaser就是终止状态的。
         * 
         * 注意：初始值是 0 时，不算是终止状态。
         * 例如：new Phaser(1); Phaser#arriveAndDeregister(); 此时内部计数器是 0 可以算是终止状态。
         * 例如：new Phaser(0); 这时调用 isTerminated() 方法将返回 false，表示不是终止状态，而此时调用 Phaser#arriveAndDeregister(); 
         * 将抛出 IllegalStateException 异常。 
         */
        System.out.println("Terminated: " + phaser.isTerminated());
    	
    }
}
