package a;

import java.util.Arrays;

public class Test {
	
	public static void main(String[] args) {
		int[] elems = {6, 10, 100, 90, 56, 2, 86, 192, 9, 20};
		
		System.out.println("" + partitionIt(0, elems.length, 60, elems));
		System.out.println(Arrays.toString(elems));
	}
	
	public static int partitionIt(int left, int right, int pivot, int[] elems) {
		int leftPtr = left - 1;
		int rightPtr = right + 1;
		
		while (true) {
			while (leftPtr < right && elems[++leftPtr] < pivot);
			while (rightPtr > left && elems[--rightPtr] > pivot);
			
			if (leftPtr >= rightPtr) {
				break;
			} else {
				int temp = elems[leftPtr];
				elems[leftPtr] = elems[rightPtr];
				elems[rightPtr] = temp;
			}
		}
		
		return leftPtr;
	}
	
	private static void dubbleSort(int[] elems) {
		int out, in;
		
		for (out = elems.length - 1; out > 1; out--) {
			for (in = 0; in < out; in++) {
				if (elems[in] > elems[in + 1]) {
					int temp = elems[in];
					elems[in] = elems[in + 1];
					elems[in + 1] = temp;
				}
			}
		}
	}
	
	private static void changeSort(int[] elems) {
		int out, in, min;
		for (out = 0; out < elems.length - 1; out++) {
			min = out;
			for (in = out + 1; in < elems.length; in++) {
				if (elems[in] < elems[min]) {
					min = in;
				}
			}
			
			int temp = elems[out];
			elems[out] = elems[min];
			elems[min] = temp;
		}
	}
	
	private static void insertSort(int[] elems) {
		int in, out;
		for (out = 1; out < elems.length; out++) {
			int temp = elems[out];
			in = out;
			while (in > 0 && elems[in - 1] >= temp) {
				elems[in] = elems[in - 1];
				--in;
			}
			
			elems[in] = temp;
		}
	}
}