package qingning.server.rpc.manager;


import java.util.List;
import java.util.Map;

import qingning.common.entity.RequestEntity;

public interface IUserCommonModuleServer {
	
	
	public List<Map<String, Object>> getServerUrls();

	Map<String,Object> getLoginInfoByLoginIdAndLoginType(Map<String, Object> reqMap);

	Map<String,String> initializeRegisterUser(Map<String, Object> reqMap);

	Map<String,Object> findLectureByLectureId(String lecture_id);
	
	Map<String,Object> findLoginInfoByUserId(String user_id);

	Map<String,Object> findUserInfoByUserId(String user_id);

	Map<String,Object> findCourseByCourseId(String courseId);

	Map<String,Object> findRewardInfoByRewardId(String reward_id);

	void closeTradeBill(Map<String, Object> failUpdateMap);


	boolean isTradebillFinish(String outTradeNo);

	void updateUserWebOpenIdByUserId(Map<String, Object> updateMap);

	Map<String,Object> findTradebillByTradeId(String outTradeNo);

	Map<String,Object> handleWeixinPayResult(Map<String, Object> requestMapData) throws Exception;
	
	Map<String,Object> findByDistributerId(String distributer_id);
	
	List<Map<String,Object>> findDistributionInfoByDistributerId(Map<String, Object> parameters);
	List<Map<String,Object>> findRoomDistributerRecommendInfo(Map<String, Object> parameters);
	List<Map<String,Object>> findRoomDistributerCourseInfo(Map<String, Object> parameters);
	List<Map<String,Object>> findCourseWithRoomDistributerCourseInfo(Map<String, Object> parameters);
	List<Map<String,Object>> findRoomDistributerCourseDetailsInfo(Map<String, Object> parameters);
	
	int updateUserCommonInfo(Map<String, Object> parameters);


	Map<String,Object> findLiveRoomByRoomId(String room_id);

	Map<String,Object> findRoomDistributerRecommendAllInfo(Map<String, Object> queryMap);

	void insertRoomDistributerRecommend(Map<String, Object> insertMap);

	Map<String,Object> findRoomDistributionInfoByDistributerId(Map<String, Object> queryMap);

	//void increteRecommendNumForRoomDistributer(Map<String, Object> updateMap);

	void updateAfterStudentBuyCourse(Map<String, Object> updateCourseMap);

	void insertFeedback(Map<String, Object> reqMap);

	Map<String,Object> findRewardByUserIdAndCourseId(Map<String, Object> rewardQueryMap);

	Map<String,Object> findVersionInfoByOS(String plateform);
	Map<String,Object> findAvailableRoomDistributer(Map<String, Object> record);
	Map<String,Object> findForceVersionInfoByOS(String force_version_key);

	Integer updateIMAccount(Map<String, Object> updateIMAccountMap);

	Map<String,Object> findUserDistributionInfo(Map<String, Object> queryuserDistribution);

	List<Map<String,Object>> findcourseRecommendUsers(Map<String, Object> reqMap);

	Map<String,Object> findCourseRecommendUserNum(Map<String, Object> reqMap);

	Map<String,Object> findRoomDistributerRecommendItem(Map<String, Object> queryMap);

	void updateRoomDistributerRecommend(Map<String, Object> insertMap);

	List<Map<String,Object>> findRoomRecommendUserList(Map<String, Object> reqMap);

	List<Map<String,Object>> findDistributionRoomDetailList(Map<String, Object> reqMap);
	
	Map<String,Object> findDistributionRoomDetail(Map<String, Object> reqMap);
	
	List<Map<String,Object>> findCourseIdByStudent(Map<String, Object> reqMap);
	
	Map<String,Object> findCoursesSumInfo(Map<String, Object> queryMap);
	
	/**
	 * 更新分销者基本信息
	 * @param parameters
	 * @return
	 */
	public int updateDistributerInfo(Map<String, Object> parameters);

	/**
	 * 获取店铺统计信息
	 * @return
	 */
	public Map<String, Object> findShopStatisticsByUserId(String user_id);

	/**
	 * 获取我购买的课程信息
	 * @return
	 */
	public List<Map<String,Object>> findCourseSaleList(Map<String, Object> parameters);

	/**
	 * 获取我的消费记录
	 * @param reqMap
	 * @return
	 */
	public List<Map<String, Object>> findMyCostList(Map<String, Object> reqMap);
	
	/**
	 * 插入新的交易信息
	 * @param insertMap
	 * @return
	 */
	int insertTradeBill(Map<String, Object> insertMap);

	/**
	 * 提交意见反馈
	 * @param reqMap
	 * @return
	 */
	public int feedback(Map<String, Object> reqMap);

	/**
	 * 根据config_key列表获取系统配置
	 * @param configKeys
	 */
    List<Map<String, Object>> findSystemConfig(List<String> configKeys);

	/**
	 * 获取店铺信息
	 * @param shopId
	 * @return
	 */
	Map<String,Object> findShopInfo(String shopId);

	/**
	 * 生成支付信息
	 * @param insertPayMap
	 * @return
	 */
	int insertPaymentBill(Map<String, Object> insertPayMap);

	/**
	 * 生成通知信息
	 * @param insertPayMap
	 * @return
	 */
	int insertInform(Map<String, Object> insertPayMap);


	/**
	 * 查找分销者信息
	 * @param inviteCode 邀请码
	 * @return
	 */
	Map<String,Object> findDistributerByInviteCode(String inviteCode);

	/**
	 * 生成分销者信息
	 * @param insertMap
	 * @return
	 */
	int insertDistributer(Map<String, Object> insertMap);

	/**
	 * 生成店铺信息
	 * @param insertMap
	 * @return
	 */
	int insertShop(Map<String, Object> insertMap);

	/**
	 * 生成店铺统计信息
	 * @param insertMap
	 * @return
	 */
	int insertShopStatistics(Map<String, Object> insertMap);


	/**
	 * 更新店铺统计信息
	 * @param insertMap
	 * @return
	 */
	int updateShopStatistics(Map<String, Object> insertMap);

	/**
	 * 生成分销者收益信息
	 * @param insertMap
	 * @return
	 */
	int insertDistributerProfit(Map<String, Object> insertMap);

	/**
	 * 更新支付清单信息
	 * @param updateMap
	 * @return
	 */
	int updatePaymentBill(Map<String, Object> updateMap);
	/**
	 * 更新交易清单信息
	 * @param updateMap
	 * @return
	 */
	int updateTradeBill(Map<String, Object> updateMap);

	/**
	 * 更新店铺课程销售数量
	 * @param insertMap
	 * @return
	 */
	int updateShopCourseNum(Map<String, Object> insertMap);

	/**
	 * 更新店铺客户信息
	 * @param insertMap
	 * @return
	 */
	int updateShopCustomer(Map<String, Object> insertMap);

	/**
	 * 更新店铺客户信息
	 * @param insertMap
	 * @return
	 */
	int insertShopCustomer(Map<String, Object> insertMap);

	/**
	 * 查询分销者信息
	 * @param user_id
	 * @return
	 */
	Map<String,Object> findDistributerByUserId(String user_id);

	/**
	 * 获取店铺客户信息
	 * @param shopId
	 * @param userId
	 * @return
	 */
	Map<String,Object> findShopCustomerByShopIdUserId(String shopId, String userId);

	/**
	 * 新增课程销售信息
	 * @param insertMap
	 *
	 * @return
	 */
	int insertCourseSale(Map<String, Object> insertMap);

	/**
	 * 获取指定条件的系统配置列表
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> findSysConfiguration(Map<String, Object> reqMap);
	/**
	 * 获取登录用户购买的指定类型课程id集合
	 * @param reqMap
	 * @return
	 */
	public List<String> getMyCourseIdList(Map<String, Object> reqMap);

	/**根据手机号码获取用户信息
	 * @param phone
	 * @return
	 */
	Map<String,Object> findByPhone(String phone);

	/**更新用户手机号码
	 * @param userId
	 * @param phone
	 * @return
	 */
	public int updateUserPhone(Map<String, Object> userMap);

}
