package qingning.dbcommon.mybatis.persistence;

import java.util.List;
import java.util.Map;

public interface SystemConfigMapper {
	List<Map<String,Object>> findByConfigKey(List<String> configKeys);
	/**
	 * 获取系统配置
	 * @param insertMap
	 * @return
	 */
	List<Map<String, Object>> findSysConfiguration(Map<String, Object> insertMap);
	
}