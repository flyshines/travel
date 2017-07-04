package qingning.dbcommon.mybatis.persistence;

import java.util.List;
import java.util.Map;

public interface CourseSaleMapper {
	
	/**
	 * 获取用户购买的课程
	 */
    List<Map<String,Object>> findUserCourse(Map<String, Object> record);

	/**
	 * 新增课程销售记录
	 * @param insertMap
	 * @return
	 */
	int inserCourseSale(Map<String, Object> insertMap);
}
