
package qingning.user.common.db.server.imp;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import qingning.common.util.Constants;
import qingning.common.util.MiscUtils;
import qingning.dbcommon.mybatis.persistence.*;
import qingning.server.rpc.manager.IUserCommonModuleServer;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserCommonModuleServerImpl implements IUserCommonModuleServer {
	
	@Autowired(required = true)
	private UserMapper userMapper;
	@Autowired(required = true)
	private LoginInfoMapper loginInfoMapper;
	@Autowired(required = true)
	private ShopStatisticsMapper shopStatisticsMapper;
	@Autowired(required = true)
	private CourseSaleMapper courseSaleMapper;
	@Autowired(required = true)
	private TradeBillMapper tradeBillMapper;
	@Autowired(required = true)
	private FeedbackMapper feedbackMapper;
	@Autowired(required = true)
	private CourseMapper courseMapper;
	@Autowired(required = true)
	private SystemConfigMapper systemConfigMapper;
    @Autowired(required = true)
    private ShopMapper shopMapper;
    @Autowired(required = true)
	private PaymentBillMapper paymentBillMapper;
	@Autowired(required = true)
	private InformMapper informMapper;
	@Autowired(required = true)
	private DistributerMapper distributerMapper;
	@Autowired(required = true)
	private ShopCourseMapper shopCourseMapper;
	@Autowired(required = true)
	private DistributerProfitMapper distributerProfitMapper;
	@Autowired(required = true)
	private ShopCustomerMapper shopCustomerMapper;

	public List<Map<String, Object>> getServerUrls() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, Object> findLoginInfoByUserId(String user_id) {
		return loginInfoMapper.findLoginInfoByUserId(user_id);
	}
	

	public Map<String, Object> findCourseByCourseId(String courseId) {
		return courseMapper.findCourseByCourseId(courseId);
	}

	public void closeTradeBill(Map<String, Object> failUpdateMap) {
        tradeBillMapper.updateTradeBill(failUpdateMap);
	}

	public int insertPaymentBill(Map<String, Object> insertPayMap) {
       return paymentBillMapper.insertPaymentBill(insertPayMap);
	}

	public Map<String, Object> handleWeixinPayResult(Map<String, Object> requestMapData) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	public List<Map<String, Object>> findCourseIdByStudent(Map<String, Object> reqMap) {
		// TODO Auto-generated method stub
		return null;
	}


	public void updateUserWebOpenIdByUserId(Map<String, Object> updateMap) {
		Map<String,Object> record = new HashMap<String,Object>();
		record.put("user_id", updateMap.get("user_id"));
		record.put("web_openid",updateMap.get("web_openid"));
		loginInfoMapper.updateLoginInfo(record);
	}
	
	public Map<String, Object> getLoginInfoByLoginIdAndLoginType(Map<String, Object> reqMap) {
		return loginInfoMapper.getLoginInfoByLoginIdAndLoginType(reqMap);
	}
	
	public Map<String, Object> findUserInfoByUserId(String user_id) {
		return userMapper.findByUserId(user_id);
	}

	@Transactional(rollbackFor=Exception.class)
	@Override
	public Map<String, String> initializeRegisterUser(Map<String, Object> reqMap) {
		//1.插入t_user		
		Date now = new Date();
		Map<String,Object> user = new HashMap<String,Object>();
		String uuid = MiscUtils.getUUId();
		user.put("user_id", uuid);
		user.put("user_name", reqMap.get("user_name"));
		user.put("nick_name", reqMap.get("nick_name"));
		user.put("avatar_address", reqMap.get("avatar_address"));
		user.put("phone_number", reqMap.get("phone_number"));
		user.put("gender", reqMap.get("gender"));
		user.put("last_login_ip", reqMap.get("last_login_ip"));
		user.put("last_login_time", now);
		user.put("course_num", 0);	//新注册用户已购买课程为0
		user.put("create_time", now);
		user.put("update_time", now);
		//位置信息未插入由消息服务处理
		userMapper.insertUser(user);
		
		//2.插入login_info
		Map<String,Object> loginInfo = new HashMap<String,Object>();
		loginInfo.put("user_id", uuid);
		String login_type = (String)reqMap.get("login_type");
		if("0".equals(login_type)){
			loginInfo.put("union_id", reqMap.get("login_id"));
		} else if("1".equals(login_type)){
			
		} else if("2".equals(login_type)){
			
		} else if("3".equals(login_type)){
			loginInfo.put("passwd",reqMap.get("certification"));
		} else if("4".equals(login_type)){
			loginInfo.put("union_id",reqMap.get("unionid"));
			loginInfo.put("web_openid",reqMap.get("web_openid"));
		}
		loginInfo.put("phone_number", reqMap.get("phone_number"));
		loginInfo.put("user_role", Constants.USER_ROLE_NORMAL);
		//位置信息未插入由消息服务处理
		loginInfo.put("create_time", now);
		loginInfo.put("update_time", now);
		loginInfoMapper.insertLoginInfo(loginInfo);

		//3.插入shop(测试)
		Map<String,Object> shopMap = new HashMap<>();
		String shopId = MiscUtils.getUUId();
		shopMap.put("shop_id", shopId);
		shopMap.put("user_id", uuid);
		shopMap.put("shop_seq", "1");
		shopMap.put("shop_name", reqMap.get("nick_name")+"的店铺");
		shopMap.put("avatar_address", "http://onk0xuj5w.bkt.clouddn.com/images/iOSBE62C403A8C35421514DDC15D6B5EC0E.jpeg?w=750&h=750");
		shopMapper.insertShop(shopMap);
		
		Map<String,String> resultMap = new HashMap<String,String>();
		resultMap.put("user_id", uuid);
		resultMap.put("shop_id", shopId);
		return resultMap;
	}


	public int updateUserCommonInfo(Map<String, Object> parameters) {
		return userMapper.updateUser(parameters);
	}

	@Override
	public Map<String, Object> findShopStatisticsByUserId(String user_id) {
		return shopStatisticsMapper.findByUserId(user_id);
	}

	@Override
	public List<Map<String, Object>> findCourseSaleList(Map<String, Object> parameters) {
		return courseSaleMapper.findUserCourse(parameters);
	}

	@Override
	public List<Map<String, Object>> findMyCostList(Map<String, Object> parameters) {
		return tradeBillMapper.findMyCostList(parameters);
	}
	

	public int insertTradeBill(Map<String, Object> parameters) {
		return tradeBillMapper.insertTradeBill(parameters);
	}

	@Override
	public int feedback(Map<String, Object> parameters) {
		// 组装信息
		Date now = new Date();
		parameters.put("feedback_id", MiscUtils.getUUId());
		parameters.put("create_time", now);
		parameters.put("update_time", now);
		return feedbackMapper.insertFeedBack(parameters);
	}

	@Override
	public List<Map<String, Object>> findSystemConfig(List<String> configKeys) {
		return systemConfigMapper.findByConfigKey(configKeys);
	}

    @Override
    public Map<String, Object> findShopInfo(String shopId) {
		return shopMapper.findByShopId(shopId);
    }

    public Map<String, Object> findTradebillByTradeId(String outTradeNo) {
		Map<String,Object> tradeBill = tradeBillMapper.findByOutTradeNo(outTradeNo);
		return tradeBill;
	}

	@Override
	public int insertInform(Map<String, Object> insertPayMap) {
		return informMapper.insertInform(insertPayMap);
	}

	public Map<String, Object> findDistributerByInviteCode(String inviteCode) {
		return distributerMapper.findDistributerByInviteCode(inviteCode);
	}

	@Override
	public int insertDistributer(Map<String, Object> insertMap) {
		return distributerMapper.insertDistributer(insertMap);
	}

	@Override
	public int insertShop(Map<String, Object> insertMap) {
		return shopMapper.insertShop(insertMap);
	}

	@Override
	public int insertShopStatistics(Map<String, Object> insertMap) {
		return shopStatisticsMapper.insertShopStatistics(insertMap);
	}

	@Override
	public int updateShopStatistics(Map<String, Object> insertMap) {
		return shopStatisticsMapper.updateShopStatistics(insertMap);
	}

	@Override
	public int insertDistributerProfit(Map<String, Object> insertMap) {
		return distributerProfitMapper.insertDistributerProfit(insertMap);
	}

	@Override
	public int updatePaymentBill(Map<String, Object> updateMap) {
		return paymentBillMapper.updatePaymentBill(updateMap);
	}

	@Override
	public int updateTradeBill(Map<String, Object> updateMap) {
		return tradeBillMapper.updateTradeBill(updateMap);
	}

	@Override
	public int updateShopCourseNum(Map<String, Object> insertMap) {
		return shopCourseMapper.updateShopCourseNum(insertMap);
	}

	@Override
	public int updateShopCustomer(Map<String, Object> insertMap) {
		return shopCustomerMapper.updateShopCustomer(insertMap);
	}

	@Override
	public int insertShopCustomer(Map<String, Object> insertMap) {
		return shopCustomerMapper.insertShopCustomer(insertMap);
	}

	@Override
	public Map<String, Object> findDistributerByUserId(String userId) {
		return distributerMapper.findDistributerByUserId(userId);
	}

	@Override
	public Map<String, Object> findShopCustomerByShopIdUserId(String shopId, String userId) {
		return shopCustomerMapper.findShopCustomerByShopIdUserId(shopId, userId);
	}

	@Override
	public int insertCourseSale(Map<String, Object> insertMap) {
		return courseSaleMapper.inserCourseSale(insertMap);
	}

	/**
	 * 获取指定条件的系统配置列表
	 */
	@Override
	public List<Map<String, Object>> findSysConfiguration(Map<String, Object> reqMap) {
		return systemConfigMapper.findSysConfiguration(reqMap);
	}

	/**
	 * 获取登录用户购买的指定类型课程id集合
	 */
	@Override
	public List<String> getMyCourseIdList(Map<String, Object> reqMap) {
		List<String> myCourseIdList = tradeBillMapper.selectMyCourseIdList(reqMap);
		return myCourseIdList;
	}

	@Override
	public Map<String, Object> findByPhone(String phone) {
		return userMapper.findByPhone(phone);
	}

	@Override
	public int updateUserPhone(Map<String, Object> userMap) {
		return userMapper.updateUser(userMap);
	}

}
