package qingning.db.common.mybatis.persistence;

import java.util.List;
import java.util.Map;

public interface LoginInfoMapper {
    /**
     * 获取用户的登录信息
     * @param record
     * @return
     */
    Map<String,Object> getLoginInfoByLoginIdAndLoginType(Map<String, Object> record);
    void updateUserRole(Map<String, Object> updateMap);
    Map<String,Object> findLoginInfoByUserId(String user_id);
    int insertLoginInfo(Map<String, Object> record);
    int updateLoginInfo(Map<String, Object> record);
    List<String> findLoginInfoByUserIds(Map<String, Object> map);
}