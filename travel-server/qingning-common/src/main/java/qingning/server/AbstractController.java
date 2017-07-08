package qingning.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import qingning.common.entity.MessageEntity;
import qingning.common.entity.QNLiveException;
import qingning.common.entity.RequestEntity;
import qingning.common.entity.ResponseEntity;
import qingning.common.util.MqUtils;

import java.util.*;

public abstract class AbstractController {
    private static final Logger logger   = LoggerFactory.getLogger(AbstractController.class);
    protected MqUtils mqUtils;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    protected ClassPathXmlApplicationContext applicationContext;
    @Autowired
    protected MessageEntity message;
    @Autowired
    protected ServiceManger serviceManger;

    protected Map<String,Object> serverUrlInfoMap;
    private List<Map<String,Object>> serverUrlInfoList;
    protected Long serverUrlInfoUpdateTime;
    private List<Map<String,Object>> rewardConfigurationList;
    protected Map<String,Object> rewardConfigurationMap;
    protected Long rewardConfigurationTime;
    private List<Map<String,Object>> processRewardConfigurationList;

    public RequestEntity createRequestEntity(String serviceName, String function, String accessToken, String version){
        RequestEntity requestEntity = new RequestEntity();
        requestEntity.setAccessToken(accessToken);
        requestEntity.setVersion(version);
        requestEntity.setServerName(serviceName);
        requestEntity.setFunctionName(function);
        requestEntity.setTimeStamp(System.currentTimeMillis());
        return requestEntity;
    }

    public ResponseEntity process(RequestEntity reqEntity,ServiceManger serviceManger, MessageEntity message) throws Exception{
        QNSharingServer servicer = serviceManger.getServer(reqEntity.getServerName(), reqEntity.getVersion());
        if(servicer==null){
            throw new QNLiveException("000001");
        }
        if(servicer.isReturnObject(reqEntity)){
            throw new QNLiveException("000103");
        }
        Object returnValue=process(reqEntity, servicer, serviceManger, message);
        ResponseEntity respEntity = new ResponseEntity();
        respEntity.setCode("0");
        respEntity.setMsg(message.getMessages("0"));
        respEntity.setReturnData(returnValue);
        return respEntity;
    }

    public Object processWithObjectReturn(RequestEntity reqEntity,ServiceManger serviceManger, MessageEntity message) throws Exception{
        QNSharingServer servicer = serviceManger.getServer(reqEntity.getServerName(), reqEntity.getVersion());
        if(servicer==null){
            throw new QNLiveException("000001");
        }
        if(!servicer.isReturnObject(reqEntity)){
            throw new QNLiveException("000103");
        }
        return process(reqEntity, servicer, serviceManger, message);
    }

    protected void initControlResource(){
        if(mqUtils==null){
            mqUtils = new MqUtils(rabbitTemplate);
        }
    }

    private Object process(RequestEntity reqEntity, QNSharingServer servicer,ServiceManger serviceManger, MessageEntity message) throws Exception{
        initControlResource();

        servicer.setApplicationContext(applicationContext);
        servicer.setMqUtils(mqUtils);
        servicer.initRpcServer();
        servicer.validateRequestParamters(reqEntity);
        Object returnValue=servicer.invoke(reqEntity);
        return servicer.processReturnValue(reqEntity, returnValue);
    }


}
