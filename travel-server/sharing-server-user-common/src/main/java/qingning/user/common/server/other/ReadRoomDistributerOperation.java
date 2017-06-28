package qingning.user.common.server.other;

import qingning.common.entity.RequestEntity;
import qingning.server.rpc.CommonReadOperation;
import qingning.server.rpc.manager.IUserCommonModuleServer;

import java.util.Map;

public class ReadRoomDistributerOperation implements CommonReadOperation {
    private IUserCommonModuleServer commonModuleServer;

    public ReadRoomDistributerOperation(IUserCommonModuleServer commonModuleServer) {
        this.commonModuleServer = commonModuleServer;
    }


    @SuppressWarnings("unchecked")
	@Override
    public Object invokeProcess(RequestEntity requestEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        return commonModuleServer.findAvailableRoomDistributer(reqMap);
    }
}
