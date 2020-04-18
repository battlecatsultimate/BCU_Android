package com.mandarin.bcu.io;

import com.mandarin.bcu.BuildConfig;

import java.util.Stack;

public class Temp {

	public static void main(String[] args) {
		Stack<Temp> stack = new Stack<>();
		Temp t0 = new Temp();
		stack.push(t0);
		if (BuildConfig.DEBUG && stack.pop() != t0) {
			throw new AssertionError("Assertion failed");
		}
		stack.pop();
		System.out.println(stack.size());
	}
}
