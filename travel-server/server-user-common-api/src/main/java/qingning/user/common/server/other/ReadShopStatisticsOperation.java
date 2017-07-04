package qingning.user.common.server.other;

import java.util.Map;

import qingning.common.entity.RequestEntity;
import qingning.server.rpc.CommonReadOperation;
import qingning.server.rpc.manager.IUserCommonModuleServer;

public class ReadShopStatisticsOperation implements CommonReadOperation {
    private IUserCommonModuleServer userCommonModuleServer;

    public ReadShopStatisticsOperation(IUserCommonModuleServer commonModuleServer) {
        this.userCommonModuleServer = commonModuleServer;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Object invokeProcess(RequestEntity requestEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        String user_id = (String) reqMap.get("user_id");
        Object obj = userCommonModuleServer.findShopStatisticsByUserId(user_id);
        return obj;
    }
}
