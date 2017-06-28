package qingning.user.common.server.other;

import qingning.common.entity.RequestEntity;
import qingning.common.util.Constants;
import qingning.server.rpc.CommonReadOperation;
import qingning.server.rpc.manager.IUserCommonModuleServer;

import java.util.Map;

public class ReadDistributerOperation implements CommonReadOperation {
    private IUserCommonModuleServer commonModuleServer;

    public ReadDistributerOperation(IUserCommonModuleServer commonModuleServer) {
        this.commonModuleServer = commonModuleServer;
    }


    @SuppressWarnings("unchecked")
	@Override
    public Object invokeProcess(RequestEntity requestEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        String function = requestEntity.getFunctionName();
        Object result = null;
        if(Constants.FUNCTION_DISTRIBUTERS_ROOM_RQ.equals(function)){
        	result = commonModuleServer.findDistributionRoomDetail(reqMap);
        } else {
        	result = commonModuleServer.findByDistributerId((String)reqMap.get("distributer_id"));
        }
        return result;
    }
}
