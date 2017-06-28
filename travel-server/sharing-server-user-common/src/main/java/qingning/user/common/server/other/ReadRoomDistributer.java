package qingning.user.common.server.other;

import qingning.common.entity.RequestEntity;
import qingning.common.util.MiscUtils;
import qingning.server.rpc.CommonReadOperation;
import qingning.server.rpc.manager.IUserCommonModuleServer;

import java.util.Map;

public class ReadRoomDistributer implements CommonReadOperation {
    private IUserCommonModuleServer commonModuleServer;

    public ReadRoomDistributer(IUserCommonModuleServer commonModuleServer) {
        this.commonModuleServer = commonModuleServer;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Object invokeProcess(RequestEntity requestEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        reqMap.put("current_date", MiscUtils.getEndTimeOfToday());
        return commonModuleServer.findAvailableRoomDistributer(reqMap);
    }
}
