package qingning.dbcommon.mybatis.persistence;
import java.util.List;
import java.util.Map;

public interface ShopMapper {
    /**
     * 查找店铺信息
     * @param shopId
     * @return
     */
	Map<String,Object> findByShopId(String shopId);

    /**
     * 生成店铺信息
     * @param insertMap
     * @return
     */
    int insertShop(Map<String, Object> insertMap);
    
    /**
	 * 根据店铺id查询店铺信息
	 * @param reqMap
	 * @return
	 */
	Map<String, Object> selectShopInfo(Map<String, Object> reqMap);
	/**
	 * 更新店铺信息
	 * @param reqMap
	 * @return
	 */
	int updateShopInfo(Map<String, Object> reqMap);
	/**
	 * 查询系统配置表详情
	 * @param rqParam
	 * @return
	 */
	Map<String, Object> selectSystemConfig(Map<String, Object> rqParam);
	/**
	 * 分页获取店铺的客户统计，并关联查询客户信息
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> selectCustomerStatistics(Map<String, Object> reqMap);
	/**
	 * 根据用户id获取店铺信息
	 * @param userId
	 * @return
	 */
	Map<String, Object> selectShopByUserId(String userId);
}