package qingning.dbcommon.mybatis.persistence;

import java.util.List;
import java.util.Map;

public interface ShopCourseMapper {

    /**
     * 更新店铺课程信息
     * @param insertMap
     * @return
     */
    int updateShopCourse(Map<String, Object> insertMap);
    /**
	 * 分页获取店铺课程列表，并关联查询出课程信息
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> selectShopCourseList(Map<String, Object> reqMap);
	/**
	 * 根据条件获取店铺课程记录
	 * @param reqMap
	 * @return
	 */
	Map<String, Object> selectShopCourse(Map<String, Object> reqMap);
	/**
	 * 上架课程
	 * @param reqMap
	 * @return
	 */
	int insertShopCourse(Map<String, Object> reqMap);
	/**
	 * 获取店铺课程id集合
	 * @param reqMap
	 * @return
	 */
	List<String> selectShopCourseIdList(Map<String, Object> reqMap);
	/**
	 * 分页获取店铺课程销售记录列表
	 * @param reqMap
	 * @return
	 */
	List<Map<String, Object>> selectCourseSaleList(Map<String, Object> reqMap);
	/**
	 * 更新店铺课程已售数量
	 * @param insertMap
	 * @return
	 */
	int updateShopCourseNum(Map<String, Object> insertMap);
}