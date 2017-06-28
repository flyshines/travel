package qingning.dbcommon.mybatis.persistence;

import java.util.Map;

public interface DistributerProfitMapper {

	/**
	 * 新增分销者收益信息
	 * @param insertMap
	 * @return
	 */
	int insertDistributerProfit(Map<String, Object> insertMap);
}
