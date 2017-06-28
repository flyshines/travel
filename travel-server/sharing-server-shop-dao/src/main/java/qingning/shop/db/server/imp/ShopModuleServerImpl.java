
package qingning.shop.db.server.imp;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import qingning.common.entity.QNLiveException;
import qingning.common.util.MiscUtils;
import qingning.dbcommon.mybatis.persistence.CoursesMapper;
import qingning.dbcommon.mybatis.persistence.DistributerMapper;
import qingning.dbcommon.mybatis.persistence.LecturerMapper;
import qingning.dbcommon.mybatis.persistence.LoginInfoMapper;
import qingning.dbcommon.mybatis.persistence.ShopCourseMapper;
import qingning.dbcommon.mybatis.persistence.ShopMapper;
import qingning.dbcommon.mybatis.persistence.ShopStatisticsMapper;
import qingning.dbcommon.mybatis.persistence.UserMapper;
import qingning.server.rpc.manager.IShopModuleServer;

public class ShopModuleServerImpl implements IShopModuleServer {
	@Autowired(required = true)
	private ShopMapper shopMapper;
	@Autowired(required = true)
	private ShopCourseMapper shopCourseMapper;
	@Autowired(required = true)
	private ShopStatisticsMapper shopStatisticsMapper;

	/**
	 * 获取店铺信息及其统计信息
	 */
	@Override
	public Map<String, Object> queryShopDetail(Map<String, Object> reqMap) {
		Map<String, Object> resultMap = shopStatisticsMapper.selectShopAndStatistics(reqMap);
		return resultMap;
	}

	/**
	 * 根据店铺id查询店铺信息
	 */
	@Override
	public Map<String, Object> queryShopInfo(Map<String, Object> reqMap) {
		Map<String, Object> resultMap = shopMapper.selectShopInfo(reqMap);
		return resultMap;
	}

	/**
	 * 更新店铺信息
	 */
	@Override
	public int updateShopInfo(Map<String, Object> reqMap) {
		int updateNum = shopMapper.updateShopInfo(reqMap);
		return updateNum;
	}

	/**
	 * 分页查询店铺课程列表，并关联查询出课程信息
	 */
	@Override
	public List<Map<String, Object>> queryShopCourseList(Map<String, Object> reqMap) {
		List<Map<String, Object>> shopCourseList = shopCourseMapper.selectShopCourseList(reqMap);
		/*
		 * 判断是否需要更新店铺访问次数
		 */
		//share_shop_id != null表示来自web访问，需要更新访问统计
		String share_shop_id = (String) reqMap.get("share_shop_id");
		if(share_shop_id != null && !share_shop_id.isEmpty()){
			shopStatisticsMapper.addShopStatisticsVisitTimes(reqMap);
		}
		return shopCourseList;
	}

	/**
	 * 查询系统配置详情
	 */
	@Override
	public Map<String, Object> querySystemConfig(Map<String, Object> rqParam) {
		Map<String, Object> resultMap = shopMapper.selectSystemConfig(rqParam);
		return resultMap;
	}

	/**
	 * 获取店铺统计信息
	 */
	@Override
	public Map<String, Object> getShopStatistics(Map<String, Object> reqMap) {
		Map<String, Object> resultMap = shopStatisticsMapper.selectShopStatistics(reqMap);
		return resultMap;
	}

	/**
	 * 获取店铺课程id集合
	 */
	@Override
	public List<String> getShopCourseIdList(Map<String, Object> reqMap) {
		List<String> result = shopCourseMapper.selectShopCourseIdList(reqMap);
		return result;
	}

	/**
	 * 分页获取店铺课程销售记录列表
	 */
	@Override
	public List<Map<String, Object>> getCourseSaleList(Map<String, Object> reqMap) {
		List<Map<String, Object>> courseSaleList = shopCourseMapper.selectCourseSaleList(reqMap);
		return courseSaleList;
	}

	/**
	 * 分页获取店铺的客户统计，并关联查询客户信息
	 */
	@Override
	public List<Map<String, Object>> getCustomerStatisticsList(Map<String, Object> reqMap) {
		List<Map<String, Object>> customerStatistics = shopMapper.selectCustomerStatistics(reqMap);
		return customerStatistics;
	}

	/**
	 * 根据条件获取店铺课程记录
	 */
	@Override
	public Map<String, Object> getShopCourseInfo(Map<String, Object> reqMap) {
		Map<String, Object> shopCourse = shopCourseMapper.selectShopCourse(reqMap);
		return shopCourse;
	}

	/**
	 * 上架课程，并更新店铺统计信息表中的课程数量及更新时间
	 */
	@Transactional(rollbackFor=Exception.class)
	@Override
	public int addShopCourse(Map<String, Object> reqMap) {
		int insertNum = shopCourseMapper.insertShopCourse(reqMap);
		//更新店铺统计信息
		insertNum = shopStatisticsMapper.addShopStatisticsCourseNum(reqMap);
		return insertNum;
	}

	/**
	 * 更新店铺课程信息，并更新相应的统计信息
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public int updateShopCourse(Map<String, Object> reqMap) {
		int updateNum = shopCourseMapper.updateShopCourse(reqMap);
		/*
		 * 并更新相应的统计信息
		 */
		//判断是上架还是下架
		long status = (long) reqMap.get("status");
		if(0 == status){	//上架
			//更新店铺统计信息
			updateNum = shopStatisticsMapper.addShopStatisticsCourseNum(reqMap);
		}else{	//下架
			//更新店铺统计信息
			updateNum = shopStatisticsMapper.subShopStatisticsCourseNum(reqMap);
		}
		
		return updateNum;
	}

	/**
	 * 根据用户id获取店铺信息
	 */
	@Override
	public Map<String, Object> queryShopByUserId(String userId) {
		return shopMapper.selectShopByUserId(userId);
	}

}
