package qingning.common.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public final class ServerUtils {
	private ServerUtils(){}

	private final static String CONST_PARAM_X_REAL_IP = "X-Real-IP";
	private final static String CONST_PARAM_X_FORWARDED_FOR = "X-Forwarded-For";
	private final static String CONST_PARAM_UNKNOW = "unknown";

	// 与微信接口配置信息中的Token要一致
	private static String CONST_PARAM_TOKEN = "Javen123456";

	/**
	 * 将字节数组转换为十六进制字符串
	 *
	 * @param byteArray
	 * @return
	 */
	private static String byteToStr(byte[] byteArray) {
		String strDigest = "";
		for (int i = 0; i < byteArray.length; i++) {
			strDigest += byteToHexStr(byteArray[i]);
		}
		return strDigest;
	}

	/**
	 * 将字节转换为十六进制字符串
	 *
	 * @param mByte
	 * @return
	 */
	private static String byteToHexStr(byte mByte) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] tempArr = new char[2];
		tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
		tempArr[1] = Digit[mByte & 0X0F];
		String s = new String(tempArr);
		return s;
	}

	private static void sort(String a[]) {
		for (int i = 0; i < a.length - 1; i++) {
			for (int j = i + 1; j < a.length; j++) {
				if (a[j].compareTo(a[i]) < 0) {
					String temp = a[i];
					a[i] = a[j];
					a[j] = temp;
				}
			}
		}
	}    

	/**
	 * 获取客户端IP
	 * @return IP
	 * */
	public static String getRequestIP(){
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String ip = request.getHeader(CONST_PARAM_X_REAL_IP);

		if (MiscUtils.isEmpty(ip) || CONST_PARAM_UNKNOW.equalsIgnoreCase(ip)) {
			ip = request.getHeader(CONST_PARAM_X_FORWARDED_FOR);
			if (MiscUtils.isEmpty(ip) || CONST_PARAM_UNKNOW.equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			} else {
				int index = ip.indexOf(',');
				if (index != -1) {
					ip = ip.substring(0, index);
				}
			}
		}

		return ip;
	}

	/**
	 * 验证签名
	 *
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @return
	 */
	public static boolean checkSignature(String signature, String timestamp, String nonce) throws Exception{
		String[] arr = new String[] { CONST_PARAM_TOKEN, timestamp, nonce };
		// 将token、timestamp、nonce三个参数进行字典序排序
		// Arrays.sort(arr);
		sort(arr);
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			content.append(arr[i]);
		}
		MessageDigest md = null;
		String tmpStr = null;

		try {
			md = MessageDigest.getInstance("SHA-1");
			// 将三个参数字符串拼接成一个字符串进行sha1加密
			byte[] digest = md.digest(content.toString().getBytes());
			tmpStr = byteToStr(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		content = null;
		// 将sha1加密后的字符串可与signature对比，标识该请求来源于微信
		return tmpStr != null ? tmpStr.equals(signature.toUpperCase()) : false;
	}

	/**效验手机验证码
	 * @param userId
	 * @param verification_code
	 * @param jedisUtils
	 * @return
	 */
	public static boolean verifyVerificationCode (String userId,String verification_code,JedisUtils jedisUtils) {
		Jedis jedis = jedisUtils.getJedis();
		Map<String,String> phoneMap = new HashMap();
		phoneMap.put("user_id",userId);
		phoneMap.put("code",verification_code);
		MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_LECTURER_ROOMS, phoneMap);
		String codeKey =  MiscUtils.getKeyOfCachedData(Constants.CAPTCHA_KEY_CODE, phoneMap);//根据userId 拿到 key
		if(!jedis.exists(codeKey)){
			return false;
		}
		String code = jedis.get(codeKey);
		if(!code.equals(verification_code)){//进行判断
			return false;
		}
		return true;
	}

	public static String getIpAddr(HttpServletRequest request){
		String ip = request.getHeader ("X-Real-IP");
		if (!org.apache.commons.lang.StringUtils.isBlank(ip) && !"unknown".equalsIgnoreCase (ip)) { return ip; }
		ip = request.getHeader ("X-Forwarded-For");
		if (!org.apache.commons.lang.StringUtils.isBlank(ip) && !"unknown".equalsIgnoreCase (ip)) {
			// 多次反向代理后会有多个IP值，第一个为真实IP。
			int index = ip.indexOf (',');
			if (index != -1) {
				return ip.substring (0, index);
			} else {
				return ip;
			}
		} else {
			return request.getRemoteAddr ();
		}
	}
}
