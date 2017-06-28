package qingning.server.rpc.manager;

import java.util.List;
import java.util.Map;

public interface IUserDistributerModuleServer {

	/**
	 * 获取我的学员列表
	 * @return
	 */
	List<Map<String, Object>> getMyStudentList(Map<String, Object> paramters);

	/**
	 * 获取分销者信息
	 * @param userId
	 * @return
	 */
	Map<String, Object> getDistributerInfo(String userId);

	/**
	 * 获取通知（公告、收益通知）
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> getInformByInformType(Map<String, Object> reqMap);

	/**
	 * 获取收益统计信息
	 * @param userId
	 * @return
	 */
	 Map<String, Object> getStatisticsIncome(String userId);

	/**
	 * 根据config_key获取系统配置信息
	 * @param string
	 * @return
	 */
	Map<String, Object> getSystemConfigByKey(String config_key);

	/**
	 * 根据position和消息类型获取最新的消息条目数量
	 * @param reqMap
	 * @return
	 */
	int getInformCountByTypeAndPosition(Map<String, Object> reqMap);

}
