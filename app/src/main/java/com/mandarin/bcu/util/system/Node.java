package com.mandarin.bcu.util.system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mandarin.bcu.util.unit.Form;
import com.mandarin.bcu.util.unit.Unit;

public class Node<T> {

	public static List<Form> deRep(List<Form> list) {
		List<Form> ans = new ArrayList<>();
		for (Form f : list)
			if (ans.size() > 0) {
				Form last = ans.get(ans.size() - 1);
				if (f.unit == last.unit) {
					if (f.fid > last.fid) {
						ans.remove(last);
						ans.add(f);
					}
				} else
					ans.add(f);
			} else
				ans.add(f);
		return ans;
	}

	public static <T> Node<T> getList(Collection<T> l, T n) {
		Node<T> ans = null, ret = null;
		for (T v : l) {
			Node<T> temp = new Node<>(v);
			if (ans != null)
				ans.add(temp);
			if (v == n)
				ret = temp;
			ans = temp;
		}
		return ret;
	}

	public static Node<Unit> getList(List<Form> list, Form unit) {
		Node<Unit> ans = null, ret = null;
		for (Form v : list) {
			if (ans != null && ans.val == v.unit)
				continue;
			Node<Unit> temp = new Node<>(v.unit);
			if (ans != null)
				ans.add(temp);
			if (v.unit == unit.unit)
				ret = temp;
			ans = temp;
		}
		return ret;
	}

	public Node<T> prev, next;

	public final T val;

	private Node<T> side, end;

	public Node(T v) {
		val = v;
	}

	public Node<T> add(Node<T> n) {
		if (next != null)
			next.prev = n;
		n.next = next;
		next = n;
		n.prev = this;
		return n;
	}

	public void adds() {
		adds(side, end);
	}

	public int len() {
		if (next == null)
			return 1;
		else
			return next.len() + 1;
	}

	public void removes() {
		side.removes(end);
	}

	public Node<T> side(Node<T> e) {
		side = next;
		end = e;
		side.removes(end);
		return this;
	}

	public List<T> sides() {
		List<T> ans = new ArrayList<>();
		for (Node<T> i = side; i != null; i = i == end ? null : i.next)
			ans.add(i.val);
		return ans;
	}

	private void adds(Node<T> p, Node<T> e) {
		if (next != null)
			next.prev = e;
		e.next = next;
		next = p;
		p.prev = this;
	}

	private void removes(Node<T> e) {
		if (prev != null)
			prev.next = e.next;
		if (next != null)
			next.prev = prev;
		prev = null;
		e.next = null;
	}

}
