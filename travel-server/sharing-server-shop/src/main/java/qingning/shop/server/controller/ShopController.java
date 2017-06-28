package qingning.shop.server.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import qingning.common.entity.RequestEntity;
import qingning.common.entity.ResponseEntity;
import qingning.server.AbstractController;

@RestController
@RequestMapping(value="/shop")
public class ShopController extends AbstractController{
	
	/**
	 * 获取店铺信息
	 * @param query_type
	 * @param shop_id
	 * @param accessToken
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/info",method=RequestMethod.GET)
	public 
	@ResponseBody 
	ResponseEntity queryShop(
			@RequestParam(value="query_type", defaultValue="0") long query_type,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version) throws Exception{
		RequestEntity requestEntity = this.createRequestEntity("ShopServer", "queryShop", accessToken, version);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("query_type", query_type);
		requestEntity.setParam(param);
		
		return this.process(requestEntity, serviceManger, message);
	}
	
	/**
	 * 编辑店铺信息
	 * @param entity
	 * @param shop_id
	 * @param accessToken
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/update/{shop_id}",method=RequestMethod.PUT)
	public 
	@ResponseBody 
	ResponseEntity updateShop(
			HttpEntity<Object> entity,
		    @PathVariable("shop_id") String shop_id,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("ShopServer", "updateShop", accessToken, version);
		((Map<String, Object>)entity.getBody()).put("shop_id", shop_id);
		requestEntity.setParam(entity.getBody());
		return this.process(requestEntity, serviceManger, message);
		
	}
	
	/**
	 * 获取店铺课程
	 * @param shop_id
	 * @param share_shop_id
	 * @param position
	 * @param page_count
	 * @param accessToken
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/course/list/{shop_id}",method=RequestMethod.GET)
	public 
	@ResponseBody 
	ResponseEntity getShopCourseList(
			@PathVariable("shop_id") String shop_id,
			@RequestParam(value="share_shop_id", defaultValue="") String share_shop_id,
			@RequestParam(value="position", defaultValue="0") long position,
			@RequestParam(value="page_count", defaultValue="10") long page_count,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("ShopServer", "getShopCourseList", accessToken, version);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("share_shop_id", share_shop_id);
		param.put("shop_id", shop_id);
		param.put("position", position);
		param.put("page_count", page_count);
		requestEntity.setParam(param);
		
		return this.process(requestEntity, serviceManger, message);
	}
	
	/**
	 * 获取店铺中的课程id集合
	 * @param shop_id
	 * @param accessToken
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/course/idList/{shop_id}",method=RequestMethod.GET)
	public 
	@ResponseBody 
	ResponseEntity getShopCourseIdList(
			@PathVariable("shop_id") String shop_id,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("ShopServer", "getShopCourseIdList", accessToken, version);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("shop_id", shop_id);
		requestEntity.setParam(param);
		
		return this.process(requestEntity, serviceManger, message);
	}
	
	/**
	 * 获取店铺销售统计
	 * @param shop_id
	 * @param page_count
	 * @param position
	 * @param accessToken
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/sale/statistics/{shop_id}",method=RequestMethod.GET)
	public 
	@ResponseBody 
	ResponseEntity saleStatistics(
			@PathVariable("shop_id") String shop_id,
			@RequestParam(value="page_count", defaultValue="10") long page_count,
			@RequestParam(value="position", defaultValue="0") long position,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("ShopServer", "saleStatistics", accessToken, version);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("shop_id", shop_id);
		param.put("page_count", page_count);
		param.put("position", position);
		requestEntity.setParam(param);
		
		return this.process(requestEntity, serviceManger, message);
	}
	
	/**
	 * 获取店铺销售记录
	 * @param shop_id
	 * @param page_count
	 * @param position
	 * @param accessToken
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/sale/record/{shop_id}",method=RequestMethod.GET)
	public 
	@ResponseBody 
	ResponseEntity getSaleRecord(
			@PathVariable("shop_id") String shop_id,
			@RequestParam(value="page_count", defaultValue="10") long page_count,
			@RequestParam(value="position", defaultValue="0") long position,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("ShopServer", "getSaleRecord", accessToken, version);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("shop_id", shop_id);
		param.put("page_count", page_count);
		param.put("position", position);
		requestEntity.setParam(param);
		
		return this.process(requestEntity, serviceManger, message);
	}
	
	/**
	 * 获取店铺客户统计
	 * @param shop_id
	 * @param page_count
	 * @param position
	 * @param accessToken
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/customer/statistics/{shop_id}",method=RequestMethod.GET)
	public 
	@ResponseBody 
	ResponseEntity getCustomerStatistics(
			@PathVariable("shop_id") String shop_id,
			@RequestParam(value="page_count", defaultValue="10") long page_count,
			@RequestParam(value="position", defaultValue="0") long position,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("ShopServer", "getCustomerStatistics", accessToken, version);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("shop_id", shop_id);
		param.put("page_count", page_count);
		param.put("position", position);
		requestEntity.setParam(param);
		
		return this.process(requestEntity, serviceManger, message);
	}
	
	/**
	 * 获取店铺指定客户的购买记录
	 * @param shop_id
	 * @param shop_customer_id
	 * @param position
	 * @param page_count
	 * @param accessToken
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/customer/buy_record/{shop_id}",method=RequestMethod.GET)
	public 
	@ResponseBody 
	ResponseEntity getCustomerBuyRecord(
			@PathVariable("shop_id") String shop_id,
		    @RequestParam("shop_customer_id") String shop_customer_id,
		    @RequestParam(value="position", defaultValue="0") String position,
		    @RequestParam(value="page_count", defaultValue="10") String page_count,
		    @RequestHeader("access_token") String accessToken,
		    @RequestHeader("version") String version) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("ShopServer", "getCustomerBuyRecord", accessToken, version);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("shop_id", shop_id);
		param.put("shop_customer_id", shop_customer_id);
		param.put("page_count", page_count);
		param.put("position", position);
		requestEntity.setParam(param);
		
		return this.process(requestEntity, serviceManger, message);
	}
	
	/**
	 * 上下架课程
	 * @param entity
	 * @param shop_id
	 * @param accessToken
	 * @param version
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/course/putaway/{shop_id}", method = RequestMethod.PUT)
	public
	@ResponseBody
	ResponseEntity putawayCourse(
			HttpEntity<Object> entity,
			@PathVariable("shop_id") String shop_id,
			@RequestHeader("access_token") String accessToken,
			@RequestHeader("version") String version) throws Exception {
		RequestEntity requestEntity = this.createRequestEntity("ShopServer", "putawayCourse", accessToken, version);
		((Map<String,Object>)entity.getBody()).put("shop_id", shop_id);
		requestEntity.setParam(entity.getBody());
		return this.process(requestEntity, serviceManger, message);
	}

	
}
