package qingning.shop.server.other;

import qingning.common.entity.RequestEntity;
import qingning.server.rpc.CommonReadOperation;
import qingning.server.rpc.manager.IUserCommonModuleServer;
import qingning.server.rpc.manager.IUserDistributerModuleServer;

import java.util.Map;

public class ReadRoomDistributerOperation implements CommonReadOperation {
    private IUserDistributerModuleServer userModuleServer;

    public ReadRoomDistributerOperation(IUserDistributerModuleServer userModuleServer) {
        this.userModuleServer = userModuleServer;
    }


    @SuppressWarnings("unchecked")
	@Override
    public Object invokeProcess(RequestEntity requestEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
//        return userModuleServer.findAvailableRoomDistributer(reqMap);
        return null;
    }
}
