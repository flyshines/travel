package qingning.db.common.mybatis.persistence;
import qingning.db.common.mybatis.pageinterceptor.domain.PageBounds;
import qingning.db.common.mybatis.pageinterceptor.domain.PageList;

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
	PageList<Map<String,Object>> selectShopList(Map<String, Object> param, PageBounds page);


	/**
	 * 更新店铺信息
	 * @param reqMap
	 * @return
	 */
	int updateShopInfo(Map<String, Object> reqMap);
}