package qingning.dbcommon.mybatis.persistence;
import java.util.Map;

public interface CourseMapper {
	Map<String,Object> findCourseByCourseId(String courseId);
}