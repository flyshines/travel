package qingning.server.rpc.manager;


import java.util.List;
import java.util.Map;

public interface IUserModuleServer {

	List<Map<String, Object>> getServerUrls();

	Map<String,Object> getLoginInfoByLoginIdAndLoginType(Map<String, Object> reqMap);

	Map<String,String> initializeRegisterUser(Map<String, Object> reqMap);


	Map<String,Object> findLoginInfoByUserId(String user_id);

	Map<String,Object> findUserInfoByUserId(String user_id);

	Map<String,Object> findCourseByCourseId(String courseId);


	void closeTradeBill(Map<String, Object> failUpdateMap);



	void updateUserWebOpenIdByUserId(Map<String, Object> updateMap);

	Map<String,Object> findTradebillByTradeId(String outTradeNo);

	Map<String,Object> handleWeixinPayResult(Map<String, Object> requestMapData) throws Exception;



	int updateUserCommonInfo(Map<String, Object> parameters);



	List<Map<String,Object>> findCourseIdByStudent(Map<String, Object> reqMap);


	/**
	 * 获取店铺统计信息
	 * @return
	 */
	Map<String, Object> findShopStatisticsByUserId(String user_id);


	/**
	 * 获取我的消费记录
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> findMyCostList(Map<String, Object> reqMap);

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
	int feedback(Map<String, Object> reqMap);

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
	 * 查找分销者信息
	 * @param inviteCode 邀请码
	 * @return
	 */
	Map<String,Object> findDistributerByInviteCode(String inviteCode);




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
	List<String> getMyCourseIdList(Map<String, Object> reqMap);

	/**根据手机号码获取用户信息
	 * @param phone
	 * @return
	 */
	Map<String,Object> findByPhone(String phone);

	/**更新用户手机号码
	 * @param userMap
	 * @return
	 */
	int updateUserPhone(Map<String, Object> userMap);

	/**查找门票信息
	 * @return
	 */
	Map<String,Object> findTicketInfo();

	List<Map<String,Object>> findBannerList();

	/**商户列表
	 * @param reqMap
	 * @return
	 */
	Map<String,Object> getShopList(Map<String, Object> reqMap);
}
