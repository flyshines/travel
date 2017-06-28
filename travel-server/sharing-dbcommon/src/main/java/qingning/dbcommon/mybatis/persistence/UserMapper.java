package qingning.dbcommon.mybatis.persistence;

import java.util.Map;

public interface UserMapper {
	int insertUser(Map<String, Object> record);
	int updateUser(Map<String, Object> record);
	int updateLiveRoomNumForUser(Map<String, Object> record);
    Map<String,Object> findByUserId(String user_id);
    
    /**
     * 更新分销者信息
     * @param parameters
     * @return
     */
	int updateDistributer(Map<String, Object> parameters);

	Map<String,Object> findByPhone(String phone);
}