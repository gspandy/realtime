package com.netease.beauty.realtime.constant.enums;

/**
 * 资源类型枚举
 * @author hzliyong
 *
 */
public enum ResourceTypeEnums {

	UNKNOWN(0, "unknown", "未知"),
	
	USER(1, "user", "用户"),
	
	NOTE(2, "note", "心得"),
	
	REPO(3, "repo", "合辑"),
	
	PRODUCT(4, "product", "产品"),
	
	BRAND(5, "brand", "品牌"),
	
	TAG(6, "label", "标签"),
	
	MYFOLLOW(7, "myFollow", "动态"),
	
	RANK(8, "rank", "排行榜"),
	
	ACTIVE(9, "active", "征集活动"),
	
	TXT(10, "txt", "纯文本"),
	
	URLSCHEME(11, "urlScheme", "url"),
	
	COMMENT(12, "comment", "评论"),
	
	SKU(13, "sku", "sku"),
	
	QIYU(14, "qiyu", "七鱼"),
	
	PHOTO(15, "photo", "图片"),
	
	APP(0, "app", "美学app应用");
	
	private final int type;
	
	private final String value;
	
	private final String desc;
	
	private ResourceTypeEnums(int type, String value, String desc) {
		this.type = type;
		this.value = value;
		this.desc = desc;
	}

	public int getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}
	
	/**
	 * 根据type返回对应的value
	 * @param type
	 * @return
	 * hzliyong
	 */
	public String getValueByType(int type) {
		for (ResourceTypeEnums resource : values()) {
			if (resource.getType() == type) {
				return resource.getValue();
			}
		}
		return ResourceTypeEnums.UNKNOWN.getValue();
	}
}
