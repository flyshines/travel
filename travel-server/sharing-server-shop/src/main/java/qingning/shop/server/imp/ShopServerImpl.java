package qingning.shop.server.imp;

import qingning.common.entity.QNLiveException;
import qingning.common.entity.RequestEntity;
import qingning.common.util.*;
import qingning.server.AbstractQNLiveServer;
import qingning.server.annotation.FunctionName;
import qingning.server.rpc.manager.IShopModuleServer;
import qingning.shop.server.other.*;
import java.util.*;

public class ShopServerImpl extends AbstractQNLiveServer {
	
	private IShopModuleServer shopModuleServer;
	
	private ReadShopOperation readShopOperation;
    
    @Override
    public void initRpcServer() {
        if (shopModuleServer == null) {
        	shopModuleServer = this.getRpcService("shopModuleServer");

        	readShopOperation = new ReadShopOperation(shopModuleServer);
        }
    }
    
    /**
     * 获取店铺信息
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("queryShop")
    public Map<String, Object> queryShop(RequestEntity reqEntity) throws Exception {
    	Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
    	Map<String, Object> resultMap = new HashMap<>();
    	String user_id = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
    	reqMap.put("user_id", user_id);
    	reqEntity.setParam(reqMap);
    	/*
    	 * 获取店铺信息，并关联查询店铺的统计信息
    	 */
    	Map<String, Object> shopDetail = readShopOperation.getShopDetail(reqEntity);
    	if(shopDetail == null){
    		throw new QNLiveException("120017");
    	}
    	
    	/*
    	 * 判断登录用户是否为店铺店主
    	 */
    	/*if(user_id == null || user_id.isEmpty() || shopDetail == null ||
    			!user_id.equals(String.valueOf(shopDetail.get("user_id")))){
    		//accessToken中的user_id为空或者和店铺店主id不同
    		throw new QNLiveException("100013");
    	}*/
    	
    	/*
    	 * 格式化店铺统计信息
    	 */
    	if(shopDetail != null && !shopDetail.isEmpty()){
	    	Map<String, Object> statisticsInfo = new HashMap<String, Object>();
	    	statisticsInfo.put("order_today", shopDetail.get("day_sale"));
	    	statisticsInfo.put("order_month", shopDetail.get("month_sale"));
	    	statisticsInfo.put("course_total", shopDetail.get("course_num"));
	    	statisticsInfo.put("visit_times", shopDetail.get("visit_times"));
	    	resultMap.put("statistics_info", statisticsInfo);
	    	resultMap.putAll(shopDetail);
    	}
    	
		return resultMap;
    	
    }

    /**
     * 更新店铺信息
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("updateShop")
    public Map<String, Object> updateShop(RequestEntity reqEntity) throws Exception {
    	Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
    	Map<String, Object> resultMap = new HashMap<>();
    	String user_id = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
    	reqMap.put("user_id", user_id);
    	reqEntity.setParam(reqMap);
    	/*
    	 * 根据shop_id获取店铺基本信息
    	 */
    	Map<String, Object> shopDetail = readShopOperation.getShopInfo(reqEntity);
    	
    	/*
    	 * 判断登录用户是否为店铺店主
    	 */
    	if(shopDetail == null){
    		throw new QNLiveException("120017");
    	}else if(user_id == null || user_id.isEmpty() || 
    			!user_id.equals(String.valueOf(shopDetail.get("user_id")))){
    		//accessToken中的user_id为空或者和店铺店主id不同
    		throw new QNLiveException("100013");
    	}
    	
    	/*
    	 * 调用服务更新店铺信息
    	 */
    	int updateNum = readShopOperation.updateShopInfo(reqEntity);
    	if(1 != updateNum){	//不是更新一条数据，说明更新异常
			throw new QNLiveException("100005");
		}
    	
		return resultMap;
    	
    }
    
    /**
     * 获取店铺中的课程
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("getShopCourseList")
    public Map<String, Object> getShopCourseList(RequestEntity reqEntity) throws Exception {
    	Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
    	Map<String, Object> resultMap = new HashMap<>();
    	
    	/*
    	 * 获取店铺的课程列表，并关联查询出课程信息
    	 */
    	List<Map<String, Object>> shopCourseList = readShopOperation.getShopCourseList(reqEntity);
    	
    	/*
    	 * 判断是否需要返回店铺信息
    	 * 如果share_shop_id有效（分享链接发起请求），且position=0（分页中的第一页）时:
    	 * 	从数据表t_shop表中查询该店铺的信息，并按position进行倒序分页排序
    	 */
    	String share_shop_id = (String) reqMap.get("share_shop_id");
    	long position = (long) reqMap.get("position");
    	if(share_shop_id != null && !share_shop_id.isEmpty() && 0 == position){	//表示web访问
    		Map<String, Object> shopDetail = readShopOperation.getShopInfo(reqEntity);
    		
    		/*
    		 * 分享链接发起才返回分销收益和官方公众号二维码
    		 */
    		//获取官方公众号二维码
    		Map<String, Object> rqParam = new HashMap<>();
    		rqParam.put("config_key", "public_rq_code_address");
    		Map<String, Object> rqCodeDetail = readShopOperation.getPublicRqCode(rqParam);
    		if(shopDetail != null && rqCodeDetail != null){
    			shopDetail.put("public_rq_code_address", rqCodeDetail.get("config_value"));
    		}
    		
    		resultMap.put("shop_info", shopDetail);
    		
    		//遍历课程列表，清空分销收益
    		if(shopCourseList != null){
	    		for(int i=0, size=shopCourseList.size(); i<size; i++){
	    			Map<String, Object> courseDetail = shopCourseList.get(i);
	    			courseDetail.put("distributer_income", "");
	    		}
    		}
    	}else{	//表示客户端访问
    		//店铺课程管理发起请求才需要返回店铺课程总数
    		Map<String, Object> shopStatistics = readShopOperation.getShopStatistics(reqEntity);
    		if(shopStatistics != null){
    			resultMap.put("course_total", shopStatistics.get("course_num"));
    		}
    	}
    	
    	resultMap.put("course_info_list", shopCourseList);
		return resultMap;
    }
    
    /**
     * 获取店铺中的课程id集合
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("getShopCourseIdList")
    public Map<String, Object> getShopCourseIdList(RequestEntity reqEntity) throws Exception {
    	Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
    	Map<String, Object> resultMap = new HashMap<>();
    	
    	/*
    	 * 判断登录用户是否为该店铺店主
    	 */
    	String user_id = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
    	//根据shop_id获取店铺基本信息
    	Map<String, Object> shopDetail = readShopOperation.getShopInfo(reqEntity);
    	if(shopDetail == null){
    		throw new QNLiveException("120017");
    	}
    	if(user_id == null || user_id.isEmpty() || 
    			!user_id.equals(String.valueOf(shopDetail.get("user_id")))){
    		//accessToken中的user_id为空或者和店铺店主id不同
    		throw new QNLiveException("100013");
    	}
    	
    	/*
    	 * 获取店铺的课程id列表
    	 */
    	List<String> shopCourseIdList = readShopOperation.getShopCourseIdList(reqEntity);
    	
    	resultMap.put("course_id_str", shopCourseIdList);
		return resultMap;
    }
    
    /**
     * 获取店铺销售统计信息
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("saleStatistics")
    public Map<String, Object> saleStatistics(RequestEntity reqEntity) throws Exception {
    	Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
    	Map<String, Object> resultMap = new HashMap<>();
    	
    	/*
    	 * 获取店铺的基本信息，并关联查询其统计信息
    	 */
    	String user_id = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
    	reqMap.put("user_id", user_id);
    	reqEntity.setParam(reqMap);
    	Map<String, Object> shopAndStatisticsDetail = readShopOperation.getShopDetail(reqEntity);
    	if(shopAndStatisticsDetail == null){
    		throw new QNLiveException("120017");
    	}
    	
    	/*
    	 * 判断登录用户是否为该店铺店主
    	 */
    /*	if(user_id == null || user_id.isEmpty() || 
    			!user_id.equals(String.valueOf(shopAndStatisticsDetail.get("user_id")))){
    		//accessToken中的user_id为空或者和店铺店主id不同
    		throw new QNLiveException("100013");
    	}*/
    	
    	/*
    	 * 分页获取店铺的销售记录列表
    	 */
    	List<Map<String, Object>> courseSaleList = readShopOperation.getCourseSaleList(reqEntity);
    	
    	/*
    	 * 格式化：拼接课程信息json对象、购买用户json对象
    	 */
    	if(courseSaleList != null){
	    	for(int i=0, size=courseSaleList.size(); i<size; i++){
	    		Map<String, Object> courseSale = courseSaleList.get(i);
	    		Map<String, Object> courseInfo = new HashMap<>();
	    		Map<String, Object> userInfo = new HashMap<>();
	    		courseInfo.put("course_title", courseSale.get("course_title"));
	    		courseInfo.put("sale_money", courseSale.get("sale_money"));
	    		courseInfo.put("distributer_income", courseSale.get("distributer_income"));
	    		userInfo.put("nick_name", courseSale.get("nick_name"));
	    		
	    		courseSale.put("course_info", courseInfo);
	    		courseSale.put("user_info", userInfo);
	    	}
    	}
    	
    	if(shopAndStatisticsDetail != null){
    		resultMap.putAll(shopAndStatisticsDetail);
    	}
    	resultMap.put("sale_record_info_list", courseSaleList);
		return resultMap;
    }
    
    /**
     * 获取店铺销售记录
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("getSaleRecord")
    public Map<String, Object> getSaleRecord(RequestEntity reqEntity) throws Exception {
    	Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
    	Map<String, Object> resultMap = new HashMap<>();
    	
    	/*
    	 * 获取店铺的基本信息
    	 */
    	Map<String, Object> shopInfo = readShopOperation.getShopInfo(reqEntity);
    	
    	if(shopInfo == null){
    		throw new QNLiveException("120017");
    	}
    	/*
    	 * 判断登录用户是否为该店铺店主
    	 */
    	String user_id = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
    	if(user_id == null || user_id.isEmpty() || 
    			!user_id.equals(String.valueOf(shopInfo.get("user_id")))){
    		//accessToken中的user_id为空或者和店铺店主id不同
    		throw new QNLiveException("100013");
    	}
    	
    	/*
    	 * 分页获取店铺的销售记录列表
    	 */
    	List<Map<String, Object>> courseSaleList = readShopOperation.getCourseSaleList(reqEntity);
    	
    	/*
    	 * 格式化：拼接课程信息json对象、购买用户json对象
    	 */
    	if(courseSaleList != null){
	    	for(int i=0, size=courseSaleList.size(); i<size; i++){
	    		Map<String, Object> courseSale = courseSaleList.get(i);
	    		Map<String, Object> courseInfo = new HashMap<>();
	    		Map<String, Object> userInfo = new HashMap<>();
	    		courseInfo.put("course_title", courseSale.get("course_title"));
	    		courseInfo.put("sale_money", courseSale.get("sale_money"));
	    		courseInfo.put("distributer_income", courseSale.get("distributer_income"));
	    		userInfo.put("nick_name", courseSale.get("nick_name"));
	    		
	    		courseSale.put("course_info", courseInfo);
	    		courseSale.put("user_info", userInfo);
	    	}
	    	
	    	resultMap.put("sale_record_info_list", courseSaleList);
    	}
		return resultMap;
    }
    
    /**
     * 获取店铺客户统计信息
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("getCustomerStatistics")
    public Map<String, Object> getCustomerStatistics(RequestEntity reqEntity) throws Exception {
    	Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
    	Map<String, Object> resultMap = new HashMap<>();
    	
    	/*
    	 * 获取店铺信息及其统计信息
    	 */
    	String user_id = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
    	reqMap.put("user_id", user_id);
    	reqEntity.setParam(reqMap);
    	Map<String, Object> shopDetail = readShopOperation.getShopDetail(reqEntity);
    	if(shopDetail == null){
    		throw new QNLiveException("120017");
    	}
    	
    	/*
    	 * 判断登录用户是否为该店铺店主
    	 */
    /*	if(user_id == null || user_id.isEmpty() || 
    			!user_id.equals(String.valueOf(shopDetail.get("user_id")))){
    		//accessToken中的user_id为空或者和店铺店主id不同
    		throw new QNLiveException("100013");
    	}*/
    	
    	/*
    	 * 分页获取店铺的客户统计，并关联查询客户信息
    	 */
    	List<Map<String, Object>> customerStatisticsList = readShopOperation.getCustomerStatisticsList(reqEntity);
    	
    	/*
    	 * 格式化：拼接购买用户json对象
    	 */
    	if(customerStatisticsList != null){
	    	for(int i=0, size=customerStatisticsList.size(); i<size; i++){
	    		Map<String, Object> customerStatistics = customerStatisticsList.get(i);
	    		Map<String, Object> customerInfo = new HashMap<>();
	    		customerInfo.put("nick_name", customerStatistics.get("nick_name"));
	    		
	    		customerStatistics.put("customer_info", customerInfo);
	    	}
	    	
	    	resultMap.put("customer_info_list", customerStatisticsList);
    	}
    	resultMap.put("customer_num_total", shopDetail.get("customer_num_total"));
		return resultMap;
    }
    
    /**
     * 获取店铺指定客户的购买记录
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("getCustomerBuyRecord")
    public Map<String, Object> getCustomerBuyRecord(RequestEntity reqEntity) throws Exception {
    	Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
    	Map<String, Object> resultMap = new HashMap<>();
    	
    	/*
    	 * 获取店铺的基本信息
    	 */
    	Map<String, Object> shopInfo = readShopOperation.getShopInfo(reqEntity);
    	if(shopInfo == null){
    		throw new QNLiveException("120017");
    	}
    	
    	/*
    	 * 判断登录用户是否为该店铺店主
    	 */
    	String user_id = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
    	if(user_id == null || user_id.isEmpty() || 
    			!user_id.equals(String.valueOf(shopInfo.get("user_id")))){
    		//accessToken中的user_id为空或者和店铺店主id不同
    		throw new QNLiveException("100013");
    	}
    	
    	/*
    	 * 分页获取店铺的销售记录列表
    	 */
    	List<Map<String, Object>> customerSaleList = readShopOperation.getCourseSaleList(reqEntity);
    	
    	/*
    	 * 格式化：拼接课程信息json对象、购买用户json对象
    	 */
    	if(customerSaleList != null){
	    	for(int i=0, size=customerSaleList.size(); i<size; i++){
	    		Map<String, Object> customerSale = customerSaleList.get(i);
	    		Map<String, Object> courseInfo = new HashMap<>();
	    		Map<String, Object> userInfo = new HashMap<>();
	    		courseInfo.put("course_title", customerSale.get("course_title"));
	    		userInfo.put("nick_name", customerSale.get("nick_name"));
	    		
	    		customerSale.put("course_info", courseInfo);
	    		customerSale.put("user_info", userInfo);
	    	}
	    	
	    	resultMap.put("buy_record_info_list", customerSaleList);
    	}
		return resultMap;
    }
    
    /**
     * 上下架课程
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("putawayCourse")
    public Map<String, Object> putawayCourse(RequestEntity reqEntity) throws Exception {
    	Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
    	Map<String, Object> resultMap = new HashMap<>();
    	
    	/*
    	 * 获取店铺的基本信息
    	 */
    	Map<String, Object> shopInfo = readShopOperation.getShopInfo(reqEntity);
    	if(shopInfo == null){
    		throw new QNLiveException("120017");
    	}
    	
    	/*
    	 * 判断登录用户是否为该店铺店主
    	 */
    	String user_id = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
    	if(user_id == null || user_id.isEmpty() || 
    			!user_id.equals(String.valueOf(shopInfo.get("user_id")))){
    		//accessToken中的user_id为空或者和店铺店主id不同
    		throw new QNLiveException("100013");
    	}
    	
    	/*
    	 * 判断课程是否已加入该店铺
    	 */
    	//根据店铺id，课程id查询店铺课程表
    	Map<String, Object> shopCourseInfo = readShopOperation.getShopCourseInfo(reqEntity);
    	//判断课程状态
    	
    	//判断是否需要进行上下架操作，若重复操作上下架直接返回操作成功
    	long putaway_type = (long) reqMap.get("putaway_type");
    	int putawayNum = 0;	//记录上下架更新的记录数
    	if(shopCourseInfo == null && putaway_type == 0){
    		//未加入课程而进行上架操作，需要进行数据库插入操作，并更新店铺统计信息表中的课程数量及更新时间
    		reqMap.put("shop_course_id", MiscUtils.getUUId());	//生成数据库主键
    		reqMap.put("sale_num", 0);
    		reqMap.put("status", "0");
    		reqMap.put("update_time", new Date());
    		reqEntity.setParam(reqMap);
    		putawayNum = readShopOperation.addShopCourse(reqEntity);
    	}else if(shopCourseInfo != null){	//曾经该店铺加入过该课程
    		int status = Integer.valueOf((String) shopCourseInfo.get("status"));
    		if(status != putaway_type){	//目前状态与要操作状态不一致，需要进行数据库更新，并更新相应的统计信息
    			reqMap.put("shop_course_id", shopCourseInfo.get("shop_course_id"));
    			reqMap.put("status", putaway_type);
    			reqEntity.setParam(reqMap);
    			putawayNum = readShopOperation.updateShopCourse(reqEntity);
    		}
    	}
    	
		return resultMap;
    }
}
