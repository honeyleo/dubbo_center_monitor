package com.lvmama.soa.monitor.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.soa.monitor.constant.alert.AlertParamKey;
import com.lvmama.soa.monitor.dao.mybatis.DubboServiceDayIPDao;
import com.lvmama.soa.monitor.dao.redis.DubboServiceDayIPRedisDao;
import com.lvmama.soa.monitor.entity.DubboServiceDayIP;
import com.lvmama.soa.monitor.service.DubboServiceDayIPService;
import com.lvmama.soa.monitor.service.alert.IAlertService;
import com.lvmama.soa.monitor.util.Assert;
import com.lvmama.soa.monitor.util.DataSourceUtil;
import com.lvmama.soa.monitor.util.DateUtil;
import com.lvmama.soa.monitor.util.DistributedLock;

@Service("dubboServiceDayIPService")
public class DubboServiceDayIPServiceImpl implements DubboServiceDayIPService {
	private static final Log log = LogFactory.getLog(DubboServiceDayIPServiceImpl.class);
	
	@Autowired
	private IAlertService dubboServiceDayIPAlertService;
	@Autowired
	private DubboServiceDayIPDao dubboServiceDayIPDao;
	@Autowired
	private DubboServiceDayIPRedisDao dubboServiceDayIPRedisDao;

	@Override
	public int insertOrAppend(DubboServiceDayIP day) {
		if (day == null) {
			return 0;
		}

		int count = 0;
		String lockKey = day.uniqueKey();
		try {
			while (!DistributedLock.tryLock(lockKey)) {
				log.warn("waiting for lock:"+lockKey);
				try {
					Thread.sleep(1000L);
				} catch (Exception e) {
					log.error("sleep error when try to get lock", e);
				}
			}

			count = append(day);
			if (count == 0) {
				count = dubboServiceDayIPRedisDao.insert(day);
			}
		} catch (Exception e) {
			log.error("insertOrAppend error", e);
		} finally {
			DistributedLock.releaseLock(lockKey);
		}

		return count;
	}

	private int append(DubboServiceDayIP day) {
		DubboServiceDayIP oldDay = dubboServiceDayIPRedisDao.findOne(day);
		if(oldDay==null){
			return 0;
		}

		oldDay=DubboServiceDayIP.merge(day, oldDay);
		
		alert(oldDay);
		
		return dubboServiceDayIPRedisDao.update(oldDay);
	}

	private void alert(DubboServiceDayIP dubboServiceDayIP) {
		new Thread(new ServiceDayIPAlertRunnable(dubboServiceDayIPAlertService,dubboServiceDayIP)).start();
	}
	
	private class ServiceDayIPAlertRunnable implements Runnable{
		private IAlertService serviceDayIPAlertService;
		private DubboServiceDayIP dubboServiceDayIP;
		public ServiceDayIPAlertRunnable(IAlertService serviceDayIPAlertService,DubboServiceDayIP dubboServiceDayIP){
			this.serviceDayIPAlertService=serviceDayIPAlertService;
			this.dubboServiceDayIP=dubboServiceDayIP;
		}
		@Override
		public void run() {
			Map<String,Object> alertParam=new HashMap<String,Object>();
			alertParam.put(AlertParamKey.DUBBO_SERVICE_DAY_IP, dubboServiceDayIP);
			serviceDayIPAlertService.alert(alertParam);
		}
	}

	@Override
	public List<DubboServiceDayIP> selectList(Map<String, Object> param) {
		Assert.notNull(param.get("time_from"), "time_from");
		Assert.notEmpty(param.get("appName"), "appName");
		
		DubboServiceDayIP day=new DubboServiceDayIP();
		day.setAppName(param.get("appName").toString());
		param.put("shardTableName", day.getShardTableName());
		
		if(DataSourceUtil.canReadFromRedis(DubboServiceDayIP.class, DateUtil.yyyyMMdd((Date)param.get("time_from")))){
			return dubboServiceDayIPRedisDao.selectList(param);			
		}else{
			Date timeFrom = (Date)param.get("time_from");
			Date timeTo = (Date)param.get("time_to");
			String fromyyyyMMdd = DateUtil.yyyyMMdd(timeFrom);
			String toyyyyMMdd = DateUtil.yyyyMMdd(timeTo);
			if(timeFrom!=null&&timeTo!=null&&fromyyyyMMdd.equals(toyyyyMMdd)){
				//For performance:if it's to search the data of the same day, do not use date range
				Map<String,Object> param2=new HashMap<String,Object>(param);
				param2.put("time", DateUtil.trimToDay(timeFrom));
				param2.remove("time_from");
				param2.remove("time_to");
				return dubboServiceDayIPDao.selectList(param2);				
			}else{
				return dubboServiceDayIPDao.selectList(param);				
			}
		}
	}

	@Override
	public List<DubboServiceDayIP> selectMergedListByAppNameAndDay(String appName,
			String yyyyMMdd) {
		if(DataSourceUtil.canReadFromRedis(DubboServiceDayIP.class, yyyyMMdd)){
			return dubboServiceDayIPRedisDao.getMergedListByAppNameAndDay(appName,yyyyMMdd);
		}else{
			return dubboServiceDayIPDao.getMergedListByAppNameAndDay(appName,yyyyMMdd);
		}
	}
	
	@Override
	public void migrateFromRedisToMysql(String yyyyMMDD){
		Set<String> keys=dubboServiceDayIPRedisDao.getKeysByDate(yyyyMMDD);
		
		Set<String> deleteApps=new HashSet<String>();
		for(String key:keys){
			try{
				DubboServiceDayIP day =dubboServiceDayIPRedisDao.getByKey(key);
				if(!deleteApps.contains(day.getAppName())){
					//remove existing data of the same day before insert the latest data
					int deletedNum=dubboServiceDayIPDao.delete(day);
					log.info("removed "+deletedNum+" records in "+day.getShardTableName());
					deleteApps.add(day.getAppName());
				}
				dubboServiceDayIPDao.insert(day);		
//				dubboServiceDayIPRedisDao.deleteByKey(key);
			}catch(Exception e){
				log.error("migrateFromRedisToMysql error. key="+key, e);
			}
		}
	}
	
}
