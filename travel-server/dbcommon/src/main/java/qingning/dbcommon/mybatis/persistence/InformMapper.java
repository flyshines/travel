package qingning.dbcommon.mybatis.persistence;

import java.util.List;
import java.util.Map;

public interface InformMapper {
	/**
	 * 获取通知消息
	 * inform_type=0，获取公告
	 * inform_type=1，获取收益通知
	 * @param paramters
	 * @return
	 */
	public List<Map<String, Object>> getInformByInformType(Map<String, Object> paramters);

	/**
	 * 新增通知
	 * @param paramters
	 * @return
	 */
	public int insertInform(Map<String, Object> paramters);

	/**
	 * 根据position和消息类型获取最新的消息条目数量
	 * @param reqMap
	 * @return
	 */
	public int selectCountByTypeAndPosition(Map<String, Object> reqMap);
}