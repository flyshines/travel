package qingning.server.rpc.manager;


import java.util.List;
import java.util.Map;

import qingning.common.entity.RequestEntity;

public interface ICourseModuleServer {
	/**
	 * 分页获取课程列表
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> queryCourseList(Map<String, Object> reqMap);
	/**
	 * 获取推荐课程列表
	 * 	按数据表t_course中的buy_times降序排序
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> queryTjCourseList(Map<String, Object> reqMap);
	/**
	 * 获取轮播列表
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> queryCarouselList(Map<String, Object> reqMap);
	/**
	 * 获取课程详情
	 * @param reqMap
	 * @return
	 */
	Map<String, Object> queryCourseDetail(Map<String, Object> reqMap);
	/**
	 * 获取指定用户消费的指定课程详情
	 * @param reqMap
	 * @return
	 */
	Map<String, Object> queryBillDetail(Map<String, Object> reqMap);
	/**
	 * 分页获取课程素材或发现列表，并关联查询课程信息
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> queryCoursePosterList(Map<String, Object> reqMap);
	/**
	 * 分页获取该课程的评论列表，关联查询出用户信息
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> queryCourseCommentList(Map<String, Object> reqMap);
	/**
	 * 新增课程评论
	 * @param reqMap
	 * @return
	 */
	int addCourseComment(Map<String, Object> reqMap);
}
