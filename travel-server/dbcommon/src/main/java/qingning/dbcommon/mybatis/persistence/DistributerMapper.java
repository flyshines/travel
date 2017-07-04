package qingning.dbcommon.mybatis.persistence;

import java.util.List;
import java.util.Map;

public interface DistributerMapper {
	/**
	 * 更新分销者基本信息
	 * @param params
	 * @return
	 */
	int updateDistributer(Map<String, Object> params);
	/**
	 * 新增分销者基本信息
	 * @param params
	 * @return
	 */
	int insertDistributer(Map<String, Object> params);
	
	/**
	 * 根据导师ID查询学员信息列表
	 * @param paramters
	 * @return
	 */
	List<Map<String, Object>> findDistributerByTutorId(Map<String, Object> paramters);

	/**
	 * 获取分销者信息
	 * @param userId
	 * @return
	 */
	Map<String, Object> findDistributerByUserId(String userId);

	/**
	 * 获取分销者信息
	 * @param rqCode 邀请码
	 * @return
	 */
	Map<String, Object> findDistributerByInviteCode(String inviteCode);


}
