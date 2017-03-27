package com.netease.beauty.realtime.business.dalog.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.netease.beauty.realtime.business.dalog.meta.UserExposeHistory;
import com.netease.print.daojar.dao.autowired.PolicyObjectDaoSqlBaseOfAutowired;
import com.netease.print.daojar.meta.base.DBCondition;
import com.netease.print.daojar.util.PrintDaoUtil;

public class UserExposeHistoryDao extends PolicyObjectDaoSqlBaseOfAutowired<UserExposeHistory> {

	private final Logger logger = LoggerFactory.getLogger(UserExposeHistoryDao.class);
	
	public List<Long> saveUserExposeHistory(List<UserExposeHistory> list) {
		logger.info("UserExposeHistoryDaoImpl#saveUserExposeHistory begin");
		List<Long> itemIdList = new ArrayList<Long>();
		for (UserExposeHistory history : list) {
			if (super.addObject(history) != null) {
				itemIdList.add(history.getItem());
			}
		}
//		logger.info("UserExposeHistoryDaoImpl#saveUserExposeHistory end");
		return itemIdList;
	}

	public List<Long> batchDeleteUserExposeHistory(List<UserExposeHistory> list) {
		logger.info("UserExposeHistoryDaoImpl#batchDeleteUserExposeHistory begin");
		List<Long> itemIdList = new ArrayList<Long>();
		DBCondition dbCondition = new DBCondition();
		for (UserExposeHistory history : list) {
			if ((StringUtils.isEmpty(history.getPvid()) || "null".equals(history.getPvid()))
					|| (history.getItem() == null
					&& StringUtils.isEmpty(history.getItemType()))) {
				continue;//pvid为空或者item和itemType都为空则不删除
			}
			dbCondition.addWhereByValue("item", history.getItem());
			dbCondition.addWhereByValue("itemType", history.getItemType());
			dbCondition.addWhereByValue("pvid", history.getPvid());
			if (PrintDaoUtil.deleteObjectsWithPrepare(this, dbCondition)) {
				itemIdList.add(history.getItem());
			}
		}
//		logger.info("UserExposeHistoryDaoImpl#batchDeleteUserExposeHistory end");
		return itemIdList;
	}

}
