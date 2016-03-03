package tcp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class Main_1 {
	
	private static class Server implements Runnable {
		
		@Override
		public void run() {
			
			try {
				ServerSocket ss = new ServerSocket(5678);
				Socket socket = null;
				while (true) {
					try {
						// 从连接队列中取出一个客户端连接，如果没有则等待。
						socket = ss.accept();
						InputStream is = socket.getInputStream();
						OutputStream out = socket.getOutputStream();
						
						System.out.println("毒牙");
						System.out.println("客户端传来-------->" + new String(getData(is)));
						System.out.println("毒牙2");
//						out.write("服务端带来的问候".getBytes());
//						out.flush();
						
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						// 处理完请求后，关闭客户端 Socket。
						if (socket != null) {
							socket.close();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private byte[] getData(InputStream is) throws IOException {
			ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
			
	        byte[] buff = new byte[1024 * 64];
	        int rc = 0;
	        
	        while ((rc = is.read(buff, 0, 1024)) > 0) {
	            swapStream.write(buff, 0, rc);
	        }
	        byte[] bytes = swapStream.toByteArray();
	        return bytes; 
	    }
	}
	
	private static class ClientWriter implements Runnable {
		
		@Override
		public void run() {
			
			PrintWriter out = null;
			try {
				Socket socket = new Socket("localhost", 5678);
				
				out = new PrintWriter(socket.getOutputStream(), true);
				
				out.println("客户端发送访问请求!!!!!!!!!!!!!");
				out.flush();
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}
	}
	
	private static class ClientReader implements Runnable {
		
		@Override
		public void run() {
			BufferedReader in = null;
			try {
				Socket socket = new Socket("localhost", 5678);
				
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String s;
				while ((s = in.readLine()) != null) {
					System.out.println("服务端传来-------->" + s);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {}
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		new Thread(new Server()).start();
		
		TimeUnit.SECONDS.sleep(2);
		
//		new Thread(new ClientWriter()).start();
		new Thread(new ClientWriter()).start();
		new Thread(new ClientWriter()).start();
		new Thread(new ClientWriter()).start();
		new Thread(new ClientWriter()).start();
		new Thread(new ClientWriter()).start();
		
	}
}