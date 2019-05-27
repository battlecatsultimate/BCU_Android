package com.mandarin.bcu.io;

import java.io.File;
import java.util.Stack;

public class Temp {

	public static void main(String[] args) {
		Stack<Temp> stack = new Stack<>();
		Temp t0 = new Temp();
		stack.push(t0);
		assert stack.pop() == t0;
		stack.pop();
		System.out.println(stack.size());
	}
/*
	public static void main$0(String[] args) {
		WebFileIO.download("http://battlecatsultimate.cf/api/resources/assets/080504.zip", new File("./img/Test.zip"),
				null);
	}
*/
}
