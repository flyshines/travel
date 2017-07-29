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
	@RequestMapping(value = "/shop/list", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity shopList(
			@RequestParam(value = "page_size", defaultValue = "10") long pageSize,
			@RequestParam(value = "page_num", defaultValue = "1") long pageNum,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
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
	 * 扫码调用
	 *
	 * @param accessToken 后台安全证书
	 * @param version     版本号
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/entering", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity shopList(
			HttpEntity<Object> entity,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "scanCode", accessToken, version);
		Map<String, Object> paramCode = (Map<String, Object>) entity.getBody();
		requestEntity.setParam(paramCode);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}




	/**
	 * 扫码调用
	 *
	 * @param accessToken 后台安全证书
	 * @param version     版本号
	 * @return
	 * @throws Exception
	 */
/*	@RequestMapping(value = "/admin/place/add", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity shopList(
			HttpEntity<Object> entity,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "scanCode", accessToken, version);
		Map<String, Object> paramCode = (Map<String, Object>) entity.getBody();
		requestEntity.setParam(paramCode);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}*/




















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
		String ip = ServerUtils.getIpAddr(httpRequest);
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
