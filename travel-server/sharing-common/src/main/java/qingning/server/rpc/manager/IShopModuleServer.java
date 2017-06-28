package qingning.server.rpc.manager;


import java.util.List;
import java.util.Map;

public interface IShopModuleServer {
	/**
	 * 获取店铺信息及其统计信息
	 * @param reqMap
	 * @return
	 */
	Map<String, Object> queryShopDetail(Map<String, Object> reqMap);
	/**
	 * 根据店铺id查询店铺信息
	 * @param reqMap
	 * @return
	 */
	Map<String, Object> queryShopInfo(Map<String, Object> reqMap);
	/**
	 * 更新店铺信息
	 * @param reqMap
	 * @return
	 */
	int updateShopInfo(Map<String, Object> reqMap);
	/**
	 * 分页查询店铺课程列表，并关联查询出课程信息
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> queryShopCourseList(Map<String, Object> reqMap);
	/**
	 * 查询系统配置详情
	 * @param rqParam
	 * @return
	 */
	Map<String, Object> querySystemConfig(Map<String, Object> rqParam);
	/**
	 * 获取店铺统计信息
	 * @param reqMap
	 * @return
	 */
	Map<String, Object> getShopStatistics(Map<String, Object> reqMap);
	/**
	 * 获取店铺课程id集合
	 * @param reqMap
	 * @return
	 */
	List<String> getShopCourseIdList(Map<String, Object> reqMap);
	/**
	 * 分页获取店铺课程销售记录列表
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> getCourseSaleList(Map<String, Object> reqMap);
	/**
	 * 分页获取店铺的客户统计，并关联查询客户信息
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> getCustomerStatisticsList(Map<String, Object> reqMap);
	/**
	 * 根据条件获取店铺课程记录
	 * @param reqMap
	 * @return
	 */
	Map<String, Object> getShopCourseInfo(Map<String, Object> reqMap);
	/**
	 * 上架课程，并更新店铺统计信息表中的课程数量及更新时间
	 * @param reqMap
	 * @return
	 */
	int addShopCourse(Map<String, Object> reqMap);
	/**
	 * 更新店铺课程信息
	 * @param reqMap
	 * @return
	 */
	int updateShopCourse(Map<String, Object> reqMap);
	/**
	 * 根据用户id获取店铺信息
	 * @param userId
	 * @return
	 */
	Map<String, Object> queryShopByUserId(String userId);
}
