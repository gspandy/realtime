package com.netease.beauty.realtime.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BaseUtils {
//	private static final Logger logger = LoggerFactory.getLogger(BaseUtils.class);

	/**
	 * 随即取list里一些值出来
	 * 
	 * @param <T>
	 * @param list
	 * @param size
	 *            取值的个数，比list的总个数大时，直接返回list
	 * @return 返回一个列表，长度为min(list.size, size)
	 */
	public static <T> List<T> getRandomSubList(List<T> list, int size) { // FIXME:
																			// 是否可以删除？
		if (list == null || size <= 0) {
			return Collections.emptyList();
		}

		if (list.size() <= size) {
			return list;
		}
		Collections.shuffle(list, new Random(System.currentTimeMillis()));

		return list.subList(0, size);
	}

	public static String gbkFilter(String str) { // FIXME: 是否可以删除？
		if (str == null)
			return null;
		char[] ces = new char[str.length()];
		int j = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if ((c < 0x4e00 || c > 0x9fa5) && c > 255)
				continue;// 过滤非常规汉字
			ces[j++] = c;
		}
		return new String(ces, 0, j);
	}

	public static String joinStrings(String sep, Object... stres) {
		if (stres == null || stres.length <= 0 || sep == null) // TODO: review
																// xj,czk
			return "";
		StringBuilder sb = new StringBuilder();
		for (Object str : stres) {
			sb.append(sep).append(str);
		}
		return sb.substring(sep.length());
	}
	

	/**
	 * sql拼接
	 */
	public static String buildSql(String... stres) { // TODO: review xj
		if (stres == null || stres.length <= 0)
			return "";
		return joinStrings(" ", (Object[]) stres);
	}

	/**
	 * 根据时间与dbId生成一个不影响时间显示的时间, 主要是为了尽量减少时间字段相同而产生的排序问题
	 */
	public static long parseTimeOrder(long time, long dbId) {
		return (time >> 9 << 9) | (dbId & 0x1FF);
	}

	public static <T> List<T> asList(T[] objes) { // FIXME：可以删除吧 review czk
		List<T> list = new LinkedList<T>();
		if (objes != null && objes.length >= 1) {
			for (T obj : objes) {
				list.add(obj);
			}
		}
		return list;
	}

	public static boolean isSameDay(long time, long time2) { // FIXME: 需要移出去
		Calendar day1 = Calendar.getInstance();
		day1.setTimeInMillis(time);
		Calendar day2 = Calendar.getInstance();
		day2.setTimeInMillis(time2);
		return day1.get(Calendar.DAY_OF_YEAR) == day2.get(Calendar.DAY_OF_YEAR) && day1.get(Calendar.YEAR) == day2.get(Calendar.YEAR);

	}

	public static long getHeaderOfDay(long dayTime) { // FIXME: 需要移出去
		Calendar day1 = Calendar.getInstance();
		day1.setTimeInMillis(dayTime);
		day1.set(Calendar.HOUR_OF_DAY, 0);
		day1.set(Calendar.MINUTE, 0);
		day1.set(Calendar.SECOND, 0);
		day1.set(Calendar.MILLISECOND, 0);
		return day1.getTimeInMillis();
	}

	/**
	 * 把毫秒转换为 HH:MM:SS
	 * 
	 * @param time
	 * @return
	 */
	public static String formatTime(long time) { // TODO: review xj //FIXME
													// 方法名字也要改一下 review czk
		StringBuilder sb = new StringBuilder();
		long hour = time / DateUtils.MILLIS_PER_HOUR;
		long minutesLeft = time % DateUtils.MILLIS_PER_HOUR;
		long minutes = minutesLeft / DateUtils.MILLIS_PER_MINUTE;
		long secondsLeft = minutesLeft % DateUtils.MILLIS_PER_MINUTE;
		long seconds = secondsLeft / DateUtils.MILLIS_PER_SECOND;
		sb.append(String.format("%02d", hour)).append(":").append(String.format("%02d", minutes)).append(":")
				.append(String.format("%02d", seconds));
		return sb.toString();
	}

	public static String formatTime(long time, String format) {
		return new SimpleDateFormat(format).format(new Date(time));
	}

//	/**
//	 * 获取Nginx的Ip
//	 * 
//	 * @param request
//	 * @return
//	 */
//	public static String getNginxIp() {
//		return IPUtils.getNginxIp();
//	}
//
//	public static String getActualIp(HttpServletRequest request) {
//		return IPUtils.getActualIp(request);
//	}

	/**
	 * 获取联通代理IP
	 * 
	 * @param request
	 * @return
	 */
//	public static String getProxyIp(HttpServletRequest request) {
//		return IPUtils.getProxyIp(request);
//	}

	/**
	 * length of String: a chinese character as length of 2; an english
	 * character as legnth of 1
	 * 
	 * @param s
	 */
	public static int lengthOfString(String s) {
		if (StringUtils.isEmpty(s)) {
			return 0;
		}
		char[] charArray = s.toCharArray();
		int length = 0;
		for (char c : charArray) {
			if (isChinese(c)) {
				length += 2;
			} else {
				length++;
			}
		}
		return length;
	}

	/**
	 * 换一种判断方式 character as legnth of 1
	 * 
	 * @param s
	 */
	public static int lengthOfString2(String s) {
		if (StringUtils.isEmpty(s)) {
			return 0;
		}
		char[] charArray = s.toCharArray();
		int length = 0;
		for (char c : charArray) {
			if (c >= 0 && c <= 0xff) {
				length++;
			} else {
				length += 2;
			}
		}
		return length;
	}

	public static String cutString(String s, int length) {
		//去除空格和换行
		if (s != null) {
			Pattern p = Pattern.compile("\t|\r|\n");
			Matcher m = p.matcher(s);
			s = m.replaceAll(" ");
			//合并空格
			s = s.replace("\\s{1,}", " ");
		}
		if (lengthOfString(s) <= length) {
			return s;
		}
		StringBuilder sb = new StringBuilder();
		char[] charArray = s.toCharArray();
		int n = 0;
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (isChinese(c) || isEmojiCharacter(c)) {
				n += 2;
			} else {
				n++;
			}
			if (n <= length) {
				sb.append(c);
			} else {
				break;
			}
		}
		return sb.toString();
	}
	
	public static boolean isEmojiCharacter(char codePoint) {
		return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD)
				|| ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
				|| ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
	}

	public static boolean isChinese(char c) {

		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);

		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS

		|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS

		|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A

		|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION

		|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION

		|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {

			return true;

		}

		return false;

	}

	public static String filterNoUtf8Characters(String content) throws UnsupportedEncodingException, CharacterCodingException { // FIXME:
																																// 是否可以删除？
		if (StringUtils.isEmpty(content)) {
			return content;
		}
		CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder();
		utf8Decoder.onMalformedInput(CodingErrorAction.IGNORE);
		utf8Decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
		ByteBuffer bytes;
		bytes = ByteBuffer.wrap(content.getBytes("utf-8"));
		CharBuffer parsed = utf8Decoder.decode(bytes);
		return parsed.toString();
	}

	public static List<String> splitToList(String data, String split) {
		if (StringUtils.isEmpty(data)) {
			return new ArrayList<String>(0);
		} else {
			String[] items = StringUtils.split(data, split);
			List<String> rs = new LinkedList<String>();
			for (String item : items) {
				rs.add(item);
			}
			return rs;
		}
	}

	/**
	 * clz.newInstance()失败时抛出RuntimeException
	 * 
	 * @param source
	 * @param delimiter
	 * @param clz
	 * @param converter
	 * @return
	 */
	public static <T2, T extends Collection<T2>> T splistToCollection(String source, String delimiter, Class<T> clz, Converter<T2> converter) {
		try {
			T newInstance = clz.newInstance();
			String[] split = StringUtils.split(source, delimiter);
			if (split != null && split.length > 0) {
				for (String s : split) {
					newInstance.add(converter.convert(s));
				}
			}
			return newInstance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public interface Converter<T> {
		public T convert(String s);
	}

	public static String escapeHtmlChar(String s) { // FIXME: 需要移出去
		// FIXME: 可以使用 StringEscapeUtils.escapeHtml4(input) review by czk
		if (s == null)
			return null;

		StringBuffer sb = new StringBuffer();
		for (int i = 0, l = s.length(); i < l; ++i) {
			char c = s.charAt(i);
			switch (c) {
			case ' ':
				sb.append("&nbsp;");
				break;
			case '\n':
				sb.append("<br/>");
				break;
			case '\r':
				sb.append("");
				break;
			case '\'':
				sb.append("&#039;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static Map<String, Object> getDocIdAndType(String url) { // TODO:
																	// review xj
		// FIXME:使用Pair作为返回值
		Map<String, Object> map = new HashMap<String, Object>();
		if (StringUtils.isBlank(url)) {
			return map;
		}
		int end = url.lastIndexOf(".");
		int start = url.lastIndexOf("/");
		if (start < 0 || end < 0 || start >= end) {
			return map;
		}
		String docString = url.substring(start + 1, end);
		String fileType = url.substring(end + 1);
		if (fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg") || fileType.equalsIgnoreCase("png")
				|| fileType.equalsIgnoreCase("gif")) {
			if (StringUtils.isNumeric(docString)) {
				map.put("docId", Long.parseLong(docString));
				map.put("docType", fileType);
			}
		}
		return map;
	}

//	public static String readResFile(String fileName) throws IOException {
//		Resource res = new ClassPathResource(fileName);
//		if (!res.exists()) {
//			logger.info("can't find properties file:" + fileName);
//			return null;
//		}
//		File file = res.getFile();
//		if (file.exists() && file.canRead()) {
//			return FileUtils.readFileToString(file, "utf-8");
//		}
//		return null;
//	}



	public static List<String> filterNicknameFromMsg(String msg) {
		if (StringUtils.isEmpty(msg)) {
			return Collections.emptyList();
		}
		List<String> nicknames = new LinkedList<String>();
		//Pattern pattern = Pattern.compile("@([a-zA-Z0-9_\\-\\u4E00-\\u9FA5]+)");
		//王逸天需求，带#的有限匹配成话题
		Pattern pattern = Pattern.compile("((@[a-zA-Z0-9_\\-\\u4E00-\\u9FA5]+)|(#([^\\\\\n\r#]+)#)){1}");
		Matcher matcher = pattern.matcher(msg);
		while (matcher.find()) {
			String nickname = matcher.group(1);
			if(nickname.startsWith("@")){
				nicknames.add(nickname.substring(1));
			}
		}
		return nicknames;
	}

	private static Pattern atPattern = Pattern.compile("@([a-zA-Z0-9_\\-\\u4E00-\\u9FA5]+)", Pattern.CASE_INSENSITIVE);

	public static String coverAt(String content, String klass) throws UnsupportedEncodingException {
		if (StringUtils.isBlank(content))
			return "";
		Matcher matcher = atPattern.matcher(content);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String nickname = content.substring(matcher.start(1), matcher.end(1));
			matcher.appendReplacement(sb, "<a href=\"/user/home?nickname=" + URLEncoder.encode(nickname, "utf-8") + "\" class=\"" + klass
					+ "\">@" + nickname + "</a>");
		}
		matcher.appendTail(sb);

		return sb.toString();
	}

//
//	/**
//	 * web500页面重定向方法，重定向的时候需要将ref参数合并到url
//	 */
//	public static void webRedirect500() { // FIXME: 需要移出去
//		HttpServletRequest request = BaseUserContext.getRequest();
//		HttpServletResponse response = BaseUserContext.getResponse();
//		String ref = request.getParameter("ref");
//		if (!response.isCommitted()) {
//			try {
//				response.sendRedirect("/500" + (StringUtils.isBlank(ref) ? "" : "?ref=" + ref));
//			} catch (IOException e) {
//				logger.error(e);
//			}
//		}else{
//			logger.warn("response has been commited!");
//		}
//	}

//	/**
//	 * web404页面重定向方法，重定向的时候需要将ref参数合并到url
//	 */
//	public static void webRedirect404() { // FIXME: 需要移出去
//		HttpServletRequest request = BaseUserContext.getRequest();
//		HttpServletResponse response = BaseUserContext.getResponse();
//		String ref = request.getParameter("ref");
//		try {
//			response.sendRedirect("/404" + (StringUtils.isBlank(ref) ? "" : "?ref=" + ref));
//		} catch (IOException e) {
//			logger.error(e);
//		}
//	}



	public static void main(String[] args) {
		// System.out.println(buildMemKey("abc", "def"));
		// System.out.println(buildSql("abc", "def"));
		// System.out.println(formatTime(53524501L));
		// System.out.println(getDocIdAndType("sadgdf"));
		// System.out.println(getDocIdAndType("http://123.png"));
		// System.out.println(getDocIdAndType("http://123.png/"));
		// System.out.println(lengthOfString2("abc我サービスは"));
		char a = 0x8A;
		System.out.println(a);
	}

	

	public static <T> void collectionSplit(Collection<T> collections, int size, CollectionSplitCallback<T> callback) {
		if (collections.isEmpty()) {
			return;
		}
		if (size < 1) {
			throw new RuntimeException("invalid params, size:" + size);
		}
		Iterator<T> iterator = collections.iterator();
		Collection<T> list = new ArrayList<T>(size);
		while (iterator.hasNext()) {
			list.add(iterator.next());
			if (list.size() >= size) {
				callback.splitedCollection(list);
				list = new ArrayList<T>(size);
			}
		}
		if (list.size() > 0) {
			callback.splitedCollection(list);
		}
	}

	public interface CollectionSplitCallback<T> {
		public void splitedCollection(Collection<T> subCollection);
	}

	
	/**
	 * string类型的object转化成long
	 * @param obj
	 * @return
	 * hzliyong
	 */
	public static long strObject2Long(Object obj) {
		if (obj instanceof String) {
			return Long.valueOf((String)obj);
		} else if (obj instanceof Long) {
			return (Long) obj;
		} else if (obj instanceof Integer) {
			return Long.valueOf((Integer)obj);
		}
		return 0;
	}
	
	/**
	 * string类型的object转化成int
	 * @param obj
	 * @return
	 * hzliyong
	 */
	public static int strObject2Integer(Object obj) {
		if (obj instanceof String) {
			return Integer.valueOf((String)obj);
		} else if (obj instanceof Long) {
			return (Integer) obj;
		} else if (obj instanceof Integer) {
			return (Integer) obj;
		}
		return 0;
	}
}
