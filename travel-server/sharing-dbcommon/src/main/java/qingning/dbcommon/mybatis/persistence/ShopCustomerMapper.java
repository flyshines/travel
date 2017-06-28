package qingning.dbcommon.mybatis.persistence;

import java.util.Map;

public interface ShopCustomerMapper {

    /**
     * 新增店铺客户信息
     * @param insertMap
     * @return
     */
    int insertShopCustomer(Map<String, Object> insertMap);

    /**
     * 更新店铺客户信息
     * @param insertMap
     * @return
     */
    int updateShopCustomer(Map<String, Object> insertMap);

    /**
     * 查询店铺客户信息
     * @param shopId
     * @param userId
     * @return
     */
    Map<String,Object> findShopCustomerByShopIdUserId(String shopId, String userId);
}