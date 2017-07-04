package qingning.shop.server.other;

import java.util.List;
import java.util.Map;

import qingning.common.entity.RequestEntity;
import qingning.server.rpc.CommonReadOperation;
import qingning.server.rpc.manager.IShopModuleServer;

public class ReadShopOperation implements CommonReadOperation {
	private IShopModuleServer shopModuleServer;
	
	public ReadShopOperation(IShopModuleServer shopModuleServer) {
		this.shopModuleServer = shopModuleServer;
	}

	@Override
	public Object invokeProcess(RequestEntity requestEntity) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 获取店铺信息及其统计信息
	 * @param reqEntity
	 * @return
	 */
	public Map<String, Object> getShopDetail(RequestEntity reqEntity) {
		Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
		return shopModuleServer.queryShopDetail(reqMap);
	}
	
	/**
	 * 根据店铺id查询店铺信息
	 * @param reqEntity
	 * @return
	 */
	public Map<String, Object> getShopInfo(RequestEntity reqEntity) {
		Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
		return shopModuleServer.queryShopInfo(reqMap);
	}

	/**
	 * 更新店铺信息
	 * @param reqEntity
	 * @return
	 */
	public int updateShopInfo(RequestEntity reqEntity) {
		Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
		return shopModuleServer.updateShopInfo(reqMap);
	}

	/**
	 * 分页获取店铺课程列表，并关联查询出课程信息
	 * @param reqEntity
	 * @return
	 */
	public List<Map<String, Object>> getShopCourseList(RequestEntity reqEntity) {
		Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
		return shopModuleServer.queryShopCourseList(reqMap);
	}

	/**
	 * 获取官方公众号二维码详情
	 * @param rqParam
	 * @return
	 */
	public Map<String, Object> getPublicRqCode(Map<String, Object> rqParam) {
		return shopModuleServer.querySystemConfig(rqParam);
	}

	/**
	 * 获取店铺统计信息
	 * @param reqEntity
	 * @return
	 */
	public Map<String, Object> getShopStatistics(RequestEntity reqEntity) {
		Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
		return shopModuleServer.getShopStatistics(reqMap);
	}

	/**
	 * 获取店铺课程id集合
	 * @param reqEntity
	 * @return
	 */
	public List<String> getShopCourseIdList(RequestEntity reqEntity) {
		Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
		return shopModuleServer.getShopCourseIdList(reqMap);
	}

	/**
	 * 分页获取店铺课程销售记录列表
	 * @param reqEntity
	 * @return
	 */
	public List<Map<String, Object>> getCourseSaleList(RequestEntity reqEntity) {
		Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
		return shopModuleServer.getCourseSaleList(reqMap);
	}

	/**
	 * 分页获取店铺的客户统计，并关联查询客户信息
	 * @param reqEntity
	 * @return
	 */
	public List<Map<String, Object>> getCustomerStatisticsList(RequestEntity reqEntity) {
		Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
		return shopModuleServer.getCustomerStatisticsList(reqMap);
	}

	/**
	 * 根据条件获取店铺课程记录
	 * @param reqEntity
	 * @return
	 */
	public Map<String, Object> getShopCourseInfo(RequestEntity reqEntity) {
		Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
		return shopModuleServer.getShopCourseInfo(reqMap);
	}

	/**
	 * 上架课程，并更新店铺统计信息表中的课程数量及更新时间
	 * @param reqEntity
	 * @return
	 */
	public int addShopCourse(RequestEntity reqEntity) {
		Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
		return shopModuleServer.addShopCourse(reqMap);
	}

	/**
	 * 更新店铺课程，并更新相应的统计信息
	 * @param reqEntity
	 * @return
	 */
	public int updateShopCourse(RequestEntity reqEntity) {
		Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
		return shopModuleServer.updateShopCourse(reqMap);
	}

}
