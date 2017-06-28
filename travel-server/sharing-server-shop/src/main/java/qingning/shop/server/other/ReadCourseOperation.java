package qingning.shop.server.other;

import qingning.common.entity.RequestEntity;
import qingning.server.rpc.CommonReadOperation;
import qingning.server.rpc.manager.IUserDistributerModuleServer;

import java.util.Map;

/**
 * 包名: qingning.shop.server.other
 * 描 述:
 * 创建日期: 2016/12/4
 */
public class ReadCourseOperation implements CommonReadOperation {
    private IUserDistributerModuleServer userModuleServer;

    public ReadCourseOperation(IUserDistributerModuleServer userModuleServer) {
        this.userModuleServer = userModuleServer;
    }


    @SuppressWarnings("unchecked")
	@Override
    public Object invokeProcess(RequestEntity requestEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();

//        return userModuleServer.findCourseByCourseId((String)reqMap.get("course_id"));
        return null;
    }
}
