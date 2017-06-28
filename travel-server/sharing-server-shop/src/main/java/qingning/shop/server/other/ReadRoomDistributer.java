package qingning.shop.server.other;

import qingning.common.entity.RequestEntity;
import qingning.common.util.MiscUtils;
import qingning.server.rpc.CommonReadOperation;
import qingning.server.rpc.manager.IUserDistributerModuleServer;

import java.util.Map;

public class ReadRoomDistributer implements CommonReadOperation {
    private IUserDistributerModuleServer userModuleServer;

    public ReadRoomDistributer(IUserDistributerModuleServer userModuleServer) {
        this.userModuleServer = userModuleServer;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Object invokeProcess(RequestEntity requestEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        reqMap.put("current_date", MiscUtils.getEndTimeOfToday());
//        return userModuleServer.findAvailableRoomDistributer(reqMap);
        return null;
    }
}
