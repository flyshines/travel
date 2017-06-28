package qingning.user.common.server.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import qingning.common.entity.RequestEntity;
import qingning.common.entity.ResponseEntity;
import qingning.common.util.MiscUtils;
import qingning.server.AbstractController;
import qingning.user.common.server.util.ServerUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value="/user/common")
public class UserCommonController extends AbstractController {

    private static final Logger logger   = LoggerFactory.getLogger(UserCommonController.class);

    /**
     * 获取系统时间
     *
     * @return
     * @throws Exception
     */
	@RequestMapping(value = "/client/information", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity collectClientInformation(HttpEntity<Object> entity,
    		@RequestHeader(value="access_token", defaultValue="") String accessToken) throws Exception {
        RequestEntity requestEntity = this.createRequestEntity("UserCommonServer", "logUserInfo", accessToken, null);
		@SuppressWarnings("unchecked")
		Map<String, String> inputParameters = (Map<String, String>)entity.getBody();
        inputParameters.put("ip", ServerUtils.getRequestIP());
        if(inputParameters.containsKey("login_id")){
        	inputParameters.put("union_id", inputParameters.get("login_id"));
        }
        requestEntity.setParam(entity.getBody());
        ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
        return responseEntity;
    }
	
    /**
     * 获得上传到七牛token
     * @param accessToken
     * @param version
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/upload/token", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity getQiNiuUploadToken(
            @RequestParam(value = "upload_type", defaultValue = "") String upload_type,
            @RequestHeader("access_token") String accessToken,
            @RequestHeader("version") String version
    ) throws Exception {
        RequestEntity requestEntity = this.createRequestEntity("UserCommonServer", "qiNiuUploadToken", accessToken, version);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("upload_type", upload_type);
        requestEntity.setParam(param);
        return this.process(requestEntity, serviceManger, message);
    }
	
    /**
     * 用户登录
     *
     * @param entity
     * @param version
     * @return
     * @throws Exception
     */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity userLogin(
    		HttpServletRequest httpRequest,
            HttpEntity<Object> entity,
            @RequestHeader("version") String version) throws Exception {
    	String ip = MiscUtils.getIpAddr(httpRequest);
        RequestEntity requestEntity = this.createRequestEntity("UserCommonServer", "userLogin", null, version);
        requestEntity.setIp(ip);
        requestEntity.setParam(entity.getBody());
        ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);

        //根据相关条件将server_url列表信息返回
        Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
//        Map<String, Object> bodyMap = (Map<String, Object>) entity.getBody();
//        if (bodyMap.get("server_url_update_time") == null ||
//                !bodyMap.get("server_url_update_time").toString().equals(serverUrlInfoUpdateTime.toString())) {
//            resultMap.put("server_url_info_list", serverUrlInfoMap);
//            resultMap.put("server_url_info_update_time", serverUrlInfoUpdateTime);
//        }
        responseEntity.setReturnData(resultMap);
        return responseEntity;
    }
    
    /**
     * 查询用户信息
     * @param query_type 1:个人中心信息 2：个人基本信息
     * @param server_url_info_update_time 服务与对应url信息更新时间
     * @param accessToken 后台安全证书
     * @param version 版本号
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/userinfo/get", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity getUserInfo(
            @RequestParam(value = "query_type", defaultValue = "1") String query_type,
            @RequestParam(value = "server_url_info_update_time", defaultValue = "") String server_url_info_update_time,
            @RequestHeader("access_token") String accessToken,
            @RequestHeader("version") String version
    ) throws Exception {
        RequestEntity requestEntity = this.createRequestEntity("UserCommonServer", "userInfo", accessToken, version);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("query_type", query_type);
        requestEntity.setParam(param);
        ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
        Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
        //根据相关条件将server_url列表信息返回
//        if("2".equals(query_type)){
//	        if (MiscUtils.isEmpty(server_url_info_update_time) ||
//	                !server_url_info_update_time.equals(serverUrlInfoUpdateTime.toString())) {
//	            resultMap.put("server_url_info_list", serverUrlInfoMap);
//	            resultMap.put("server_url_info_update_time", serverUrlInfoUpdateTime);
//	        }
//        }
        responseEntity.setReturnData(resultMap);
        return responseEntity;
    }
    
    /**
     * 查询用户信息
     * @param accessToken 后台安全证书
     * @param version 版本号
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/userinfo/update", method = RequestMethod.PUT)
    public @ResponseBody ResponseEntity getUserInfo(
    		HttpEntity<Object> entity,
    		@RequestHeader("access_token") String accessToken,
    		@RequestHeader("version") String version
    		) throws Exception {
    	RequestEntity requestEntity = this.createRequestEntity("UserCommonServer", "updateUserInfo", accessToken, version);
    	requestEntity.setParam(entity.getBody());
    	
    	//根据相关条件将server_url列表信息返回
    	ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
    	return responseEntity;
    }
    
    /**
     * 获取我购买的课程列表
     * @param page_count
     * @param position
     * @return
     * @throws Exception
     */
	@RequestMapping(value = "/myCourseList/get", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity getMyCourseList(
            @RequestParam(value = "page_count", defaultValue = "20") String page_count,
            @RequestParam(value = "position", defaultValue = "") String position,
            @RequestHeader("access_token") String accessToken,
            @RequestHeader("version") String version
    ) throws Exception {
        RequestEntity requestEntity = this.createRequestEntity("UserCommonServer", "getMyCourseList", accessToken, version);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("page_count", page_count);
        param.put("position", position);
        requestEntity.setParam(param);
        ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
        return responseEntity;
    }
    
    /**
     * 获取我的消费记录
     * @param page_count
     * @param position
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/myCostList", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity getMyCostList(
    		@RequestParam(value = "page_count", defaultValue = "20") String page_count,
    		@RequestParam(value = "position", defaultValue = "") String position,
    		@RequestHeader("access_token") String accessToken,
    		@RequestHeader("version") String version
    		) throws Exception {
    	RequestEntity requestEntity = this.createRequestEntity("UserCommonServer", "getMyCostList", accessToken, version);
    	Map<String, Object> param = new HashMap<String, Object>();
    	param.put("page_count", page_count);
    	param.put("position", position);
    	requestEntity.setParam(param);
    	ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
    	return responseEntity;
    }

    /**
     * 提交意见反馈
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/feedback", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity feedback(
    		HttpEntity<Object> entity,
    		@RequestHeader("access_token") String accessToken,
    		@RequestHeader("version") String version
    		) throws Exception {
    	RequestEntity requestEntity = this.createRequestEntity("UserCommonServer", "feedback", accessToken, version);
    	requestEntity.setParam(entity.getBody());
    	ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
    	return responseEntity;
    }
    
    /**
     * 获取系统配置信息
     * @param config_key
     * @param accessToken
     * @param version
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/sys/configuration", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity sysConfiguration(
    		@RequestParam(value = "config_key", defaultValue = "") String config_key,
    		@RequestHeader("access_token") String accessToken,
    		@RequestHeader("version") String version
    		) throws Exception {
    	RequestEntity requestEntity = this.createRequestEntity("UserCommonServer", "getSysConfiguration", accessToken, version);
    	Map<String, Object> param = new HashMap<String, Object>();
    	param.put("config_key", config_key);
    	requestEntity.setParam(param);
    	ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
    	return responseEntity;
    }
    
    /**
     * 解码并重定向到分享地址
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/sys/decodeUrl", method = RequestMethod.GET)
    public void decodeUrl(
    		HttpServletRequest request, HttpServletResponse response) throws Exception {
    	//获取请求参数
    	String url = request.getParameter("url");
    	
    	//重定向
    	response.sendRedirect(url);
    }
    
    /**
     * 获取登录用户购买的指定类型课程id集合
     * @param course_type
     * @param accessToken
     * @param version
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/myCourseIdList", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity getMyCourseIdList(
    		@RequestParam(value = "course_type", defaultValue = "0") String course_type,
    		@RequestHeader("access_token") String accessToken,
    		@RequestHeader("version") String version
    		) throws Exception {
    	RequestEntity requestEntity = this.createRequestEntity("UserCommonServer", "ds", accessToken, version);
    	Map<String, Object> param = new HashMap<String, Object>();
    	param.put("course_type", course_type);
    	requestEntity.setParam(param);
    	ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
    	return responseEntity;
    }

    /**
     * 发送手机验证码
     *
     * @param phone       电话号码
     * @param accessToken 用户安全证书
     * @param version     版本
     * @throws Exception
     */
    @RequestMapping(value = "/sendVerificationCode", method = RequestMethod.GET)
    public
    @ResponseBody
    ResponseEntity sendVerificationCode(HttpServletRequest request,
    									 @RequestParam(value = "type" ,defaultValue = "1") String type,
    							         @RequestParam(value = "phone", defaultValue = "") String phone,
                                        @RequestHeader("access_token") String accessToken,
                                        @RequestHeader("version") String version) throws Exception {
        RequestEntity requestEntity = this.createRequestEntity("UserCommonServer", "sendVerificationCode", accessToken, null);
        Map<String,String> map = new HashMap<>();
        map.put("type",type);
        map.put("phone",phone);
        requestEntity.setParam(map);
        return this.process(requestEntity, serviceManger, message);
    }
    /**
     * 绑定手机号码（校验验证码）
     * @param verification_code 验证码
     * @param accessToken 用户安全证书
     * @param version 版本
     * @throws Exception
     */
    @RequestMapping(value = "/bindPhone", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity  verifyVerificationCode(
			HttpEntity<Object> entity,
            @RequestHeader("access_token") String accessToken,
            @RequestHeader("version") String version)throws Exception {
        RequestEntity requestEntity = this.createRequestEntity("UserCommonServer", "bindPhone", accessToken, null);
        requestEntity.setParam(entity.getBody());
        return this.process(requestEntity, serviceManger, message);
    }
}
