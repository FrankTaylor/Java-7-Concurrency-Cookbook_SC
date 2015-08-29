package chapter4;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main_5 {
	
	private static class UserValidator {
	    // 用来存储任务名称。
		private String name;
		
		public UserValidator(String name) {
			this.name = name;
		}
		
        /** 用来模拟用户身份认证。*/
		public boolean validate(String name, String password) {
			Random random = new Random();
			try {
                long duration = (long)(Math.random() * 10);
                System.out.printf("Validator %s: Validating a user during %d seconds\n", this.name, duration);

                // 用等待一段随机事件来模拟身份认证的过程。
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) { return false; }

            // 随机返回一个认证的结果。
            return random.nextBoolean();
		}

        public String getName() {
            return name;
        }
	}

    private static class TaskValidator implements Callable<String> {
        private UserValidator validator;
        private String user;
        private String password;

        public TaskValidator(UserValidator validator, String user, String password) {
            this.validator = validator;
            this.user = user;
            this.password = password;
        }

        @Override
        public String call() throws Exception {
            // 如果返回的认证结果为false，就抛出异常。
            if (!validator.validate(user, password)) {
                System.out.printf("%s: The user has not been found\n", validator.getName());
                throw new Exception("Error validating user");
            }

            // 如果认证通过，就输出一段文字，同时返回任务名称。
            System.out.printf("%s: The user has been found\n", validator.getName());
            return validator.getName();
        }
    }

    public static void main(String[] args) {
        String username = "test";
        String password = "test";

        UserValidator ldapValidator = new UserValidator("LDAP");
        UserValidator dbValidator = new UserValidator("DataBase");

        TaskValidator ldapTask = new TaskValidator(ldapValidator, username, password);
        TaskValidator dbTask = new TaskValidator(dbValidator, username, password);

        List<TaskValidator> taskList = new ArrayList<TaskValidator>();
        taskList.add(ldapTask);
        taskList.add(dbTask);

        ExecutorService executor = Executors.newCachedThreadPool();

        try {
            /*
             * 使用执行器的 invokeAny() 方法接收一个任务列表，然后执行任务，并返回第一个已完成、且没有抛出异常的任务执行结果。
             * 该方法将阻塞当前的执行线程，直到执行器中其中一个任务完成。
             * 
             * 该任务可以有如下4中可能性：
             * 1、如果两个任务都返回 true，那么invokeAny()将返回第一个完成的任务名称。
             * 2、如果第一个任务返回 true，第二个任务抛出异常，那么 invokeAny() 将返回第一个任务的名称。
             * 3、如果第一个任务抛出异常，第二个任务返回true，那么 invokeAny() 将返回第二个任务的名称。
             * 4、如果两个任务都抛出异常，那么 invokeAny() 将抛出 ExecutionException 异常。
             *
             * invokeAny(taskList, 1, TimeUnit.SECONDS); 该方法执行所有的任务，如果在给定的超时期满之前某个任务已经成功完成（未抛出异常），则返回其结果。
             * 如果到了规定时间，任何一个任务都未执行完成，将抛出 TimeoutException 异常。
             */
            String result = executor.invokeAny(taskList, 1, TimeUnit.SECONDS);
            System.out.printf("Main: Result: %s\n", result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        executor.shutdown();
        System.out.printf("Main: End of the Execution\n");
    }
}
