package qingning.db.common.mybatis.persistence;

import org.apache.ibatis.annotations.Param;
import qingning.db.common.mybatis.pageinterceptor.domain.PageBounds;
import qingning.db.common.mybatis.pageinterceptor.domain.PageList;

import java.util.Map;

public interface UserMapper {
	int insertUser(Map<String, Object> record);
    int insertShop(Map<String, Object> user);

    int updateUser(Map<String, Object> record);
    Map<String,Object> findByUserId(String user_id);
    
	Map<String,Object> findByPhone(String phone);

    int insertVipUser(Map<String, Object> userInfo);

	Map<String,Object> selectVipUserById(String user_id);

	int updateVipUser(Map<String, Object> userInfo);

    Map<String,Object> selectVipUserBySign(String sign);

    int insertVisit(Map<String, Object> param);

    int selectUserVisitCount(@Param("user_id") String user_id, @Param("shop_id") String shop_id);

    /**后台登录
     * @param reqMap
     * @return
     */
    Map<String,Object> selectAdminUserByMobile(Map<String, Object> reqMap);

    void updateAdminUserByAllMap(Map<String, Object> adminUserMap);

    /**收入列表
     * @param param
     * @param page
     * @return
     */
    PageList<Map<String,Object>> selectIncomeList(Map<String, Object> param, PageBounds page);

    int selectCountUser();

    /**用户列表
     * @param param
     * @param page
     * @return
     */
    PageList<Map<String,Object>> selectUserList(Map<String, Object> param, PageBounds page);
}