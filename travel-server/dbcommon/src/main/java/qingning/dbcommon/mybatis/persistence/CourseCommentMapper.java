package qingning.dbcommon.mybatis.persistence;

import java.util.List;
import java.util.Map;

public interface CourseCommentMapper {
	/**
	 * 分页获取该课程的评论列表，关联查询出用户信息
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> selectCommentListByCourseId(Map<String, Object> reqMap);
	/**
	 * 插入一条课程评论
	 * @param reqMap
	 * @return
	 */
	int insertCourseComment(Map<String, Object> reqMap);

}
