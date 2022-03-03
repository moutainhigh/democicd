import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.main.MainApplication;
import com.uwallet.pay.main.controller.AppEpochController;
import com.uwallet.pay.main.model.dto.RefundFlowDTO;
import com.uwallet.pay.main.model.entity.RefundFlow;
import com.uwallet.pay.main.service.LatPayService;
import com.uwallet.pay.main.service.OrderRefundService;
import com.uwallet.pay.main.service.ServerService;
import com.uwallet.pay.main.service.StripeAPIService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = MainApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
@ActiveProfiles("test")
public class StripeTest {
    @Autowired
    AppEpochController appEpochController;

    @Autowired
    ServerService serverService;

    @Autowired
    private OrderRefundService orderRefundService;

    @Autowired
    StripeAPIService stripeAPIService;

    @Test
    public void testCheckCardNoLast4() {

        //mock request
        MockHttpServletRequest request = new MockHttpServletRequest();
        //user token
        request.addHeader("Authorization", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjdXJyZW50IjoxNjQzMzYyMzg5MzQ2LCJsb2dpbk5hbWUiOiI2MTE4MTQxOTIyMCIsImlkIjo2MDYzNTU3NzgwNjIzNDAwOTYsImFwcFRva2VuS2V5IjoiNjA2MzU1Nzc4MDYyMzQwMDk2XzYxMTgxNDE5MjIwXzEwIn0.OpjP7nqCGlhMcwRn34Oz9hEe0CucdR9zkUNy0dDWLag");

        //request data
        JSONObject requestInfo = new JSONObject();
        JSONObject data = new JSONObject();
        //last 4 card number
        data.put("last4", "1234");
        requestInfo.put("data",data);

        //request
        R result = (R)appEpochController.stripeCheckCardNoRedundancy(requestInfo, request);

        //response
        JSONObject resData = (JSONObject) result.getData();
        String redundancyState = resData.getString("redundancyState");

        Assertions.assertThat(result.getCode()).isEqualTo(ErrorCodeEnum.SUCCESS_CODE.getCode()).as("Http request error");

        Assertions.assertThat(redundancyState).isEqualTo("0").as("This card number is repeated");
    }
    @Test
    public void stripeRefund() throws Exception {
        RefundFlowDTO refundFlowDTO = new RefundFlowDTO();
        refundFlowDTO.setStripeRefundNo("ch_3KVrN1Agx3Fd2j3e1jRVGO3G");
        refundFlowDTO.setRefundAmount(new BigDecimal(2));
        refundFlowDTO.setId(651590287359561729l);
        JSONObject jsonObject = serverService.stripeRefund(null,refundFlowDTO,null,null);
        log.info("结果:{}",jsonObject);
    }

    @Test
    public void stripeRefundCheck() throws Exception {
        orderRefundService.refundStatusCheck();
//        JSONObject param=new JSONObject();
//        param.put("paymentIntent","pi_3KRXufAgx3Fd2j3e0nO8HzWN");
//        param.put("id",651590287359561729l);
//        stripeAPIService.stripeRefundCheck(param);
    }


}
