package qingning.db.common.mybatis.persistence;

import java.util.Map;

public interface TicketMapper {

	/**
	 * 插入记录
	 * @param paramters
	 * @return
	 */
	int insert(Map<String, Object> paramters);

	/**
	 * 查询记录
	 * @param key
	 * @return
	 */
	Map<String, Object> selectByPrimaryKey(String key);

	/**查询票价
	 * @return
	 */
	Map<String,Object> selectTicketPrice();
}