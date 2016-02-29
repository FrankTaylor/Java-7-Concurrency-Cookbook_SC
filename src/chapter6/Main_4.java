package chapter6;

/**
 * DelayQueue 这个类可以保存带有 “激活时间” 的元素。当调用的方法从队列中 “返回/提取” 元素时，未来的元素日期将被忽略。
 * 这些元素对于这些方法是不可见的。
 * 
 * 为了具有调用时的这些行为，存放到 DelayQueue 类中的元素必须继承 Delayed 接口。Delayed 接口使对象成为延迟对象，
 * 它使存放在 DelayQueue 类中的对象具有了激活时间，该接口会强制执行下列两个方法：
 * 
 * compareTo(Delayed o)：
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 1.0
 */
public class Main_4 {
	
}