package com.netease.realtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {

	public static void main(String[] args) {
		Long a = 1L;
		long b = a;
		
		List<Long> list1 = new ArrayList<Long>();
		list1.add(2L);
		list1.add(3L);
		
		List<Long> list2 = new ArrayList<Long>();
		list2.add(2L);
		list1.removeAll(list2);
		System.out.println(list1.get(0));
		
		List<Long> notExistProductId = new ArrayList<Long>();
		notExistProductId.add(123L);
		String prdId = "123";
		System.out.println(notExistProductId.contains(Long.valueOf(prdId)));
//		notExistProductId = null;
//		System.out.println(notExistProductId.contains(null));
		System.out.println(System.getProperties().get("java.vm.info"));
		System.out.println(System.getProperties().get("java.home"));
		
		AtomicInteger count = new AtomicInteger();
		System.out.println(count.get());
		count.incrementAndGet();
		System.out.println(count.get());
		int value = count.getAndSet(0);
		System.out.println(value);
		System.out.println(count.get());
		
	}
}
