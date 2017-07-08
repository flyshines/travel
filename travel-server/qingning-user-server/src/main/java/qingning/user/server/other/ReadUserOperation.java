package qingning.user.server.other;

import qingning.common.entity.RequestEntity;
import qingning.common.util.Constants;
import qingning.server.rpc.CommonReadOperation;
import qingning.server.rpc.manager.IUserModuleServer;

import java.util.Map;

public class ReadUserOperation implements CommonReadOperation {
	private IUserModuleServer userModuleServer;

    public ReadUserOperation(IUserModuleServer userModuleServer) {
        this.userModuleServer = userModuleServer;
    }


    @SuppressWarnings("unchecked")
	@Override
    public Object invokeProcess(RequestEntity requestEntity) throws Exception {    	
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        Object  result = null;
        String userId =(String)reqMap.get("user_id");
        	result = userModuleServer.findUserInfoByUserId(userId);
        return result;
    }
}
