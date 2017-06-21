package com.netease.realtime;

import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;

public class Test {

	private static final String SYM = ":";
	
	public static void main(String[] args) {
		analyzeAccess();
	}
	
	private static void test() {
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
		
		List<Long> list3 = new ArrayList<Long>();
		list3.add(123L);
		list3.add(234L);
		list3.add(345L);
		handleOdd(list3);
		System.out.println("list3.size()=" + list3.size());
		printList(list3);
		BigDecimal big = new BigDecimal(3.001);
		System.out.println(big);
		big = big.setScale(2);
		System.out.println(big);
	}
	
	private static void handleOdd(List<Long> list) {
		int size = list.size();
		if (size % 2 != 0) {//奇数个则去掉最后一个元素
			list.remove(size - 1);
		}
	}
	
	private static void printList(List<Long> list) {
		for (Long l : list) {
			System.out.println(l);
		}
	}
	
	private static void analyzeAccess() {
		String requestPath = "/partner3";
		String requestPath2 = "/partner3/";
		String requestPath3 = "/partner3/index.html";
		String path = "/partner3/css/index.css";
		String get = "GET";
		String success = "200";
		
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile("C:/Users/hzliyong/PycharmProjects/pythonBible/chapter25/access.log", "r");
			List<String> lines = new ArrayList<String>();//存放文件中的每行数据
			Map<String, Integer> dayPVMap = new HashMap<String, Integer>();
			Map<String, Set<String>> dayUVMap = new HashMap<String, Set<String>>();
			Map<String, Integer> daySuccessMap = new HashMap<String, Integer>();
			String line;
			String[] arr;
			while ((line = file.readLine()) != null) {
				arr = line.split(" ");
				String method = arr[5].substring(1);
				if (arr != null 
						&& arr.length >= 10
						&& success.equals(arr[8])
						&& get.equals(method)) {
					lines.add(line);
				}
			}
			System.out.println("lines.size():" + lines.size());
			for (String l : lines) {
				arr = l.split(" ");
				if (path.equals(arr[6])) {
					String day = getDay(arr[2]);
					//分析每天的pv
					Integer pv = dayPVMap.get(day);
					if (pv == null) {
						dayPVMap.put(day, 1);
					} else {
						dayPVMap.put(day, pv + 1);
					}
					//分析每天的uv
					String ip = arr[11];
					Set<String> uvSet = dayUVMap.get(day);
					if (CollectionUtils.isEmpty(uvSet)) {
						uvSet = new HashSet<String>();
						dayUVMap.put(day, uvSet);
					}
					uvSet.add(ip);
				}
				String day = getDay(arr[2]);
				//分析每天的请求成功的记录
				Integer s = daySuccessMap.get(day);
				if (s == null) {
					daySuccessMap.put(day, 1);
				} else {
					daySuccessMap.put(day, s + 1);
				}
			}
			System.out.println("--------------PV--------------");
			for (Map.Entry<String, Integer> entry : dayPVMap.entrySet()) {
				System.out.println("日期：" + entry.getKey() + " pv:" + entry.getValue());
			}
			System.out.println("--------------UV--------------");
			for (Map.Entry<String, Set<String>> entry : dayUVMap.entrySet()) {
				System.out.println("日期：" + entry.getKey() + " uv:" + entry.getValue().size());
			}
			System.out.println("--------------请求成功--------------");
			for (Map.Entry<String, Integer> entry : daySuccessMap.entrySet()) {
				System.out.println("日期：" + entry.getKey() + " 请求返回200数量:" + entry.getValue());
			}
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * 
	 * @param day
	 * @return
	 * hzliyong
	 */
	private static String getDay(String day) {
		return day.substring(1, day.indexOf(SYM));
	}
}
