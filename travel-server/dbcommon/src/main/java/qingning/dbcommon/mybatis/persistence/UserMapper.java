package qingning.dbcommon.mybatis.persistence;

import java.util.Map;

public interface UserMapper {
	int insertUser(Map<String, Object> record);
	int updateUser(Map<String, Object> record);
    Map<String,Object> findByUserId(String user_id);
    
	Map<String,Object> findByPhone(String phone);
}