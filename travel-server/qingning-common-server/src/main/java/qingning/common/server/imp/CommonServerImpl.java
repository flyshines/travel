package qingning.common.server.imp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.FetchRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import qingning.common.dj.DjSendMsg;
import qingning.common.entity.AccessToken;
import qingning.common.entity.QNLiveException;
import qingning.common.entity.RequestEntity;
import qingning.common.entity.TemplateData;
import qingning.common.server.other.*;
import qingning.common.server.util.DES;
import qingning.common.util.*;
import qingning.server.AbstractQNLiveServer;
import qingning.server.JedisBatchCallback;
import qingning.server.JedisBatchOperation;
import qingning.server.annotation.FunctionName;
import qingning.server.rpc.manager.ICommonModuleServer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonServerImpl extends AbstractQNLiveServer {

    private static final Logger logger   = LoggerFactory.getLogger(CommonServerImpl.class);
    private ICommonModuleServer commonModuleServer;
    private ReadUserOperation readUserOperation;

    @Override
    public void initRpcServer() {
        if(commonModuleServer == null){
            commonModuleServer = this.getRpcService("commonModuleServer");
            readUserOperation = new ReadUserOperation(commonModuleServer);
        }
    }

    private static Auth auth;
    static {
        auth = Auth.create (MiscUtils.getConfigKey("qiniu_AK"), MiscUtils.getConfigKey("qiniu_SK"));
    }






}
