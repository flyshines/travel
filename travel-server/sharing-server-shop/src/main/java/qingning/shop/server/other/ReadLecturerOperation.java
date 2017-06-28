package qingning.shop.server.other;

import qingning.common.entity.RequestEntity;
import qingning.common.util.Constants;
import qingning.server.rpc.CommonReadOperation;
import qingning.server.rpc.manager.IShopModuleServer;
import qingning.server.rpc.manager.IUserDistributerModuleServer;

import java.util.Map;

/**
 * 包名: qingning.lecture.server.other
 * 描 述:
 * 创建日期: 2016/12/4
 */
public class ReadLecturerOperation implements CommonReadOperation {
	private IUserDistributerModuleServer userModuleServer;

    public ReadLecturerOperation(IUserDistributerModuleServer userModuleServer) {
        this.userModuleServer = userModuleServer;
    }


    @SuppressWarnings("unchecked")
	@Override
    public Object invokeProcess(RequestEntity requestEntity) throws Exception {
    	Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
//    	return userModuleServer.findLectureByLectureId((String)reqMap.get("lecture_id"));
    	return null;
    }
}
