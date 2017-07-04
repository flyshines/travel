package qingning.user.common.server.imp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.FetchRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.ModelAndView;

import qingning.common.entity.QNLiveException;
import qingning.common.entity.RequestEntity;
import qingning.common.util.*;
import qingning.server.AbstractQNLiveServer;
import qingning.server.annotation.FunctionName;
import qingning.server.rpc.manager.IUserCommonModuleServer;
import qingning.user.common.server.other.*;
import qingning.user.common.server.util.ServerUtils;
import redis.clients.jedis.Jedis;

import java.security.Timestamp;
import java.util.*;

public class UserCommonServerImpl extends AbstractQNLiveServer {
    private static final Logger logger = LoggerFactory.getLogger(UserCommonServerImpl.class);
    private IUserCommonModuleServer userCommonModuleServer;
    private ReadUserOperation readUserOperation;
    private ReadShopStatisticsOperation readShopStatisticsOperation;
    private ReadCourseOperation readCourseOperation;

    @Override
    public void initRpcServer() {
        if (userCommonModuleServer == null) {
            userCommonModuleServer = this.getRpcService("userCommonModuleServer");
            readUserOperation = new ReadUserOperation(userCommonModuleServer);
            readShopStatisticsOperation = new ReadShopStatisticsOperation(userCommonModuleServer);
            readCourseOperation = new ReadCourseOperation(userCommonModuleServer);

        }
    }
    private static Auth auth;
    static {
        auth = Auth.create(MiscUtils.getConfigByKey("qiniu_AK"), MiscUtils.getConfigByKey("qiniu_SK"));
    }

    /**
     * 获取服务器时间，从而获取客户登录信息
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @FunctionName("logUserInfo")
    public Map<String, Object> collectClientInformation(RequestEntity reqEntity) throws Exception {
        // Jedis jedis = jedisUtils.getJedis();
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        long loginTime = System.currentTimeMillis();
        if (!"2".equals(reqMap.get("status"))) {
            reqMap.put("create_time", loginTime);
            reqMap.put("create_date", MiscUtils.getDate(loginTime));
            if (!MiscUtils.isEmpty(reqEntity.getAccessToken())) {
                String user_id = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
                reqMap.put("user_id", user_id);
                // Map<String,String> userInfo = CacheUtils.readUser(user_id,
                // reqEntity, readUserOperation, jedisUtils);
                // reqMap.put("gender", userInfo.get("gender"));
                Map<String, String> queryParam = new HashMap<>();
                queryParam.put(Constants.CACHED_KEY_ACCESS_TOKEN_FIELD, reqEntity.getAccessToken());
                String accessTokenKey = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_ACCESS_TOKEN, queryParam);
                // Map<String,String> accessTokenInfo =
                // jedis.hgetAll(accessTokenKey);
                Map<String, String> accessTokenInfo = null;
                if (MiscUtils.isEmpty(accessTokenInfo)) {
                    Map<String, Object> queryMap = new HashMap<String, Object>();
                    String login_id = (String) reqMap.get("login_id");
                    String login_type = (String) reqMap.get("login_type");
                    queryMap.put("login_type", login_type);
                    queryMap.put("login_id", login_id);
                    accessTokenInfo = new HashMap<String, String>();
                    MiscUtils.converObjectMapToStringMap(
                            userCommonModuleServer.getLoginInfoByLoginIdAndLoginType(queryMap), accessTokenInfo);
                }
                // reqMap.put("record_time", userInfo.get("create_time"));
                reqMap.put("old_subscribe", accessTokenInfo.get("subscribe"));
                // reqMap.put("country", userInfo.get("country"));
                // reqMap.put("province", userInfo.get("province"));
                // reqMap.put("city", userInfo.get("city"));
                // reqMap.put("district", userInfo.get("district"));
                String web_openid = (String) accessTokenInfo.get("web_openid");
                reqMap.put("subscribe", null);
                reqMap.put("web_openid", null);
                // if(MiscUtils.isEmpty(web_openid)){
                // reqMap.put("subscribe", "0");
                // } else {
                // reqMap.put("web_openid", web_openid);
                // }
                // String user_role =
                // MiscUtils.convertString(accessTokenInfo.get("user_role"));
                // if(user_role.contains(Constants.USER_ROLE_LECTURER)){
                // reqMap.put("live_room_build", "1");
                // } else {
                // reqMap.put("live_room_build", "0");
                // }
                String status = (String) reqMap.get("status");
                if ("1".equals(status) || "3".equals(status)) {
                    Map<String, String> updateValue = new HashMap<String, String>();
                    updateValue.put("last_login_time", loginTime + "");
                    updateValue.put("last_login_ip", (String) reqMap.get("ip"));
                    // String plateform = (String)userInfo.get("plateform");
                    // String newPalteForm = (String)reqMap.get("plateform");
                    // if(MiscUtils.isEmpty(plateform)){
                    // plateform = newPalteForm;
                    // } else if(!MiscUtils.isEmpty(newPalteForm) &&
                    // plateform.indexOf(newPalteForm) == -1){
                    // plateform=plateform+","+newPalteForm;
                    // }
                    // updateValue.put("plateform", plateform);
                    Map<String, Object> query = new HashMap<String, Object>();
                    query.put("user_id", user_id);
                    String key = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_USER, query);
                    // jedis.hmset(key, updateValue);
                    // jedis.sadd(Constants.CACHED_UPDATE_USER_KEY, user_id);
                }
            }
            // RequestEntity requestEntity =
            // this.generateRequestEntity("LogServer",
            // Constants.MQ_METHOD_ASYNCHRONIZED, "logUserInfo", reqMap);
            // mqUtils.sendMessage(requestEntity);
        }
        loginTime = System.currentTimeMillis();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("server_time", System.currentTimeMillis());
        Map<String, Object> versionReturnMap = new HashMap<>();
        // 增加下发版本号逻辑
        // 平台：0： 微信 1：andriod 2:IOS
        if (!"0".equals(reqMap.get("plateform"))) {
            // Map<String,String> versionInfoMap =
            // CacheUtils.readAppVersion(reqMap.get("plateform").toString(),
            // reqEntity, readAPPVersionOperation, jedisUtils, true);
            // if(! MiscUtils.isEmpty(versionInfoMap)){
            // //状态 0：关闭 1：开启
            // if(versionInfoMap.get("status").equals("1")){
            // if(MiscUtils.isEmpty(reqMap.get("version")) ||
            // compareVersion(reqMap.get("plateform").toString(),
            // versionInfoMap.get("version_no"),
            // reqMap.get("version").toString())){
            // Map<String,Object> cacheMap = new HashMap<>();
            // cacheMap.put(Constants.CACHED_KEY_APP_VERSION_INFO_FIELD,
            // reqMap.get("plateform"));
            // String force_version_key =
            // MiscUtils.getKeyOfCachedData(Constants.FORCE_UPDATE_VERSION,
            // cacheMap);
            // ((Map<String, Object>)
            // reqEntity.getParam()).put("force_version_key",
            // force_version_key);
            // Map<String,String> forceVersionInfoMap =
            // CacheUtils.readAppForceVersion(reqMap.get("plateform").toString(),
            // reqEntity, readForceVersionOperation, jedisUtils, true);
            // versionReturnMap.put("is_force","2");
            // if(! MiscUtils.isEmpty(forceVersionInfoMap)){
            // if(MiscUtils.isEmpty(reqMap.get("version")) ||
            // compareVersion(reqMap.get("plateform").toString(),
            // versionInfoMap.get("version_no"),
            // reqMap.get("version").toString())){
            // versionReturnMap.put("is_force","1");//是否强制更新 1强制更新 2非强制更新
            // }
            // }
            // versionReturnMap.put("version_no",versionInfoMap.get("version_no"));
            // versionReturnMap.put("update_desc",versionInfoMap.get("update_desc"));
            // versionReturnMap.put("version_url",versionInfoMap.get("version_url"));
            // resultMap.put("version_info", versionReturnMap);
            // }
            // }
            // }
        }

        return resultMap;
    }


    @SuppressWarnings("unchecked")
    @FunctionName("qiNiuUploadToken")
    public Map<String, Object> getQiNiuUploadToken(RequestEntity reqEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        long expiredTime = 3600;
        String token = null;
        String url = null;
        if ("1".equals(reqMap.get("upload_type"))) { //图片
            url = IMMsgUtil.configMap.get("images_space_domain_name");
 
            token = auth.uploadToken(IMMsgUtil.configMap.get("image_space"), null, expiredTime, new StringMap()
                    .putNotEmpty("returnBody", "{\"key\": $(key), \"hash\": $(etag), \"width\": $(imageInfo.width), \"height\": $(imageInfo.height)}"));

        } else if ("2".equals(reqMap.get("upload_type"))) { //音频
            url = IMMsgUtil.configMap.get("audio_space_domain_name");
//            StringMap putPolicy = new StringMap()
//                    .put("persistentPipeline","qnlive-audio-convert")//设置私有队列处理
//                    .put("persistentOps", "avthumb/mp3/ab/64k")
//                    .put("persistentNotifyUrl",MiscUtils.getConfigByKey("qiniu-audio-transfer-persistent-notify-url"));//转码策略

            token = auth.uploadToken(IMMsgUtil.configMap.get("audio_space"), null, expiredTime, (StringMap) null);

        }

        resultMap.put("upload_token", token);
        resultMap.put("access_prefix_url", url);
        return resultMap;
    }

    /**
     * 移动端登录
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @FunctionName("userLogin")
    public Map<String, Object> userLogin(RequestEntity reqEntity) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        Map<String, Object> loginInfoMap = userCommonModuleServer.getLoginInfoByLoginIdAndLoginType(reqMap);

        //int login_type_input = Integer.parseInt(reqMap.get("login_type").toString());

        //switch (login_type_input) {
           // case 0: // 微信登录
                // 如果登录信息为空，则进行注册
        if (MiscUtils.isEmpty(loginInfoMap)) {
            Jedis jedis = jedisUtils.getJedis();
            // 设置默认用户头像
            String transferAvatarAddress = (String) reqMap.get("avatar_address");
            if (!MiscUtils.isEmpty(transferAvatarAddress)) {
                try {
                    //transferAvatarAddress = qiNiuFetchURL(reqMap.get("avatar_address").toString());
                } catch (Exception e) {
                    transferAvatarAddress = null;
                }
            }
            if (MiscUtils.isEmpty(transferAvatarAddress)) {
                transferAvatarAddress = MiscUtils.getConfigByKey("default_avatar_address");
            }

            reqMap.put("avatar_address", transferAvatarAddress);
            reqMap.put("last_login_ip", reqEntity.getIp());

            if (reqMap.get("nick_name") == null || StringUtils.isBlank(reqMap.get("nick_name").toString())) {
                reqMap.put("nick_name", "用户" + jedis.incrBy(Constants.CACHED_KEY_USER_NICK_NAME_INCREMENT_NUM, 1));
            }

            Map<String, String> dbResultMap = userCommonModuleServer.initializeRegisterUser(reqMap);

            // 生成access_token，将相关信息放入缓存，构造返回参数
            processLoginSuccess(1, dbResultMap, null, resultMap);

        } else {
            // 构造相关返回参数 TODO
            processLoginSuccess(2, null, loginInfoMap, resultMap);
        }
                /*break;

            case 1: // QQ登录
                // TODO
                break;

            case 2: // 手机号登录
                if (MiscUtils.isEmpty(loginInfoMap)) {
                    // 抛出用户不存在
                    throw new QNLiveException("120002");
                } else {
                    // 校验用户名和密码
                    // 登录成功
                    if (reqMap.get("certification").toString().equals(loginInfoMap.get("passwd").toString())) {
                        // 构造相关返回参数 TODO
                        processLoginSuccess(2, null, loginInfoMap, resultMap);
                    } else {
                        // 抛出用户名或者密码错误
                        throw new QNLiveException("120001");
                    }
                }
                break;
            case 4:
                if (MiscUtils.isEmpty(loginInfoMap)) {
                    throw new QNLiveException("120002");
                }
                break;
        }
*/
        return resultMap;
    }

    /**
     * 查询用户信息
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @FunctionName("userInfo")
    public Map<String, Object> getUserInfo(RequestEntity reqEntity) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        // 个人基本信息
        String queryType = reqMap.get("query_type").toString();
        Map<String, Object> param = (Map)reqEntity.getParam();
        param.put("user_id",userId);
        Map<String, String> values = CacheUtils.readUser(userId, reqEntity, readUserOperation, jedisUtils);
        reqMap.put("user_id", userId);
        resultMap.put("avatar_address", values.get("avatar_address"));
        resultMap.put("nick_name", MiscUtils.RecoveryEmoji(values.get("nick_name")));
        resultMap.put("level", values.get("level"));
        resultMap.put("rq_card_address", values.get("rq_card_address"));
        resultMap.put("course_num", values.get("course_num"));


		reqMap.put("user_id", userId);
		resultMap.put("avatar_address", values.get("avatar_address"));
		resultMap.put("nick_name", MiscUtils.RecoveryEmoji(values.get("nick_name")));
		resultMap.put("level", values.get("level"));
		resultMap.put("rq_card_address", values.get("rq_card_address"));
		resultMap.put("course_num", values.get("course_num"));
		resultMap.put("subscribe", values.get("subscribe"));

		// 获取收益和余额
		String role = values.get("user_role");
		if (queryType.equals("2") && (!MiscUtils.isEmpty(role) && role.indexOf(Constants.USER_ROLE_DISTRIBUTER) > -1)) {
			Map<String, String> shopMap = CacheUtils.readShop(userId, reqEntity, readShopStatisticsOperation);//			resultMap.put("course_num", shopMap.get("course_num"));
            resultMap.put("balance", shopMap.get("balance"));
            resultMap.put("sale_num_total", shopMap.get("sale_num_total"));
            resultMap.put("student_offer", shopMap.get("student_offer"));
        }

        return resultMap;
    }

    /**
     * 更新用户信息
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("updateUserInfo")
    public Map<String, Object> updateUserInfo(RequestEntity reqEntity) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        reqMap.put("user_id", userId);

        String nick_name = (String) reqMap.get("nick_name");
        String avatar_address = (String) reqMap.get("avatar_address");
        String rq_card_address = (String) reqMap.get("rq_card_address");

        if (!MiscUtils.isEmpty(nick_name) || !MiscUtils.isEmpty(avatar_address)
                || !MiscUtils.isEmpty(rq_card_address)) {
            Map<String, Object> parameters = new HashMap<String, Object>();
            if (nick_name != null) {
                parameters.put("nick_name", nick_name);
            }
            if (avatar_address != null) {
                parameters.put("avatar_address", avatar_address);
            }
            if (!parameters.isEmpty()) {
                parameters.put("user_id", userId);
                parameters.put("update_time", new Date());
                int count = userCommonModuleServer.updateUserCommonInfo(parameters);
                if (count < 1) {
                    throw new QNLiveException("000104");
                } else {
                    // 更新用户信息缓存
                    Map<String, Object> parameter = new HashMap<String, Object>();
                    parameter.put(Constants.CACHED_KEY_USER_FIELD, userId);
                    String cachedKey = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_USER, parameter);

                    Map<String, String> cachedValues = new HashMap<String, String>();
                    MiscUtils.converObjectMapToStringMap(parameters, cachedValues);
                    jedisUtils.getJedis().hmset(cachedKey, cachedValues);
                }
            }
        }
        return new HashMap<String, Object>();
    }

    /**
     * 获取微信配置信息
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @FunctionName("weiXinConfiguration")
    public Map<String, String> getWeiXinConfiguration(RequestEntity reqEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        String JSApiTIcket = WeiXinUtil.getJSApiTIcket(jedisUtils.getJedis());
        return WeiXinUtil.sign(JSApiTIcket, reqMap.get("url").toString());
    }

    /**
     * 获取我购买的课程记录
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("getMyCourseList")
    public Map<String, Object> getMyCourseList(RequestEntity reqEntity) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        @SuppressWarnings("unchecked")
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        reqMap.put("user_id", userId);

        List<Map<String, Object>> resList = userCommonModuleServer.findCourseSaleList(reqMap);
        resultMap.put("course_info_list", resList);
        return resultMap;
    }

    /**
     * 获取我的消费记录
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("getMyCostList")
    public Map<String, Object> getMyCostList(RequestEntity reqEntity) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        @SuppressWarnings("unchecked")
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        reqMap.put("user_id", userId);

        List<Map<String, Object>> resList = userCommonModuleServer.findMyCostList(reqMap);
        resultMap.put("cost_info_list", resList);
        return resultMap;
    }

    /**
     * 微信授权code登录
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @FunctionName("weixinCodeUserLogin")
    public Map<String, Object> weixinCodeUserLogin(RequestEntity reqEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("key", "1");// 钥匙 用于在controller判断跳转的页面
        String code = reqMap.get("code").toString();
        // 1.传递授权code及相关参数，调用微信验证code接口
        JSONObject getCodeResultJson = WeiXinUtil.getUserInfoByCode(code);
        if (getCodeResultJson == null || getCodeResultJson.getInteger("errcode") != null
                || getCodeResultJson.getString("openid") == null) {
            if (getCodeResultJson.getString("openid") == null) {
                resultMap.put("key", "0");
                return resultMap;
            }
            throw new QNLiveException("120008");
        }
        String openid = getCodeResultJson.getString("openid");

        // 1.2如果验证成功，则得到用户的union_id和用户的access_token。
        // 1.2.1根据 union_id查询数据库
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("login_type", "4");// 4.微信code方式登录
        queryMap.put("web_openid", openid);
        Map<String, Object> loginInfoMap = userCommonModuleServer.getLoginInfoByLoginIdAndLoginType(queryMap);

        // 1.2.1.1如果用户存在则进行登录流程
        if (loginInfoMap != null) {
            processLoginSuccess(2, null, loginInfoMap, resultMap);
            return resultMap;
        } else {
            // 1.2.1.2如果用户不存在，则根据用户的open_id和用户的access_token调用微信查询用户信息接口，得到用户的头像、昵称等相关信息
            String userWeixinAccessToken = getCodeResultJson.getString("access_token");
            JSONObject userJson = WeiXinUtil.getUserInfoByAccessToken(userWeixinAccessToken, openid);
            // 根据得到的相关用户信息注册用户，并且进行登录流程。
            if (userJson == null || userJson.getInteger("errcode") != null || userJson.getString("unionid") == null) {
                if (userJson.getString("unionid") == null) {
                    resultMap.put("key", "0");
                    return resultMap;
                }
                throw new QNLiveException("120008");
            }

            queryMap.clear();
            queryMap.put("login_type", "0");// 0.微信方式登录
            queryMap.put("login_id", userJson.getString("unionid"));
            Map<String, Object> loginInfoMapFromUnionid = userCommonModuleServer.getLoginInfoByLoginIdAndLoginType(queryMap);
            if (loginInfoMapFromUnionid != null) {
                // 将open_id更新到login_info表中
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("user_id", loginInfoMapFromUnionid.get("user_id").toString());
                updateMap.put("web_openid", openid);
                userCommonModuleServer.updateUserWebOpenIdByUserId(updateMap);
                processLoginSuccess(2, null, loginInfoMapFromUnionid, resultMap);
                return resultMap;
            }

            String nickname = userJson.getString("nickname");
            String sex = userJson.getString("sex");
            String headimgurl = userJson.getString("headimgurl");

            // 设置默认用户头像
            if (MiscUtils.isEmpty(headimgurl)) {
                reqMap.put("avatar_address", MiscUtils.getConfigByKey("default_avatar_address"));// TODO
            } else {
                String transferAvatarAddress = qiNiuFetchURL(headimgurl);
                reqMap.put("avatar_address", transferAvatarAddress);
            }

            if (MiscUtils.isEmpty(nickname)) {
                Jedis jedis = jedisUtils.getJedis();
                reqMap.put("nick_name", "用户" + jedis.incrBy(Constants.CACHED_KEY_USER_NICK_NAME_INCREMENT_NUM, 1));// TODO
            } else {
                reqMap.put("nick_name", nickname);
            }

            if (MiscUtils.isEmpty(sex)) {
                reqMap.put("gender", "2");// TODO
            }

            // 微信性别与本系统性别转换
            // 微信用户性别 用户的性别，值为1时是男性，值为2时是女性，值为0时是未知
            if (sex.equals("1")) {
                reqMap.put("gender", "1");// TODO
            }
            if (sex.equals("2")) {
                reqMap.put("gender", "0");// TODO
            }
            if (sex.equals("0")) {
                reqMap.put("gender", "2");// TODO
            }

            String unionid = userJson.getString("unionid");
            reqMap.put("unionid", unionid);
            reqMap.put("web_openid", openid);
            reqMap.put("login_type", "4");
            Map<String, String> dbResultMap = userCommonModuleServer.initializeRegisterUser(reqMap);

            // 生成access_token，将相关信息放入缓存，构造返回参数
            processLoginSuccess(1, dbResultMap, null, resultMap);
            return resultMap;
        }
    }

    /**
     * 生成具体微信订单
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @FunctionName("generateWeixinPayBill")
    public Map<String, String> generateWeixinPayBill(RequestEntity reqEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();

        Map<String, Object> insertMap = new HashMap<>();
        String tradeId = MiscUtils.getWeiXinId();//TODO
        insertMap.put("trade_id", tradeId);
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        insertMap.put("user_id", userId);

        String profit_type = reqMap.get("profit_type").toString();
        String payGoodName = null;
        Integer totalFee = 0;
        if ("0".equals(profit_type)) {
            //1.检测课程是否存在，课程不存在则给出提示（ 课程不存在，120009）
            String courseId = reqMap.get("course_id").toString();
            Map<String, Object> query = new HashMap<String, Object>();
            query.put("course_id", courseId);
            Map<String, String> courseMap = CacheUtils.readCourse(courseId, generateRequestEntity(null, null, null, query), readCourseOperation, jedisUtils, false);

            if (MiscUtils.isEmpty(courseMap)) { //如果课程不存在
                throw new QNLiveException("120009");
            }

            String shopId = reqMap.get("shop_id").toString();
            insertMap.put("shop_id", shopId);
            insertMap.put("course_id", courseMap.get("course_id"));
            insertMap.put("amount", courseMap.get("course_price"));
            totalFee = Integer.parseInt(courseMap.get("course_price"));
            payGoodName = MiscUtils.getConfigByKey("weixin_pay_buy_course_good_name") + "-" + MiscUtils.RecoveryEmoji(courseMap.get("course_title"));
            insertMap.put("remark", courseMap.get("course_title"));
            Map<String, Object> shopMap = userCommonModuleServer.findShopInfo(shopId);
            insertMap.put("shop_name", shopMap.get("shop_name"));

        } else {// 加盟
            // 获取系统当前加盟费
            List<String> paramters = new ArrayList<>();
            String inviteJoinMoney = "invite_join_money";
            String inviteJoinGoodName = "invite_join_good_name";
            paramters.add(inviteJoinMoney);
            paramters.add(inviteJoinGoodName);
            List<Map<String, Object>> systemConfigMap = userCommonModuleServer.findSystemConfig(paramters);
            for (Map<String, Object> sysConfig : systemConfigMap) {
                if (inviteJoinMoney.equals(sysConfig.get("config_value"))) {
                    insertMap.put("amount", sysConfig.get("config_value").toString());
                } else if (inviteJoinGoodName.equals(sysConfig.get(inviteJoinGoodName))) {
                    payGoodName = sysConfig.get("config_value").toString();
                }
            }
            insertMap.put("invite_code", reqMap.get("invite_code"));
        }

		//3.插入t_trade_bill表 交易信息表
		insertMap.put("profit_type",profit_type);
		insertMap.put("status", 0);
		insertMap.put("create_time", new Date());
		insertMap.put("update_time", new Date());

		userCommonModuleServer.insertTradeBill(insertMap);
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(Constants.CACHED_KEY_ACCESS_TOKEN_FIELD, reqEntity.getAccessToken());
        String key = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_ACCESS_TOKEN, query);

        //4.调用微信生成预付单接口
        String terminalIp = reqMap.get("remote_ip_address").toString();
        String outTradeNo = tradeId;
        String platform = (String) reqMap.get("platform");
        String openid = null;

        boolean isWeb = false;
        if (platform == null || platform.equals("0")) {
            Map<String, String> userMap = jedisUtils.getJedis().hgetAll(key);
            openid = userMap.get("web_openid");
            if (MiscUtils.isEmpty(openid)) {
                openid = "ovwtFwDW1gPQ4UH2E0Gff33V3fp0";
            }
            isWeb = true;
        }

        payGoodName = new String(payGoodName.getBytes("UTF-8"));

        Map<String, String> payResultMap = TenPayUtils.sendPrePay(payGoodName, totalFee, terminalIp, outTradeNo, openid, platform);

        //5.处理生成微信预付单接口
        if (payResultMap.get("return_code").equals("FAIL")) {
            //更新交易表
            Map<String, Object> failUpdateMap = new HashMap<>();
            failUpdateMap.put("status", "3");
            failUpdateMap.put("close_reason", "生成微信预付单失败 " + payResultMap.get("return_msg") + payResultMap.get("err_code_des"));
            failUpdateMap.put("trade_id", tradeId);
            userCommonModuleServer.closeTradeBill(failUpdateMap);
            throw new QNLiveException("120015");
        } else {
            //成功，则需要插入支付表
            Map<String, Object> insertPayMap = new HashMap<>();
            insertPayMap.put("payment_id", MiscUtils.getUUId());
            insertPayMap.put("trade_id", tradeId);
            insertPayMap.put("pre_pay_no", payResultMap.get("prepay_id"));
            insertPayMap.put("create_time", new Date());
//			insertPayMap.put("update_time",new Date());
			
            userCommonModuleServer.insertPaymentBill(insertPayMap);
            //返回相关参数给前端.
            SortedMap<String, String> resultMap = new TreeMap<>();
            if (isWeb) {
                resultMap.put("appId", MiscUtils.getConfigByKey("appid"));
                resultMap.put("package", "prepay_id=" + payResultMap.get("prepay_id"));
            } else {
                resultMap.put("prepayId", payResultMap.get("prepay_id"));
                resultMap.put("package", "Sign=WXPay");
            }
            resultMap.put("nonceStr", payResultMap.get("random_char"));
            resultMap.put("signType", "MD5");
            resultMap.put("timeStamp", System.currentTimeMillis() / 1000 + "");

            String paySign = null;
            if (isWeb) {
                paySign = TenPayUtils.getSign(resultMap, platform);
            } else {
                SortedMap<String, String> signMap = new TreeMap<>();
                signMap.put("appid", MiscUtils.getConfigByKey("app_app_id"));
                signMap.put("partnerid", MiscUtils.getConfigByKey("weixin_app_pay_mch_id"));
                signMap.put("prepayid", resultMap.get("prepayId"));
                signMap.put("package", resultMap.get("package"));
                signMap.put("noncestr", resultMap.get("nonceStr"));
                signMap.put("timestamp", resultMap.get("timeStamp"));
                paySign = TenPayUtils.getSign(signMap, platform);
            }

            resultMap.put("paySign", paySign);

            return resultMap;
        }
    }

    /**
     * 微信支付成功后回调的方法
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @FunctionName("handleWeixinPayResult")
    public String handleWeixinPayResult(RequestEntity reqEntity) throws Exception {
        Jedis jedis = jedisUtils.getJedis();
        String resultStr = TenPayConstant.FAIL;// 默认失败
        SortedMap<String, String> requestMapData = (SortedMap<String, String>) reqEntity.getParam();
        String tradeId = requestMapData.get("out_trade_no");

        // 检查该订单是否已处理
        Map<String, Object> billMap = userCommonModuleServer.findTradebillByTradeId(tradeId);
        if (billMap == null) {
            logger.debug("====> 系统找不到该流水号：: " + tradeId);
            return TenPayConstant.FAIL;
        }
//		if (billMap != null && billMap.get("status").equals("2")) {
//			logger.debug("====>　已经处理完成, 不需要继续。流水号是: " + tradeId);
//			return TenPayConstant.SUCCESS;
//		}

        if (!TenPayUtils.isValidSign(requestMapData)) {// MD5签名成功
            logger.debug(" ===> 微信notify Md5 验签成功 <=== ");
            if ("SUCCESS".equals(requestMapData.get("return_code"))
                    && "SUCCESS".equals(requestMapData.get("result_code"))) {
                Date now = new Date();
                // 更新支付信息状态
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("trade_id", tradeId);
                updateMap.put("status", "2");
                updateMap.put("payment", requestMapData.get("total_fee"));
                updateMap.put("trade_no", requestMapData.get("transaction_id"));
                updateMap.put("update_time", now);
                userCommonModuleServer.updatePaymentBill(updateMap);

                // 更新交易信息状态
                userCommonModuleServer.updateTradeBill(updateMap);

                String userId = billMap.get("user_id").toString();
                String profitType = billMap.get("profit_type").toString();
                if ("0".equals(profitType)) {
                    // 购买课程交易
                    String shopId = billMap.get("shop_id").toString();
                    Map<String, Object> shopMap = userCommonModuleServer.findShopInfo(shopId);
                    String courseId = billMap.get("course_id").toString();
                    Map<String, Object> courseMap = userCommonModuleServer.findCourseByCourseId(courseId);
                    Map<String, Object> shopStatisticsMap = userCommonModuleServer.findShopStatisticsByUserId(shopMap.get("user_id").toString());

                    Map<String, Object> insertMap = new HashMap<>();
                    // 更新店铺课程表的销售数量
                    insertMap.put("shop_id", shopId);
                    insertMap.put("course_id", courseId);
                    insertMap.put("sale_num", 1);
                    userCommonModuleServer.updateShopCourseNum(insertMap);
                    // 新增店铺客户表
                    insertMap.clear();
                    insertMap.put("user_id", userId);
                    insertMap.put("shop_id", shopId);
                    insertMap.put("cost_money", requestMapData.get("total_fee"));
                    boolean isNewCustomer = false;
                    String shopCustomerId = MiscUtils.getUUId();
                    if (userCommonModuleServer.updateShopCustomer(insertMap) == 0) {
                        insertMap.put("shop_customer_id", shopCustomerId);
                        userCommonModuleServer.insertShopCustomer(insertMap);
                        isNewCustomer = true;
                    } else {
                        Map<String, Object> shopCustomerMap = userCommonModuleServer.findShopCustomerByShopIdUserId(shopId, userId);
                        shopCustomerId = shopCustomerMap.get("shop_customer_id").toString();
                    }

                    // 新增课程销售表
                    insertMap.clear();
                    insertMap.put("record_id", MiscUtils.getUUId());
                    insertMap.put("shop_customer_id", shopCustomerId);
                    insertMap.put("user_id", userId);
//					insertMap.put("nick_name", value);
                    insertMap.put("shop_id", shopId);
                    insertMap.put("course_id", courseId);
//					insertMap.put("course_title", value);
//					insertMap.put("course_url", value);
//					insertMap.put("course_type", value);
//					insertMap.put("lecturer_id", value);
//					insertMap.put("lecturer_name", value);
//					insertMap.put("create_time", value);
                    insertMap.put("sale_money", requestMapData.get("total_fee"));
                    insertMap.put("distributer_distribute_income", courseMap.get("distributer_distribute_income"));
//					insertMap.put("distributer_name", value);
                    insertMap.put("tutor_distribute_income", courseMap.get("tutor_distribute_income"));
                    insertMap.put("platform_distribute_income", courseMap.get("platform_distribute_income"));
                    insertMap.put("lecturer_distribute_income", courseMap.get("lecturer_distribute_income"));
                    userCommonModuleServer.insertCourseSale(insertMap);

                    // 更新分销者店铺统计表
                    insertMap.clear();
                    insertMap.put("shop_id", shopId);
                    insertMap.put("user_id", shopStatisticsMap.get("user_id"));
                    insertMap.put("update_time", now);
                    if (isNewCustomer) {
                        insertMap.put("customer_num_total", 1);
                    }
                    insertMap.put("sale_money_total", courseMap.get("course_price"));
                    insertMap.put("sale_num_total", 1);
                    Long profit = (Long) courseMap.get("distributer_distribute_income");
                    insertMap.put("sale_income_total", profit);
                    insertMap.put("customer_offer", profit);
                    insertMap.put("balance", profit);
                    insertMap.put("course_num", 1);

                    Timestamp updateTime = (Timestamp) shopStatisticsMap.get("update_time");
                    if (MiscUtils.isThisDay(updateTime.getTimestamp().getTime(), now.getTime())) {
                        // 上一次更新是当日
                        insertMap.put("month_sale", (Long) shopStatisticsMap.get("month_sale") + 1);
                        insertMap.put("day_sale", (Long) shopStatisticsMap.get("day_sale") + 1);
                        insertMap.put("day_income", (Long) shopStatisticsMap.get("day_income") + profit);
                        insertMap.put("week_income", (Long) shopStatisticsMap.get("week_income") + profit);
                        insertMap.put("month_income", (Long) shopStatisticsMap.get("month_income") + profit);
                    } else if (MiscUtils.isThisWeek(updateTime.getTimestamp().getTime(), now.getTime())) {
                        // 上一次更新非当日，但是本周
                        insertMap.put("month_sale", (Long) shopStatisticsMap.get("month_sale") + 1);
                        insertMap.put("day_sale", 1);
                        insertMap.put("week_income", (Long) shopStatisticsMap.get("week_income") + profit);
                        insertMap.put("month_income", (Long) shopStatisticsMap.get("month_income") + profit);
                    } else if (MiscUtils.isThisMonth(updateTime.getTimestamp().getTime(), now.getTime())) {
                        // 上一次更新非当日，非本周，但是本月
                        insertMap.put("month_sale", 1);
                        insertMap.put("day_sale", 1);
                        insertMap.put("day_income", profit);
                        insertMap.put("week_income", profit);
                        insertMap.put("month_income", (Long) shopStatisticsMap.get("month_income") + profit);
                    } else {
                        // 上一次更新非当日，非本周，但是本月
                        insertMap.put("month_sale", 1);
                        insertMap.put("day_sale", 1);
                        insertMap.put("day_income", profit);
                        insertMap.put("week_income", profit);
                        insertMap.put("month_income", profit);
                        insertMap.put("last_month_income", (Long) shopStatisticsMap.get("month_income"));
                    }
                    userCommonModuleServer.updateShopStatistics(insertMap);

                    // 更新导师店铺统计信息
                    insertMap.clear();
                    Map<String, Object> tutorMap = userCommonModuleServer.findDistributerByUserId(shopMap.get("user_id").toString());
                    if (!MiscUtils.isEmpty(tutorMap.get("tutor_id"))) {
                        Map<String, Object> tutorShopStatisticsMap = userCommonModuleServer.findShopStatisticsByUserId(tutorMap.get("tutor_id").toString());
                        if (!MiscUtils.isEmpty(tutorShopStatisticsMap)) {
                            insertMap.clear();
                            insertMap.put("shop_id", tutorShopStatisticsMap.get("shop_id"));
                            insertMap.put("user_id", tutorMap.get("tutor_id"));
                            insertMap.put("update_time", now);

                            profit = (Long) courseMap.get("tutor_distribute_income");
                            insertMap.put("sale_income_total", profit);
                            insertMap.put("student_offer", profit);
                            insertMap.put("balance", profit);
                            insertMap.put("course_num", 1);

                            updateTime = (Timestamp) tutorShopStatisticsMap.get("update_time");
                            if (MiscUtils.isThisDay(updateTime.getTimestamp().getTime(), now.getTime())) {
                                // 上一次更新是当日
                                insertMap.put("day_income", (Long) tutorShopStatisticsMap.get("day_income") + profit);
                                insertMap.put("week_income", (Long) tutorShopStatisticsMap.get("week_income") + profit);
                                insertMap.put("month_income", (Long) tutorShopStatisticsMap.get("month_income") + profit);
                            } else if (MiscUtils.isThisWeek(updateTime.getTimestamp().getTime(), now.getTime())) {
                                // 上一次更新非当日，但是本周
                                insertMap.put("week_income", (Long) tutorShopStatisticsMap.get("week_income") + profit);
                                insertMap.put("month_income", (Long) tutorShopStatisticsMap.get("month_income") + profit);
                            } else if (MiscUtils.isThisMonth(updateTime.getTimestamp().getTime(), now.getTime())) {
                                // 上一次更新非当日，非本周，但是本月
                                insertMap.put("day_income", profit);
                                insertMap.put("week_income", profit);
                                insertMap.put("month_income", (Long) tutorShopStatisticsMap.get("month_income") + profit);
                            } else {
                                // 上一次更新非当日，非本周，但是本月
                                insertMap.put("day_income", profit);
                                insertMap.put("week_income", profit);
                                insertMap.put("month_income", profit);
                                insertMap.put("last_month_income", (Long) tutorShopStatisticsMap.get("month_income"));
                            }
                            userCommonModuleServer.updateShopStatistics(insertMap);
                        }
                    }

                    // 生成分销者收益流水表
                    insertMap.put("profit_id", MiscUtils.getUUId());
                    insertMap.put("user_id", shopMap.get("usre_id"));
                    insertMap.put("trade_id", tradeId);
                    insertMap.put("profit_type", "0");
                    insertMap.put("money", courseMap.get("platform_distribute_income"));
                    insertMap.put("former_balance", shopStatisticsMap.get("balance"));
                    insertMap.put("later_balance", (Long) shopStatisticsMap.get("balance") + (Long) courseMap.get("platform_distribute_income"));
                    userCommonModuleServer.insertDistributerProfit(insertMap);

                    // 生成分销者收益通知
                    insertMap.clear();
                    insertMap.put("inform_id", MiscUtils.getUUId());
                    insertMap.put("inform_type", "1");
                    Long income = Long.parseLong(courseMap.get("distributer_distribute_income").toString());
                    insertMap.put("income", income);
                    insertMap.put("course_name", courseMap.get("course_name"));
                    insertMap.put("user_id", shopStatisticsMap.get("user_id"));
                    insertMap.put("balance", Long.parseLong(courseMap.get("balance").toString()) + income);
                    Map<String, Object> userMap = userCommonModuleServer.findUserInfoByUserId(userId);
                    userCommonModuleServer.insertPaymentBill(insertMap);
                } else {// 购买会员交易
                    // 生成分销者信息
                    Map<String, Object> insertMap = new HashMap<>();
                    insertMap.put("user_id", userId);
                    String inviteCodeD = billMap.get("invite_code").toString();
                    String tutorUserId = null;
                    Map<String, Object> tutorMap = null;
                    if (!MiscUtils.isEmpty(inviteCodeD)) {
                        tutorMap = userCommonModuleServer.findDistributerByInviteCode(inviteCodeD);
                        tutorUserId = (String) tutorMap.get("user_id");
                        insertMap.put("tutor_id", tutorUserId);
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.YEAR, 1);
                    insertMap.put("end_time", cal.getTime());

                    List<String> paramters = new ArrayList<>();
                    String inviteJoinRatio = "invite_join_ratio";
                    String inviteCodeStart = "invite_code_start";
                    String inviteJoinMoney = "invite_join_money";
                    paramters.add(inviteJoinRatio);
                    paramters.add(inviteCodeStart);
                    List<Map<String, Object>> resMap = userCommonModuleServer.findSystemConfig(paramters);
                    for (Map<String, Object> sysConfig : resMap) {
                        if (inviteJoinRatio.equals(sysConfig.get("config_key"))) {
                            inviteJoinRatio = sysConfig.get("config_value").toString();
                        } else if (inviteCodeStart.equals(sysConfig.get("config_key"))) {
                            inviteCodeStart = sysConfig.get("config_value").toString();
                        } else if (inviteJoinMoney.equals(sysConfig.get("config_key"))) {
                            inviteJoinMoney = sysConfig.get("config_value").toString();
                        }
                    }
                    long inviteCode = jedis.incrBy(Constants.CACHED_KEY_INVITE_CODE, 1);
                    if (inviteCode < Long.parseLong(inviteCodeStart)) {
                        inviteCode = Long.parseLong(inviteCodeStart);
                        jedis.set(Constants.CACHED_KEY_INVITE_CODE, inviteCodeStart);
                    }
                    insertMap.put("invite_code", inviteCode);
                    int ret = userCommonModuleServer.insertDistributer(insertMap);
                    if (ret < 1) {
                        throw new QNLiveException("000099");
                    }

                    // 生成分销者店铺信息
                    insertMap.clear();
                    String shopId = MiscUtils.getUUId();
                    insertMap.put("shop_id", shopId);
                    insertMap.put("user_id", userId);
                    userCommonModuleServer.insertShop(insertMap);

                    long profit = Long.parseLong(inviteJoinMoney) * Long.parseLong(inviteJoinRatio) / 100;
                    // 生成分销者店铺统计信息
                    insertMap.clear();
                    insertMap.put("shop_id", shopId);
                    insertMap.put("user_id", userId);
                    userCommonModuleServer.insertShopStatistics(insertMap);

                    // 如果有导师，则更新导师信息
                    if (!MiscUtils.isEmpty(tutorUserId)) {
                        Map<String, Object> tutorShopStatistics = userCommonModuleServer.findShopStatisticsByUserId(tutorMap.get("user_id").toString());
                        if (MiscUtils.isEmpty(tutorShopStatistics)) {
                            // 更新导师店铺统计信息
                            insertMap.clear();
                            insertMap.put("shop_id", tutorMap.get("shop_id"));
                            insertMap.put("user_id", tutorUserId);
                            insertMap.put("update_time", now);
                            insertMap.put("sale_income_total", profit);
                            insertMap.put("invite_join_offer", profit);
                            insertMap.put("balance", profit);

                            Timestamp updateTime = (Timestamp) tutorShopStatistics.get("update_time");
                            if (MiscUtils.isThisDay(updateTime.getTimestamp().getTime(), now.getTime())) {
                                // 上一次更新是当日
                                insertMap.put("day_income", (Long) tutorShopStatistics.get("day_income") + profit);
                                insertMap.put("week_income", (Long) tutorShopStatistics.get("week_income") + profit);
                                insertMap.put("month_income", (Long) tutorShopStatistics.get("month_income") + profit);
                            } else if (MiscUtils.isThisWeek(updateTime.getTimestamp().getTime(), now.getTime())) {
                                // 上一次更新非当日，但是本周
                                insertMap.put("day_income", profit);
                                insertMap.put("week_income", (Long) tutorShopStatistics.get("week_income") + profit);
                                insertMap.put("month_income", (Long) tutorShopStatistics.get("month_income") + profit);
                            } else if (MiscUtils.isThisMonth(updateTime.getTimestamp().getTime(), now.getTime())) {
                                // 上一次更新非当日，非本周，但是本月
                                insertMap.put("day_income", profit);
                                insertMap.put("week_income", profit);
                                insertMap.put("month_income", (Long) tutorShopStatistics.get("month_income") + profit);
                            } else {
                                // 上一次更新非当日，非本周，但是本月
                                insertMap.put("day_income", profit);
                                insertMap.put("week_income", profit);
                                insertMap.put("month_income", profit);
                                insertMap.put("last_month_income", (Long) tutorShopStatistics.get("month_income"));
                            }
                            userCommonModuleServer.updateShopStatistics(insertMap);

                            // 生成导师收益流水表
                            insertMap.clear();
                            insertMap.put("profit_id", MiscUtils.getUUId());
                            insertMap.put("user_id", tutorUserId);
                            insertMap.put("trade_id", tradeId);
                            insertMap.put("profit_type", "0");
                            insertMap.put("money", profit);
                            insertMap.put("former_balance", tutorShopStatistics.get("balance"));
                            insertMap.put("later_balance", (Long) tutorShopStatistics.get("balance") + profit);
                            userCommonModuleServer.insertDistributerProfit(insertMap);
                        }
                    }
                }
            } else {
                logger.debug("==> 微信支付失败 ,流水 ：" + tradeId);
                resultStr = TenPayConstant.FAIL;
            }

        } else {// MD5签名失败
            logger.debug("==> fail -Md5 failed");
            resultStr = TenPayConstant.FAIL;
        }
        return resultStr;
    }

    /**
     * 提交反馈信息
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("feedback")
    public Map<String, Object> feedback(RequestEntity reqEntity) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        @SuppressWarnings("unchecked")
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        reqMap.put("user_id", userId);
        int res = userCommonModuleServer.feedback(reqMap);
        return resultMap;
    }

    /**
     * 获取前端需要的系统配置
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("getSysConfiguration")
    public Map<String, Object> getSysConfiguration(RequestEntity reqEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        Map<String, Object> resultMap = new HashMap<>();

        reqMap.put("status", "1");
        reqMap.put("is_front", "1");

        List<Map<String, Object>> sysConfigurationList = userCommonModuleServer.findSysConfiguration(reqMap);
        resultMap.put("config_info_list", sysConfigurationList);

        return resultMap;
    }

    /**
     * 获取登录用户购买的指定类型课程id集合
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("getMyCourseIdList")
    public Map<String, Object> getMyCourseIdList(RequestEntity reqEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        Map<String, Object> resultMap = new HashMap<>();

        String user_id = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        reqMap.put("user_id", user_id);

		/*
		 * 获取店铺的课程id列表
		 */
        List<String> myCourseIdList = userCommonModuleServer.getMyCourseIdList(reqMap);

		resultMap.put("voice_id_list", myCourseIdList);
        return resultMap;
    }
    /**
     * @param type         1新注册用户处理方式，2老用户处理方式
     * @param dbResultMap  当type为1时，传入该值，该值为新用户注册后返回的信息；当type为2时该值传入null
     * @param loginInfoMap
     * @param resultMap    service要返回的map
     */
    private void processLoginSuccess(Integer type, Map<String, String> dbResultMap, Map<String, Object> loginInfoMap,
                                     Map<String, Object> resultMap) throws Exception {

        Jedis jedis = jedisUtils.getJedis();
        String last_login_date = (new Date()).getTime() + "";
        String user_id = null;
        Map<String, String> cacheMap = new HashMap<String, String>();

        if (type == 1) {
            //新注册用户,重新查询loginInfo
            user_id = dbResultMap.get("user_id");
            loginInfoMap = userCommonModuleServer.findLoginInfoByUserId(user_id);
        } else if (type == 2) {
            //老用户
            user_id = loginInfoMap.get("user_id").toString();
        }
        //1.将objectMap转为StringMap
        MiscUtils.converObjectMapToStringMap(loginInfoMap, cacheMap);
        //2.根据相关信息生成access_token
        String access_token = AccessTokenUtil.generateAccessToken(user_id, last_login_date);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("access_token", access_token);
        String process_access_token = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_ACCESS_TOKEN, map);
        if (!cacheMap.isEmpty()) {
            jedis.hmset(process_access_token, cacheMap);
        }
        jedis.expire(process_access_token, Integer.parseInt(MiscUtils.getConfigByKey("access_token_expired_time")));
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("user_id", user_id);
        Map<String, String> userMap = CacheUtils.readUser(user_id, this.generateRequestEntity(null, null, null, query), readUserOperation, jedisUtils);

        //3.增加相关返回参数
        resultMap.put("access_token", access_token);
        resultMap.put("user_id", user_id);

        resultMap.put("avatar_address", userMap.get("avatar_address"));
        //resultMap.put("nick_name", MiscUtils.RecoveryEmoji(userMap.get("nick_name")));
        resultMap.put("nick_name", userMap.get("nick_name"));
        resultMap.put("phone", userMap.get("phone_number"));
        //TODO 会员查询
        resultMap.put("is_vip", "1");
    }

    private String qiNiuFetchURL(String mediaUrl) throws Exception {
        Configuration cfg = new Configuration(Zone.zone0());
        BucketManager bucketManager = new BucketManager(auth, cfg);
        String bucket = MiscUtils.getConfigByKey("image_space");
        String key = Constants.WEB_FILE_PRE_FIX + MiscUtils.parseDateToFotmatString(new Date(), "yyyyMMddHH")
                + MiscUtils.getUUId();
        FetchRet result = bucketManager.fetch(mediaUrl, bucket, key);
        String imageUrl = MiscUtils.getConfigByKey("images_space_domain_name") + "/" + key;
        return imageUrl;
    }
    /**
     *  发送验证码
     * @param reqEntity
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @FunctionName("sendVerificationCode")
    public void sendVerificationCode (RequestEntity reqEntity) throws Exception{
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());//用安全证书拿userId
        Map<String,String> map = (Map<String, String>) reqEntity.getParam();
        String phoneNum = map.get("phone");//手机号
        String type = map.get("type");//手机号
        //1：type=1 电话号码去重
        if("1".equals(type)){
            Map<String,String> reqMap = (Map<String, String>) reqEntity.getParam();
            reqMap.put("user_id", userId);
            Map<String, String> values = CacheUtils.readUser(userId, reqEntity, readUserOperation, jedisUtils);
            if(values.get("phone_number")!=null){
             	throw new QNLiveException("120024");
             }
        	if(!CollectionUtils.isEmpty(userCommonModuleServer.findByPhone(phoneNum))){
                throw new QNLiveException("120023");
            }
        }
        //假发送
        /*
        if(MiscUtils.isMobile(phoneNum)){ //效验手机号码
            Jedis jedis = jedisUtils.getJedis();
            Map<String,String> userMap = new HashMap<>();
            userMap.put("user_id",userId);
            String userKey =  MiscUtils.getKeyOfCachedData(Constants.SEND_MSG_TIME_S, userMap);
            jedis.setex(userKey,5*60,phoneNum);//把手机号码和userid还有效验码 存入缓存当中  //一分钟内
            String dayKey =  MiscUtils.getKeyOfCachedData(Constants.SEND_MSG_TIME_D, userMap);//判断日期 一天三次
            if(jedis.exists(dayKey)){
                Map<String,String> redisMap = JSON.parseObject(jedis.get(dayKey), new TypeReference<Map<String, String>>(){});
                if(Integer.parseInt(redisMap.get("count"))==20){
                    throw new QNLiveException("000002");//发送太频繁
                }else{
                    int count = Integer.parseInt(redisMap.get("count")) + 1;

                    int expireTime = (int) (System.currentTimeMillis()/1000 - Long.parseLong(redisMap.get("timestamp"))) ;

                    jedis.setex(dayKey,
                            86410 - expireTime , "{'timestamp':'"+redisMap.get("timestamp")+"','count':'"+count+"'}");
                }
            }else{
                jedis.setex(dayKey,86400,"{'timestamp':'"+System.currentTimeMillis()/1000+"','count':'1'}");//把手机号码和userid还有效验码 存入缓存当中  //一分钟内
            }

            String code = MiscUtils.createRandom(true, 4);   //4位 生成随机的效验码
            String message = String.format("您的短信验证码:%s，请及时完成验证。",code);

            String codeKey =  MiscUtils.getKeyOfCachedData(Constants.CAPTCHA_KEY_CODE, userMap);//存入缓存中
            jedis.setex(codeKey,20*60,code);

            Map<String,String> phoneMap = new HashMap<>();
            phoneMap.put("code",code);
            String phoneKey =  MiscUtils.getKeyOfCachedData(Constants.CAPTCHA_KEY_PHONE, phoneMap);
            jedis.setex(phoneKey,20*60,phoneNum);

            String result = SendMsgUtil.sendMsgCode(phoneNum, message);
            logger.info("【梦网】（" + phoneNum + "）发送短信内容（" + message + "）返回结果：" + result);
            if(!"success".equalsIgnoreCase(SendMsgUtil.validateCode(result))){
                throw new QNLiveException("130006");
            }
        }else{
            throw new QNLiveException("130001");
        }*/
    }

    /**
     * 绑定手机号码
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("bindPhone")
    public Map<String, Object> bindPhone(RequestEntity reqEntity) throws Exception {
        Map<String,String> reqMap = (Map<String, String>) reqEntity.getParam();
        String verification_code = reqMap.get("verification_code");
        String phone = reqMap.get("phone");
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        //if(ServerUtils.verifyVerificationCode(userId, verification_code, jedisUtils)){
        if("0000".equals(verification_code)){
        	Map<String, Object> userMap = new HashMap<>();
        	userMap.put("user_id", userId);
        	userMap.put("phone_number", phone);
        	userCommonModuleServer.updateUserPhone(userMap);
        	//清空用户缓存
        	Map<String,Object> query = new HashMap<String,Object>();
			query.put(Constants.CACHED_KEY_USER_FIELD, userId);
			String key = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_USER, query);
			Jedis jedis = jedisUtils.getJedis();
			jedis.del(key);
        }else{
        	throw new QNLiveException("120022");
        }
        return null;
    }
}
