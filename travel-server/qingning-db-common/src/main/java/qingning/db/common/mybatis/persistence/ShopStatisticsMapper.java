package qingning.db.common.mybatis.persistence;

import java.util.Map;

public interface ShopStatisticsMapper {
	
	/**
	 * 获取店铺统计信息-根据用户ID
	 */
    Map<String,Object> findByUserId(String user_id);
	
	/**
	 * 获取店铺统计信息-根据店铺ID
	 */
    Map<String,Object> findByShopId(String shop_id);

	/**
	 * 更新店铺统计信息
	 * @param insertMap
	 * @return
	 */
	int updateShopStatistics(Map<String, Object> insertMap);

	/**
	 * 新增店铺统计信息
	 * @param insertMap
	 * @return
	 */
	int insertShopStatistics(Map<String, Object> insertMap);
	/**
	 * 获取店铺信息及其统计信息
	 * @param reqMap
	 * @return
	 */
	Map<String, Object> selectShopAndStatistics(Map<String, Object> reqMap);
	/**
	 * 获取店铺统计信息
	 * @param reqMap
	 * @return
	 */
	Map<String, Object> selectShopStatistics(Map<String, Object> reqMap);
	/**
	 * 根据shop_id，店铺统计课程数量加1
	 * @param reqMap
	 * @return
	 */
	int addShopStatisticsCourseNum(Map<String, Object> reqMap);
	/**
	 * 根据shop_id，店铺统计课程数量减1
	 * @param reqMap
	 * @return
	 */
	int subShopStatisticsCourseNum(Map<String, Object> reqMap);
	/**
	 * 根据shop_id，店铺统计访问数量加1
	 * @param reqMap
	 */
	void addShopStatisticsVisitTimes(Map<String, Object> reqMap);
}
