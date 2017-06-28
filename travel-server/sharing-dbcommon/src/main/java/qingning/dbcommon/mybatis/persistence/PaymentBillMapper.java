package qingning.dbcommon.mybatis.persistence;

import java.util.Map;

public interface PaymentBillMapper {
	int insertPaymentBill(Map<String, Object> record);

/*    int deleteByPrimaryKey(String paymentId);


    int insert(PaymentBill record);

    int insertSelective(PaymentBill record);

    PaymentBill selectByPrimaryKey(String paymentId);

    int updateByPrimaryKeySelective(PaymentBill record);

    int updateByPrimaryKey(PaymentBill record);

    int updateByTradeIdKeySelective(PaymentBill updatePayBill);

    PaymentBill selectByTradeId(String tradeId);*/
	int updatePaymentBill(Map<String, Object> record);
	Map<String,Object> findPaymentBillByTradeId(String tradeId);
}