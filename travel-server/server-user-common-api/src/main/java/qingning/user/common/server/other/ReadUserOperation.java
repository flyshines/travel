package qingning.user.common.server.other;

import qingning.common.entity.RequestEntity;
import qingning.common.util.Constants;
import qingning.server.rpc.CommonReadOperation;
import qingning.server.rpc.manager.IUserCommonModuleServer;

import java.util.Map;

public class ReadUserOperation implements CommonReadOperation {
    private IUserCommonModuleServer commonModuleServer;

    public ReadUserOperation(IUserCommonModuleServer commonModuleServer) {
        this.commonModuleServer = commonModuleServer;
    }


    @SuppressWarnings("unchecked")
	@Override
    public Object invokeProcess(RequestEntity requestEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        Object  result = null;
        String userId =(String)reqMap.get("user_id");
        if(Constants.SYS_READ_USER_COURSE_LIST.equals(requestEntity.getFunctionName())){
        	result = commonModuleServer.findCourseIdByStudent(reqMap);
        } else {
        	result = commonModuleServer.findUserInfoByUserId(userId);
        }
        
        return result;
    }
}
