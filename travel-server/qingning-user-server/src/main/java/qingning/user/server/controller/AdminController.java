package qingning.user.server.controller;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import qingning.common.entity.RequestEntity;
import qingning.common.entity.ResponseEntity;
import qingning.common.util.ServerUtils;
import qingning.server.AbstractController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("admin")
public class AdminController extends AbstractController{
	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);


	/**
	 * 登录
	 *
	 * @param version     版本号
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity sysLogin(
			HttpEntity<Object> entity,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "sysLogin", null, version);
		Map<String, Object> paramCode = (Map<String, Object>) entity.getBody();
		requestEntity.setParam(paramCode);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}


	/**
	 * 新增景区
	 *
	 * @param version     版本号
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/place/add", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity addPlace(
			HttpEntity<Object> entity,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "addPlace", accessToken, version);
		Map<String, Object> paramCode = (Map<String, Object>) entity.getBody();
		requestEntity.setParam(paramCode);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}
	/**
	 * 编辑景区
	 *
	 * @param version     版本号
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/place/edit", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity editPlace(
			HttpEntity<Object> entity,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "editPlace", accessToken, version);
		Map<String, Object> paramCode = (Map<String, Object>) entity.getBody();
		requestEntity.setParam(paramCode);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}
	/**
	 * editTicket
	 *
	 * @param version     版本号
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/ticket/edit", method = RequestMethod.POST)
	public
	@ResponseBody
	ResponseEntity editTicket(
			HttpEntity<Object> entity,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "editTicket", accessToken, version);
		Map<String, Object> paramCode = (Map<String, Object>) entity.getBody();
		requestEntity.setParam(paramCode);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}

	/**
	 * 收入列表
	 *
	 * @param version     版本号
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/income/list", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity incomeList(
			@RequestHeader("access_token") String accessToken,
			@RequestParam(value = "start_time", defaultValue = "") String start_time,
			@RequestParam(value = "end_time", defaultValue = "") String end_time,
			@RequestParam(value = "place_id", defaultValue = "") String place_id,
			@RequestParam(value = "keyword", defaultValue = "") String keyword,
			@RequestParam(value = "page_num",defaultValue = "1")  String page_num,
			@RequestParam(value = "page_size",defaultValue = "20")  String page_size,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "incomeList", accessToken, version);
		Map<String, Object> param = new HashMap<>();
		if(StringUtils.isNotEmpty(start_time))
			param.put("start_time", start_time);
		if(StringUtils.isNotEmpty(end_time))
			param.put("end_time", end_time);
		if(StringUtils.isNotEmpty(place_id))
			param.put("place_id", place_id);
		if(StringUtils.isNotEmpty(keyword))
			param.put("keyword", keyword);
		param.put("page_num", page_num);
		param.put("page_size", page_size);
		requestEntity.setParam(param);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}

	/**
	 * 用户列表
	 *
	 * @param version     版本号
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/user/list", method = RequestMethod.GET)
	public
	@ResponseBody
	ResponseEntity incomeList(
			@RequestHeader("access_token") String accessToken,
			@RequestParam(value = "type", defaultValue = "") String type,
			@RequestParam(value = "keyword", defaultValue = "") String keyword,
			@RequestParam(value = "page_num",defaultValue = "1")  String page_num,
			@RequestParam(value = "page_size",defaultValue = "20")  String page_size,
			@RequestHeader("version") String version
	) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "userList", accessToken, version);
		Map<String, Object> param = new HashMap<>();
		if(StringUtils.isNotEmpty(type))
			param.put("type", type);
		if(StringUtils.isNotEmpty(keyword))
			param.put("keyword", keyword);
		param.put("page_num", page_num);
		param.put("page_size", page_size);
		requestEntity.setParam(param);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();
		responseEntity.setReturnData(resultMap);
		return responseEntity;
	}





}
