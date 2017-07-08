package qingning.db.common.mybatis.persistence;
import java.util.List;
import java.util.Map;

public interface CoursesMapper {
	/**
	 * 分页获取课程列表
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> selectCourseList(Map<String, Object> reqMap);
	/**
	 * 获取推荐课程列表
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> selectTjCourseList(Map<String, Object> reqMap);
	/**
	 * 根据id查询课程详情
	 * @param reqMap
	 * @return
	 */
	Map<String, Object> selectCourseById(String courseId);
	/**
	 * 分页获取课程素材或发现列表，并关联查询课程信息
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> selectCoursePosterList(Map<String, Object> reqMap);
	/**
	 * 获取轮播列表
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> selectCarouselList(Map<String, Object> reqMap);
	
}