package qingning.user.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import qingning.common.entity.RequestEntity;
import qingning.common.entity.ResponseEntity;
import qingning.common.util.Constants;
import qingning.common.util.MiscUtils;
import qingning.common.util.ServerUtils;
import qingning.server.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController extends AbstractController{
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	/**
	 * 购买主页
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/buy/index", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity buyIndex(
			@RequestHeader(value = "access_token", defaultValue = "") String accessToken) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "buyIndex", accessToken, null);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		return responseEntity;
	}

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
											@RequestHeader(value = "access_token", defaultValue = "") String accessToken) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "logUserInfo", accessToken, null);
		@SuppressWarnings("unchecked")
		Map<String, String> inputParameters = (Map<String, String>) entity.getBody();
		inputParameters.put("ip", ServerUtils.getRequestIP());
		if (inputParameters.containsKey("login_id")) {
			inputParameters.put("union_id", inputParameters.get("login_id"));
		}
		requestEntity.setParam(entity.getBody());
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		return responseEntity;
	}













































	/**
	 * 获得上传到七牛token
	 *
	 * @param accessToken
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/upload/token", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity getQiNiuUploadToken(
			@RequestParam(value = "upload_type", defaultValue = "") String upload_type,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "qiNiuUploadToken", accessToken, version);
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
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "userLogin", null, version);
		requestEntity.setIp(ip);
		requestEntity.setParam(entity.getBody());
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);

		//根据相关条件将server_url列表信息返回
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}

	/**
	 * 查询用户信息
	 *
	 * @param accessToken 后台安全证书
	 * @param version     版本号
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/user/info", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity getUserInfo(
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "userInfo", accessToken, version);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}

	/**
	 * 查询用户信息
	 *
	 * @param accessToken 后台安全证书
	 * @param version     版本号
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/user/info", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity getUserInfo(
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "userInfo", accessToken, version);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}


	/**
	 * 查询景区列表
	 *
	 * @param accessToken 后台安全证书
	 * @param version     版本号
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/userinfo/update", method = RequestMethod.PUT)
	@RequestMapping(value = "/shop/list", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity getUserInfo(
			HttpEntity<Object> entity,
	ResponseEntity shopList(
			@RequestParam(value = "page_size", defaultValue = "10") long pageSize,
			@RequestParam(value = "page_num", defaultValue = "1") long pageNum,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "updateUserInfo", accessToken, version);
		requestEntity.setParam(entity.getBody());

		//根据相关条件将server_url列表信息返回
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "shopList", accessToken, version);
		Map<String, Object> paramCode = new HashMap<>();
		paramCode.put("page_size", pageSize);
		paramCode.put("page_num", pageNum);
		requestEntity.setParam(paramCode);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}



	/**
	 * 获取我的消费记录
	 * 获得上传到七牛token
	 *
	 * @param page_count
	 * @param position
	 * @param accessToken
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/myCostList", method = RequestMethod.GET)
	@RequestMapping(value = "/upload/token", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity getMyCostList(
			@RequestParam(value = "page_count", defaultValue = "20") String page_count,
			@RequestParam(value = "position", defaultValue = "") String position,
	ResponseEntity getQiNiuUploadToken(
			@RequestParam(value = "upload_type", defaultValue = "") String upload_type,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "getMyCostList", accessToken, version);
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "qiNiuUploadToken", accessToken, version);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("page_count", page_count);
		param.put("position", position);
		param.put("upload_type", upload_type);
		requestEntity.setParam(param);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		return responseEntity;
		return this.process(requestEntity, serviceManger, message);
	}

	/**
	 * 提交意见反馈
	 * 用户登录
	 *
	 * @param entity
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/feedback", method = RequestMethod.POST)
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity feedback(
	ResponseEntity userLogin(
			HttpServletRequest httpRequest,
			HttpEntity<Object> entity,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "feedback", accessToken, version);
			@RequestHeader("version") String version) throws Exception {
		String ip = MiscUtils.getIpAddr(httpRequest);
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "userLogin", null, version);
		requestEntity.setIp(ip);
		requestEntity.setParam(entity.getBody());
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);

		//根据相关条件将server_url列表信息返回
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}

	/**
	 * 获取系统配置信息
	 *
	 * @param config_key
	 * @param accessToken
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/sys/configuration", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity sysConfiguration(
			@RequestParam(value = "config_key", defaultValue = "") String config_key,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "getSysConfiguration", accessToken, version);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("config_key", config_key);
		requestEntity.setParam(param);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		return responseEntity;
	}

	/**
	 * 解码并重定向到分享地址
	 *
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
										@RequestParam(value = "type", defaultValue = "1") String type,
										@RequestParam(value = "phone", defaultValue = "") String phone,
										@RequestHeader("access_token") String accessToken,
										@RequestHeader("version") String version) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "sendVerificationCode", accessToken, null);
		Map<String, String> map = new HashMap<>();
		map.put("type", type);
		map.put("phone", phone);
		requestEntity.setParam(map);
		return this.process(requestEntity, serviceManger, message);
	}

	/**
	 * 绑定手机号码（校验验证码）
	 *
	 * @param accessToken       用户安全证书
	 * @param version           版本
	 * @throws Exception
	 */
	@RequestMapping(value = "/bindPhone", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity verifyVerificationCode(
			HttpEntity<Object> entity,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "bindPhone", accessToken, null);
		requestEntity.setParam(entity.getBody());
		return this.process(requestEntity, serviceManger, message);
	}

}
