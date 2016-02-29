package chapter6;

import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * ConcurrentSkipListMap 是 ConcurrentNavigableMap 接口的实现类。
 * 
 * 从内部实现机制看，它使用了一个 Skip List 来存放数据。 Skip List 是基于并发列表的数据结构，效率与二叉树相近。
 * 与 “有序列表” 相比，ConcurrentSkipListMap 在添加、搜索、删除元素时，将耗费更少的时间。
 * 
 * 备注：Skip List 是由 William Pugh 在 1990 年引入的，详见：http://www.cs.umd.edu/~pugh/
 * 
 * 当插入元素到映射中时，ConcurrentSkipListMap 类使用键值来排序所有元素。除了提供返回一个具体元素的方法外，这
 * 个类也提供获取子映射的方法。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_5 {
	
	private static class Contact {
		
		private String name;
		private String phone;
		
		public Contact(String name, String phone) {
			this.name = name;
			this.phone = phone;
		}
		
		// --- get method ---
		
		public String getName() {
			return name;
		}
		
		public String getPhone() {
			return phone;
		}
	}
	
	private static class Task implements Runnable {
		
		/** 存放当前 task 的 ID。*/
		private String id;
		private ConcurrentSkipListMap<String, Contact> map;
		
		public Task(String id, ConcurrentSkipListMap<String, Contact> map) {
			this.id = id;
			this.map = map;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < 1000; i++) {
				Contact contact = new Contact(id, String.valueOf(i + 1000));
				map.put(id + contact.getPhone(), contact);
			}
		}
	}
	
	/*
	 * 工作原理：
	 * 
	 * Task 类在 ConcurrentSkipListMap 映射中存放 Contact 对象。每一个 contact 对象用 ID 标识，
	 * 而且还具有一个 1000 ~ 2000 的电话号码。拼接这两个值作为 contact 对象在映射中的键值。每个 task 对象
	 * 将生成 1000 个 contact 对象，并用 ConcurrentSkipListMap#put() 方法将它们保存在映射中。
	 * 
	 * 注意：如果新插入元素的键值在映射中已存在，就用新插入的值覆盖已有的值。
	 * 
	 * 更多信息：
	 * 
	 * headMap<K toKey>：这个方法返回映射中所有键值小于 “参数值 toKey” 的子映射。 
	 * 
	 * tailMap<K fromKey>：这个方法返回映射中所有键值大于 “参数值 fromKey” 的子映射。
	 * 
	 * putIfAbsent(K key, V value)：如果映射中不存在键 key，那么就将 key 和 value 保存到映射中。
	 * 
	 * pollLastEntry()：返回并移除映射中的最后一个 Map.Entry 对象。
	 * 
	 * replace(K key, V value)：如果映射中已经存在键 key，则用参数中的 value 替换现有的值。
	 */
	public static void main(String[] args) throws InterruptedException {
		
		ConcurrentSkipListMap<String, Contact> map = new ConcurrentSkipListMap<String, Contact>();
		
		Thread[] threads = new Thread[25];
		int counter = 0;
		
		for (char i = 'A'; i < 'Z'; i++) {
			Task task = new Task(String.valueOf(i), map);
			
			threads[counter] = new Thread(task);
			threads[counter].start();
			
			counter++;
		}
		
		for (Thread t : threads) {
			t.join();
		}
		
		System.out.printf("Main: Size of the map: %d\n", map.size());
		
		/*
		 * ConcurrentSkipListMap#firstEntry() 方法将返回映射中第一个包含 “键值和元素” 的 Map.Entry 对象，
		 * 使用 getValue() 方法将获得元素，而使用 getKey() 将获得元素的键值。而且，这个方法不会从映射中移除元素。
		 * 
		 * ConcurrentSkipListMap#lastEntry() 方法将返回映射中最后一个包含 “键值和元素” 的 Map.Entry 对象。
		 * 
		 * ConcurrentSkipListMap#subMap() 方法返回含有映射中部分元素的 ConcurrentNavigableMapd 对象。
		 * 在本例中，元素的值介于 A1996 ~ B1002 之间。程序中使用 pollFirst() 到子映射中获取元素，该方法会返回，并删
		 * 除子映射中的第一个 Map.Entry 对象。
		 * 
		 */
		Map.Entry<String, Contact> element = map.firstEntry();
		Contact contact = element.getValue();
		
		System.out.printf("Main: First Entry: %s: %s\n", contact.getName(), contact.getPhone());
		System.out.printf("Main: Submap from A1996 to B1002: \n");
		
		ConcurrentNavigableMap<String, Contact> submap = map.subMap("A1996", "B1002");
		do {
			element = submap.pollFirstEntry();
			if (element != null) {
				contact = element.getValue();
				System.out.printf("%s: %s\n", contact.getName(), contact.getPhone());
			}
		} while (element != null); 
		
		
	}
}