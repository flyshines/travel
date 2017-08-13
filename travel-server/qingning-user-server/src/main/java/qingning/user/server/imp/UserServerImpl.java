package qingning.user.server.imp;

import com.alibaba.fastjson.JSONObject;
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
import qingning.common.entity.QNLiveException;
import qingning.common.entity.RequestEntity;
import qingning.common.util.*;
import qingning.server.AbstractQNLiveServer;
import qingning.server.annotation.FunctionName;
import qingning.server.rpc.manager.IUserModuleServer;
import qingning.user.server.other.*;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.*;

public class UserServerImpl extends AbstractQNLiveServer {

    private IUserModuleServer userModuleServer;

    private ReadUserOperation readUserOperation;
    private static Logger logger = LoggerFactory.getLogger(UserServerImpl.class);

    @Override
    public void initRpcServer() {
        if (userModuleServer == null) {
            userModuleServer = this.getRpcService("userModuleServer");

            readUserOperation = new ReadUserOperation(userModuleServer);
        }
    }
    private static Auth auth;
    static {
        auth = Auth.create(MiscUtils.getConfigByKey("qiniu_AK"), MiscUtils.getConfigByKey("qiniu_SK"));
    }
    /**
     * 购买主页
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("buyIndex")
    public Map<String, Object> buyIndex(RequestEntity reqEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        Map<String,Object> info = userModuleServer.findTicketInfo();

        List<Map<String,Object>> bannerList = userModuleServer.findBannerList();
        info.put("banner_list",bannerList);
        return info;
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
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        // 个人基本信息
        Map<String, Object> param = new HashMap<>();
        param.put("user_id",userId);
        reqEntity.setParam(param);
        Map<String, String> values = CacheUtils.readUser(userId, reqEntity, readUserOperation, jedisUtils);
        //VIP标识
        String sign = values.get("sign");
        resultMap.put("avatar_address", values.get("avatar_address"));
        resultMap.put("nick_name", MiscUtils.RecoveryEmoji(values.get("nick_name")));
        resultMap.put("is_vip", sign!=null?1:0);
        resultMap.put("invalid_time", new Date());
        resultMap.put("user_id", userId);
        if(sign!=null){
            resultMap.put("rq_code",AESOperator.getInstance().encrypt(System.currentTimeMillis()+sign));
        }

        return resultMap;
    }


    /**
     * 商户列表
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("shopList")
    public Map<String, Object> shopList(RequestEntity reqEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        reqMap.put("status","1");
        Map<String, Object> resMap = userModuleServer.getShopList(reqMap);
        return resMap;

    }
    /**
     * 商户列表-后台
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("placeList")
    public Map<String, Object> placeList(RequestEntity reqEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        Map<String, Object> resMap = userModuleServer.getShopList(reqMap);
        return resMap;

    }

    /**
     * 商户扫码
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("scanCode")
    public Map<String, Object> scanCode(RequestEntity reqEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        Map<String,Object> result = null;
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        String code = reqMap.get("code").toString();
        String signAll = AESOperator.getInstance().decrypt(code);
        Map<String,Object> param = new HashMap<>();
        param.put("check_user_id",userId);
        param.put("shop_id",reqMap.get("place_id"));
        String nickName = null;
        if(signAll!=null){
            long time = Long.valueOf(signAll.substring(0,13));
            long now = System.currentTimeMillis();
            if(now-time>300){
                //sign过期
                throw new QNLiveException("210008");
            }else{
                String sign = signAll.substring(13,signAll.length());
                Map<String,Object> vipInfo = userModuleServer.getVipUserInfo(sign);
                if(vipInfo == null){
                    //没有查到会员信息
                    throw new QNLiveException("210009");
                }
                Date closeTime = (Date)vipInfo.get("close_time");
                if(now>closeTime.getTime()){
                    //会员过期
                    throw new QNLiveException("210010");
                }
                //今日入园次数
                int count = userModuleServer.getUserVisitCount(vipInfo.get("user_id").toString(),reqMap.get("place_id").toString());

                if(count>0){
                    throw new QNLiveException("210011");
                }

                param.put("sign",sign);
                param.put("user_id",vipInfo.get("user_id"));
                param.put("create_time",new Date());
                //插入访问记录
                userModuleServer.addUserVisit(param);
                result = new HashMap<>();
                nickName = vipInfo.get("nick_name")+"";
                result.put("nick_name",nickName);
            }
        }else{
            throw new QNLiveException("210007");
        }
        return result;
    }
    /**
     * 后台登录
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("sysLogin")
    public Map<String, Object> sysLogin(RequestEntity reqEntity) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        /*
         * 获取请求参数
         */
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        Jedis jedis = jedisUtils.getJedis();

        String password = ((String) reqMap.get("password")).toUpperCase();

        /*
         * 根据号码查询数据库
         */
        Map<String, Object> adminUserMap = userModuleServer.getAdminUserByMobile(reqMap);
        if (adminUserMap == null) {
            logger.info("后台登录>>>>手机号没有关联账户");
            throw new QNLiveException("000005");
        }
       // 44310B3143C5C0411AB3550B5A72CB38
        //260EFC08DD372845291C4B8F96BFD26D
        /*
         * 验证密码:
         * 	前端传递的MD5加密字符串后追加appName，在进行MD5加密
         */
        String md5Pw = MD5Util.getMD5(password + "_" + "@travel");
        if (!md5Pw.equals(adminUserMap.get("password").toString())) {
            logger.info("后台登录>>>>登录密码错误");
            throw new QNLiveException("120001");
        }

        /*
         * 更新后台登录用户统计数据
         */
        Date now = new Date();
        adminUserMap.put("last_login_time", now);
        adminUserMap.put("last_login_ip", "");
        /*
         * 判断是否需要生成token
         */
        String userId = String.valueOf(adminUserMap.get("user_id"));
        String accessToken = (String) adminUserMap.get("token");
        Map<String, Object> map = new HashMap<String, Object>();
        String accessTokenKey = null;
        if (!StringUtils.isBlank(accessToken)) {    //数据库中的token不为空
            map.put("access_token", accessToken);
            accessTokenKey = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_ACCESS_TOKEN,
                    map);
            if (!jedis.exists(accessTokenKey)) {    //不在redis中
            	/*
            	 * 生成新的accessToken
            	 */
                accessToken = AccessTokenUtil.generateAccessToken(userId, String.valueOf(now.getTime()));
                map.put("access_token", accessToken);
                accessTokenKey = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_ACCESS_TOKEN,
                        map);
            }
        } else {
        	/*
        	 * 生成新的accessToken
        	 */
            accessToken = AccessTokenUtil.generateAccessToken(userId, String.valueOf(now.getTime()));
            map.put("access_token", accessToken);
            accessTokenKey = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_ACCESS_TOKEN,
                    map);
        }

        adminUserMap.put("token", accessToken);
        userModuleServer.updateAdminUserByAllMap(adminUserMap);

        /*
         * 将token写入缓存
         */
        Map<String, String> tokenCacheMap = new HashMap<>();
        MiscUtils.converObjectMapToStringMap(adminUserMap, tokenCacheMap);

        if (!tokenCacheMap.isEmpty()) {
            jedis.hmset(accessTokenKey, tokenCacheMap);
        }
        jedis.expire(accessTokenKey, Integer.parseInt(MiscUtils.getConfigByKey("access_token_expired_time")));

        /*
         * 返回数据
         */
        adminUserMap.put("access_token", accessToken);
        adminUserMap.put("version", "1.0.0");    //暂时写死
        resultMap.putAll(adminUserMap);
        return resultMap;
    }
    /**
     * 后台-新增景区
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("addPlace")
    public Map<String, Object> addPlace(RequestEntity reqEntity) throws Exception {
        Map<String, Object> param = (Map<String, Object>) reqEntity.getParam();
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        param.put("create_user",userId);
        param.put("create_time",new Date());
        param.put("shop_id",MiscUtils.getUUId());
        param.put("ticket_id","1");
        param.put("shop_remark",param.get("remark"));
        param.put("shop_name",param.get("place_name"));
        if(param.get("place_image")==null){
            param.put("shop_image","http://ou8rry0e1.bkt.clouddn.com/banner.2446c16.png");
        }else{
            param.put("shop_image",param.get("place_image"));
        }
        userModuleServer.addPlace(param);

        return null;
    }
    /**
     * 后台-编辑景区
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("editPlace")
    public Map<String, Object> editPlace(RequestEntity reqEntity) throws Exception {
        Map<String, Object> param = (Map<String, Object>) reqEntity.getParam();
        param.put("update_time",new Date());
        param.put("shop_id",param.get("place_id"));

        if(param.get("remark")==null&&param.get("place_name")==null&&param.get("place_image")==null&&param.get("status")==null){
            return null;
        }

        if(param.get("remark")!=null){
            param.put("shop_remark",param.get("remark"));
        }
        if(param.get("place_name")!=null){
            param.put("shop_name",param.get("place_name"));
        }
        if(param.get("place_image")!=null){
            param.put("shop_image",param.get("place_image"));
        }
        if(param.get("status")!=null){
            param.put("status",param.get("status"));
        }

        userModuleServer.updatePlace(param);

        return null;
    }
   /**
     * 编辑票价
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("editTicket")
    public Map<String, Object> editTicket(RequestEntity reqEntity) throws Exception {
        Map<String, Object> param = (Map<String, Object>) reqEntity.getParam();
        //double price = Double.valueOf(param.get("price").toString());
        //long newPrice = new BigDecimal(DoubleUtil.mul(price,100D)).longValue();
        //param.put("price",newPrice);
        userModuleServer.updateTicket(param);
        return null;
    }

   /**
     * 收入列表
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("incomeList")
    public Map<String, Object> incomeList(RequestEntity reqEntity) throws Exception {
        Map<String, Object> param = (Map<String, Object>) reqEntity.getParam();
        if(param.get("start_time")!=null){
            long start = Long.valueOf(param.get("start_time").toString());
            param.put("start_time",new Date(start));
        }
        if(param.get("end_time")!=null){
            long end = Long.valueOf(param.get("end_time").toString());
            param.put("end_time",new Date(end));
        }
        Map<String, Object> res = userModuleServer.getIncomeList(param);
        return res;
    }
   /**
     * 用户列表
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("userList")
    public Map<String, Object> userList(RequestEntity reqEntity) throws Exception {
        Map<String, Object> param = (Map<String, Object>) reqEntity.getParam();
        Map<String, Object> res = userModuleServer.getUserList(param);
        return res;
    }


















    /*=====================================其他代码=========================================*/



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
                int count = userModuleServer.updateUserCommonInfo(parameters);
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
        return new HashMap<>();
    }

    /**
     * 获取微信配置信息
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("weiXinConfiguration")
    public Map<String, String> getWeiXinConfiguration(RequestEntity reqEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        String JSApiTIcket = WeiXinUtil.getJSApiTIcket(jedisUtils.getJedis());
        return WeiXinUtil.sign(JSApiTIcket, reqMap.get("url").toString());
    }
    /**
     * 获取微信商户配置信息
     *
     * @param reqEntity
     * @return
     * @throws Exception
     */
    @FunctionName("getWeiXinShopConfiguration")
    public Map<String, String> getWeiXinShopConfiguration(RequestEntity reqEntity) throws Exception {
        Map<String, Object> reqMap = (Map<String, Object>) reqEntity.getParam();
        String JSApiTIcket = WeiXinUtil.getJSSHOPTIcket(jedisUtils.getJedis());
        return WeiXinUtil.signForShop(JSApiTIcket, reqMap.get("url").toString());
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

        List<Map<String, Object>> resList = userModuleServer.findMyCostList(reqMap);
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
        boolean isShop = "1".equals(reqMap.get("is_shop")+"")?true:false;
        if(isShop) {
            resultMap.put("key", "2");// 钥匙 跳转的页面
        }else{
            resultMap.put("key", "1");// 钥匙 跳转的页面
        }
        String code = reqMap.get("code").toString();
        // 1.传递授权code及相关参数，调用微信验证code接口
        JSONObject getCodeResultJson = WeiXinUtil.getUserInfoByCode(code,isShop);
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
        Map<String, Object> loginInfoMap = userModuleServer.getLoginInfoByLoginIdAndLoginType(queryMap);

        // 1.2.1.1如果用户存在则进行登录流程
        if (loginInfoMap != null) {
            processLoginSuccess(2, null, loginInfoMap, resultMap);
            return resultMap;
        } else {
            // 1.2.1.2如果用户不存在，则根据用户的open_id和用户的access_token调用微信查询用户信息接口，得到用户的头像、昵称等相关信息
            String userWeixinAccessToken = getCodeResultJson.getString("access_token");
            JSONObject userJson = WeiXinUtil.getUserInfoByAccessToken(userWeixinAccessToken, openid);
            // 根据得到的相关用户信息注册用户，并且进行登录流程。
            if (userJson == null || userJson.getInteger("errcode") != null /*|| userJson.getString("unionid") == null*/) {
                if (userJson.getString("unionid") == null) {
                    resultMap.put("key", "0");
                    return resultMap;
                }
                throw new QNLiveException("120008");
            }
            //公众平台unionid校验
            /*queryMap.clear();
            queryMap.put("login_type", "0");// 0.微信方式登录
            queryMap.put("login_id", userJson.getString("unionid"));
            Map<String, Object> loginInfoMapFromUnionid = userModuleServer.getLoginInfoByLoginIdAndLoginType(queryMap);
            if (loginInfoMapFromUnionid != null) {
                // 将open_id更新到login_info表中
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("user_id", loginInfoMapFromUnionid.get("user_id").toString());
                updateMap.put("web_openid", openid);
                userModuleServer.updateUserWebOpenIdByUserId(updateMap);
                processLoginSuccess(2, null, loginInfoMapFromUnionid, resultMap);
                return resultMap;
            }*/

            String nickname = userJson.getString("nickname");
            String sex = userJson.getString("sex");
            String headimgurl = userJson.getString("headimgurl");

            // 设置默认用户头像
            if (MiscUtils.isEmpty(headimgurl)) {
                reqMap.put("avatar_address", MiscUtils.getConfigByKey("default_avatar_address"));// TODO
            } else {
                //String transferAvatarAddress = qiNiuFetchURL(headimgurl);
                reqMap.put("avatar_address", headimgurl);
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
            if(userJson.get("country")!=null)
                reqMap.put("country", userJson.get("country"));
            if(userJson.get("province")!=null)
                reqMap.put("province", userJson.get("province"));
            if(userJson.get("city")!=null)
                reqMap.put("city", userJson.get("city"));
            Map<String, String> dbResultMap = userModuleServer.initializeRegisterUser(reqMap,isShop);

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
        Jedis jedis = jedisUtils.getJedis();
        Map<String, Object> insertMap = new HashMap<>();
        String tradeId = MiscUtils.getWeiXinId();//TODO
        insertMap.put("trade_id", tradeId);
        String userId = AccessTokenUtil.getUserIdFromAccessToken(reqEntity.getAccessToken());
        insertMap.put("user_id", userId);

        String billType = reqMap.get("bill_type").toString();

        Map<String,Object> ticket = userModuleServer.getTicketPrice();

        String payGoodName = ticket.get("ticket_name").toString();

        Integer totalFee = Integer.parseInt(String.valueOf(ticket.get("ticket_price")));


        //3.插入t_trade_bill表 交易信息表
        insertMap.put("profit_type",billType);
        insertMap.put("status", 0);
        insertMap.put("amount", totalFee);
        insertMap.put("payment", totalFee);
        insertMap.put("create_time", new Date());
        insertMap.put("remark", payGoodName);

        userModuleServer.insertTradeBill(insertMap);
        Map<String, Object> query = new HashMap<>();
        query.put(Constants.CACHED_KEY_ACCESS_TOKEN_FIELD, reqEntity.getAccessToken());
        String key = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_ACCESS_TOKEN, query);
        Map<String, String> userMap = jedis.hgetAll(key);

        //4.调用微信生成预付单接口
        String terminalIp = reqMap.get("remote_ip_address").toString();
        String outTradeNo = tradeId;
        String openid  = userMap.get("web_openid");

        String attach = "{profit_type:" + billType + "}";

        Map<String, String> payResultMap = TenPayUtils.sendPrePay(payGoodName, totalFee, terminalIp, outTradeNo, openid, null,attach);

        //5.处理生成微信预付单接口
        if (payResultMap.get("return_code").equals("FAIL")) {
            //更新交易表
            Map<String, Object> failUpdateMap = new HashMap<>();
            failUpdateMap.put("status", "3");
            failUpdateMap.put("close_reason", "生成微信预付单失败 " + payResultMap.get("return_msg") + payResultMap.get("err_code_des"));
            failUpdateMap.put("trade_id", tradeId);
            userModuleServer.closeTradeBill(failUpdateMap);
            throw new QNLiveException("120015",payResultMap.get("err_code_des")+":"+payResultMap.get("return_msg"));
        } else {
            //成功，则需要插入支付表
            Map<String, Object> insertPayMap = new HashMap<>();
            insertPayMap.put("payment_id", MiscUtils.getUUId());
            insertPayMap.put("trade_id", tradeId);
            insertPayMap.put("pre_pay_no", payResultMap.get("prepay_id"));
            insertPayMap.put("create_time", new Date());
            insertPayMap.put("payment", totalFee);
            userModuleServer.insertPaymentBill(insertPayMap);

            //返回相关参数给前端.
            SortedMap<String, String> resultMap = new TreeMap<>();
            resultMap.put("appId", MiscUtils.getConfigByKey("appid"));
            resultMap.put("package", "prepay_id=" + payResultMap.get("prepay_id"));
            resultMap.put("nonceStr", payResultMap.get("random_char"));
            resultMap.put("signType", "MD5");
            resultMap.put("timeStamp", System.currentTimeMillis() / 1000 + "");

            String paySign = TenPayUtils.getSign(resultMap,null);
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
        //获得支付类型
        JSONObject attachJsonObj = JSONObject.parseObject(requestMapData.get("attach"));
        String profitType = attachJsonObj.getString("profit_type");

        // 检查该订单是否已处理
        Map<String, Object> billMap = userModuleServer.findTradebillByTradeId(tradeId);
        if (billMap == null) {
            logger.debug("====> 系统找不到该流水号：: " + tradeId);
            return TenPayConstant.FAIL;
        }else if (billMap.get("status").equals("2")) {
			logger.debug("====>　已经处理完成, 不需要继续。流水号是: " + tradeId);
			return TenPayConstant.SUCCESS;
		}

        if (TenPayUtils.isValidSign(requestMapData)) {// MD5签名成功
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
                // 更新交易信息状态
                userModuleServer.updateTradeBill(updateMap);

                String userId = billMap.get("user_id").toString();

                //购买年卡门票
                if ("0".equals(profitType)) {
                    // 购买课程交易
                    Map<String,Object> vipInfo = new HashMap<>();
                    vipInfo.put("user_id",userId);
                    vipInfo.put("create_time",now);
                    vipInfo.put("close_time",MiscUtils.getYearLater(now));
                    String uuid = MiscUtils.getOrderId();
                    vipInfo.put("sign",uuid);
                    userModuleServer.insertVipUser(vipInfo);

                    Map<String,Object> query = new HashMap<>();
                    query.put(Constants.CACHED_KEY_USER_FIELD, userId);
                    String key = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_USER, query);
                    jedis.hset(key,"sign",uuid);

                    // 生成分销者收益通知
                    /*insertMap.clear();
                    insertMap.put("inform_id", MiscUtils.getUUId());
                    insertMap.put("inform_type", "1");
                    Long income = Long.parseLong(courseMap.get("distributer_distribute_income").toString());
                    insertMap.put("income", income);
                    insertMap.put("course_name", courseMap.get("course_name"));
                    insertMap.put("user_id", shopStatisticsMap.get("user_id"));
                    insertMap.put("balance", Long.parseLong(courseMap.get("balance").toString()) + income);
                    userModuleServer.insertPaymentBill(insertMap);*/
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
            loginInfoMap = userModuleServer.findLoginInfoByUserId(user_id);
        } else if (type == 2) {
            //老用户
            user_id = loginInfoMap.get("user_id").toString();
        }
        //1.将objectMap转为StringMap
        MiscUtils.converObjectMapToStringMap(loginInfoMap, cacheMap);
        //2.根据相关信息生成access_token

        /*String userID_AccessToken = Constants.CACHED_KEY_ACCESS_TOKEN_USER+user_id;
        //已经有Token
        String oldToken = jedis.get(userID_AccessToken);
        if(oldToken!=null){
            Map<String, Object> map = new HashMap<>();
            map.put("access_token", oldToken);
            String old_access_token = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_ACCESS_TOKEN, map);
            jedis.del(old_access_token);
        }*/
        String access_token = AccessTokenUtil.generateAccessToken(user_id, last_login_date);
        Map<String, Object> map = new HashMap<>();
        map.put("access_token", access_token);
        String process_access_token = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_ACCESS_TOKEN, map);
        if (!cacheMap.isEmpty()) {
            jedis.hmset(process_access_token, cacheMap);
            //jedis.set(userID_AccessToken,access_token);
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
            if(!CollectionUtils.isEmpty(userModuleServer.findByPhone(phoneNum))){
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
            userModuleServer.updateUserPhone(userMap);
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