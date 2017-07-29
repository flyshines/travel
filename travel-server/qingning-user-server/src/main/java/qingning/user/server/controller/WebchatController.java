package qingning.user.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import qingning.common.entity.RequestEntity;
import qingning.common.entity.ResponseEntity;
import qingning.common.util.MiscUtils;
import qingning.common.util.ServerUtils;
import qingning.server.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

@Controller
public class WebchatController extends AbstractController {

	private static final Logger logger   = LoggerFactory.getLogger(WebchatController.class);

	/**
	 * 微信公众号授权后的回调
	 * 前端 默认进行 微信静默授权
	 *  授权回调进入后台在后台获取code进行判断时候获取 openid
	 *  如果有就进行正常跳转
	 *  如果没有就进行手动授权
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/weixin/weixinlogin", method = RequestMethod.GET)
	public void weixinLogin(HttpServletRequest request,HttpServletResponse response) throws Exception {

		StringBuffer url = request.getRequestURL();//获取路径
		Map<String, String[]> params = request.getParameterMap();
		String[] codes = params.get("code");//拿到的code的值
		String code = codes[0];
		Map<String,String> map = new HashMap<>();
		map.put("code",code);
		String remote_ip_address = ServerUtils.getIpAddr(request);
		if("0:0:0:0:0:0:0:1".equals(remote_ip_address)){
			remote_ip_address = "127.0.0.1";
		}
		map.put("last_login_ip",remote_ip_address);
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "weixinCodeUserLogin", null, "");
		requestEntity.setParam(map);
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		Map<String, Object> resultMap = (Map<String, Object>) responseEntity.getReturnData();

		Integer key = Integer.valueOf(resultMap.get("key").toString());
		if(key == 1){
			//正常跳转到首页
			String userWeixinAccessToken = (String) resultMap.get("access_token");
			response.sendRedirect(MiscUtils.getConfigByKey("web_index")+userWeixinAccessToken);
			return ;
		}

		//如果没有拿到
		logger.info("没有拿到openId 或者 unionid 跳到手动授权页面");
		String authorization_url = MiscUtils.getConfigByKey("authorization_url");//手动授权url
		String appid = MiscUtils.getConfigByKey("appid");
		String redireceUrl = MiscUtils.getConfigByKey("redirect_url");
		String authorizationUrl = authorization_url.replace("APPID", appid).replace("REDIRECTURL", redireceUrl);//修改参数
		response.sendRedirect(authorizationUrl);
		return ;
	}

	/**
	 * 生成微信支付单
	 * @param accessToken 安全证书
	 * @param entity post参数
	 * @param version 版本号
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/payment/weixin/bill", method = RequestMethod.POST)
	public
	@ResponseBody
    ResponseEntity generateWeixinPayBill(
			HttpEntity<Object> entity,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version, HttpServletRequest request) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("UserServer", "generateWeixinPayBill", accessToken, version);
		String remote_ip_address = ServerUtils.getIpAddr(request);
		if("0:0:0:0:0:0:0:1".equals(remote_ip_address)){
			remote_ip_address = "127.0.0.1";
		}
		((Map<String,Object>)entity.getBody()).put("remote_ip_address",remote_ip_address);
		requestEntity.setParam(entity.getBody());
		ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
		return responseEntity;
	}

	/**
	 * 处理微信支付回调
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/payment/weixin/result", method = RequestMethod.POST)
	public void handleWeixinPayResult(HttpServletRequest request, HttpServletResponse response) throws Exception{
		logger.info("================> 接收到 weixinNotify 通知。==================");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = null;
		try{
			out =response.getWriter();
			String resultData = MiscUtils.convertStreamToString(request.getInputStream());
			SortedMap<String,String> requestMapData = MiscUtils.requestToMap(resultData);//notify请求数据
			logger.info("===> weixinNotify 请求数据：" + requestMapData);
			RequestEntity requestEntity = this.createRequestEntity("UserServer", "handleWeixinPayResult", "", "");
			requestEntity.setParam(requestMapData);
			ResponseEntity responseEntity = this.process(requestEntity, serviceManger, message);
			String result = responseEntity.getReturnData().toString();
			logger.info("===> result Data: " + result);
			out.println(result);
			out.flush();
			out.close();
		}catch(Exception e){
			logger.error("====================>  weixinNotify 处理异常  ========");
			logger.error(e.getMessage(), e);
			out.flush();
			out.close();
		}
		logger.info("==========================>  weixinNotify 处理完毕。 =======");
	}

	/**
     * 获得微信JS端配置
     * @param version
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/weixin/configuration", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity getWeiXinConfiguration(
            @RequestParam(value = "url", defaultValue = "") String url,
            @RequestHeader("version") String version
    ) throws Exception {
        RequestEntity requestEntity = this.createRequestEntity("UserServer", "weiXinConfiguration", null, version);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("url", url);
        requestEntity.setParam(param);
        return this.process(requestEntity, serviceManger, message);
    }
	/**
	 * 微信消息接收和token验证
	 * @param model
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/webchat/validation")
	public void tokenValidation(Model model, HttpServletRequest request,HttpServletResponse response) throws Exception {
		boolean isGet = request.getMethod().toLowerCase().equals("get");
		PrintWriter print;
		if (isGet) {
			// 微信加密签名
			String signature = request.getParameter("signature");
			// 时间戳
			String timestamp = request.getParameter("timestamp");
			// 随机数
			String nonce = request.getParameter("nonce");
			// 随机字符串
			String echostr = request.getParameter("echostr");
			// 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
			if (signature != null && ServerUtils.checkSignature(signature, timestamp, nonce)) {
				try {
					print = response.getWriter();
					print.write(echostr);
					print.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * 微信提现
	 * @param model
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/payment/weixin/withdraw")
	public void generateWeixinWithdrawBill(Model model, HttpServletRequest request,HttpServletResponse response) throws Exception {
		boolean isGet = request.getMethod().toLowerCase().equals("get");
		PrintWriter print;
		String transfersurl = MiscUtils.getConfigByKey("weixin_pay_transfers_url");

		if (isGet) {
			// 微信加密签名
			String signature = request.getParameter("signature");
			// 时间戳
			String timestamp = request.getParameter("timestamp");
			// 随机数
			String nonce = request.getParameter("nonce");
			// 随机字符串
			String echostr = request.getParameter("echostr");
			// 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
			if (signature != null && ServerUtils.checkSignature(signature, timestamp, nonce)) {
				try {
					print = response.getWriter();
					print.write(echostr);
					print.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
