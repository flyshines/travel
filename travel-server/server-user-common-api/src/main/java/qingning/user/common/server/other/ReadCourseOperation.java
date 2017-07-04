package qingning.user.common.server.other;

import qingning.common.entity.RequestEntity;
import qingning.server.rpc.CommonReadOperation;
import qingning.server.rpc.manager.IUserCommonModuleServer;
import java.util.Map;

/**
 * 包名: distribute.lecture.server.other
 * 描 述:
 * 创建日期: 2016/12/4
 */
public class ReadCourseOperation implements CommonReadOperation {
    private IUserCommonModuleServer userCommonModuleServer;

    public ReadCourseOperation(IUserCommonModuleServer commonModuleServer) {
        this.userCommonModuleServer = commonModuleServer;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Object invokeProcess(RequestEntity requestEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        return userCommonModuleServer.findCourseByCourseId(reqMap.get("course_id").toString());
    }
}
