package qingning.db.common.mybatis.persistence;

import java.util.List;
import java.util.Map;

public interface ExtensionMapper {
	/**
	 * 分页获取官方推广列表
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> selectExtension();
	/**
	 * 获得最新的官方推广列表
	 * @param reqMap 排序位置position、状态status、获取数量page_count等条件
	 * @return
	 */
	List<Map<String, Object>> selectLatestExtensionList(Map<String, Object> reqMap);
	
}
