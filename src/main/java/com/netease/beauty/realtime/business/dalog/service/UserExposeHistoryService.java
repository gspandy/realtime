package com.netease.beauty.realtime.business.dalog.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netease.beauty.realtime.business.dalog.dao.UserExposeHistoryDao;
import com.netease.beauty.realtime.business.dalog.meta.UserExposeHistory;

/**
 * 用户首页曝光历史
 * @author hzliyong
 *
 */
public class UserExposeHistoryService {

	private final Logger logger = LoggerFactory.getLogger(UserExposeHistoryService.class);
	
	private UserExposeHistoryDao userExposeHistoryDao;
	
	private final Lock lock = new ReentrantLock();
	
	public List<Long> saveUserExposeHistory(List<UserExposeHistory> list) {
		logger.info("UserExposeHistoryServiceImpl#saveUserExposeHistory begin");
		if (CollectionUtils.isEmpty(list)) {
			return new ArrayList<Long>();
		}
		List<Long> idList = userExposeHistoryDao.saveUserExposeHistory(list);
		return idList;
	}

	public List<Long> batchDeleteUserExposeHistory(List<UserExposeHistory> list) {
		logger.info("UserExposeHistoryServiceImpl#batchDeleteUserExposeHistory begin");
		if (CollectionUtils.isEmpty(list)) {
			return new ArrayList<Long>();
		}
		List<Long> idList = userExposeHistoryDao.batchDeleteUserExposeHistory(list);
		return idList;
	}

	public void batchDeleteAddUserEposeHistory(List<UserExposeHistory> list) {
		lock.lock();
		try {
			List<Long> deletedItemList = batchDeleteUserExposeHistory(list);
			logger.info("UserExposeHistoryServiceImpl#batchDeleteAddUserEposeHistory批量删除userExposeHistory，删除成功的items：" + deletedItemList);
			List<Long> itemList = saveUserExposeHistory(list);
			logger.info("UserExposeHistoryServiceImpl#batchDeleteAddUserEposeHistory保存成功的items：" + itemList);
		} catch (Exception e) {
			logger.error("MessageHandler#handleDaExpose批量保存或删除失败", e);
		} finally {
			lock.unlock();
		}
	}
	
	

}
