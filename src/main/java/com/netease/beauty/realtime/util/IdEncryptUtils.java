package com.netease.beauty.realtime.util;

import org.apache.commons.lang.StringUtils;

import com.netease.beauty.realtime.exception.RealTimeException;

/**
 * 用于加密和解密返回给客户端的各类id，包括商品id，品牌id等等
 * 2016年8月12日上午12:13:18
 * 
 */
public class IdEncryptUtils {
	
//	private static final Logger logger = Logger.getLogger("IdEncryptUtils");
	
	private static String prefix = "prx";
	
	private static String key = "3A4A-5NC2-65C5-8";

	public static String encrypt(long id) {
		String encryptedId = String.valueOf(id);
		return encrypt(encryptedId);
	}
	
	public static String encrypt(String id) {
		if(id == null) {
			return null;
		}
		if(id.startsWith(prefix)) {
			//不需要再加密
//			logger.warn("IdEncryptUtils#idEncrypt. already encrypt id="+id);
			return id;
		}
		try {
			String eId = AESUtils.encrypt(id, key);
			return prefix+eId;
		} catch (Exception e) {
//			logger.error("IdEncryptUtils#idEncrypt. Exception id="+id, e);
			throw new RuntimeException(e);
		}
		//return id;
		
	}
	
	public static String decryptAsString(String encryptedId) {
		if(encryptedId == null) {
			return null;
		}
		if(!encryptedId.startsWith(prefix)) {
			//不是美学加密标示,需要重点记录和埋点观察
//			logger.warn("IdEncryptUtils#idDecryptAsString. no need encrypt id="+encryptedId);
			return encryptedId;
		}
		try {
			String sub = encryptedId.substring(prefix.length());
			String id = AESUtils.decrypt(sub, key);
			
			return id;
		} catch (Exception e) {
//			logger.error("IdEncryptUtils#idDecryptAsString. Exception id="+encryptedId, e);
			throw new RuntimeException(e);
		}
	}
	
	public static long decryptAsLong(String encryptedId) {
		String idStr = decryptAsString(encryptedId);
		return Long.parseLong(idStr);
	}
	
	public static String idEncrypt(long id) {
		//TODO shxujialei 加解密的工作待日后有空再完成
		String encryptedId = String.valueOf(id);
		
		return encryptedId;
	}
	
	public static String idEncrypt(String id) {
		//TODO shxujialei 加解密的工作待日后有空再完成
		String encryptedId = id;
		
		return encryptedId;
	}
	
	public static String idDecryptAsString(String encryptedId) {
		//TODO shxujialei 加解密的工作待日后有空再完成
		String id = encryptedId;
		
		return id;
	}
	
	public static long idDecryptAsLong(String encryptedId) {
		//TODO shxujialei 加解密的工作待日后有空再完成
		long id = Long.parseLong(encryptedId);
		if (id < 0) {
//			logger.error("IdEncryptUtils idDncryptAsLong error! encryptedId, id :" + encryptedId + ", " + id);
			throw new RealTimeException(500, "参数错误");
		}
		return id;
	}
	
	//以下3个方法因为不需要加密，且需暂时兼容数据类型时调用（主要是String）
	public static String idLongToString(long id) {
		String idString = String.valueOf(id);
		return idString;
	}
	
	public static String idLongToString(String id) {
		return id;
	}
	
	public static long idStringToLong(String id) {
		if (StringUtils.isBlank(id)) {
			return -1;
		}
		long idLong = Long.parseLong(id);
		return idLong;
	}
	
	public static void main(String[] args) {
		String id = decryptAsString("prxa5714d6a8cb14e94ced15ac1e43f2865");
		System.out.println(id);
		
		String encryptId = encrypt(1017391);
		System.out.println(encryptId);
	}
	
}
