package qingning.dbcommon.mybatis.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface TradeBillMapper {
    int insertTradeBill(Map<String, Object> record);

    int updateTradeBill(Map<String, Object> record);
    Map<String,Object> findByOutTradeNo(String outTradeNo);
    
    /**
     * 获取我的消费记录
     * @param parameters
     * @return
     */
	List<Map<String, Object>> findMyCostList(Map<String, Object> parameters);
	/**
     * 获取指定用户消费的指定课程详情
     * @param userId
     * @param courseId
     * @return
     */
	Map<String, Object> selectBillByUserIdAndCourseId(
			@Param("userId") String userId, 
			@Param("courseId") String courseId);
	/**
	 * 获取登录用户购买的指定类型课程id集合
	 * @param reqMap
	 * @return
	 */
	List<String> selectMyCourseIdList(Map<String, Object> reqMap);
}