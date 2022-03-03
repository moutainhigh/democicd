
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.*;
import com.uwallet.pay.main.MainApplication;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest(classes = MainApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class TestUnit {

    @Autowired
    RechargeFlowService rechargeFlowService;

    @Autowired
    WithholdFlowService withholdFlowService;

    @Autowired
    LatPayService latPayService;

    @Autowired
    UserService userService;

    @Autowired
    OmiPayService omiPayService;

    @Autowired
    OrderRefundService orderRefundService;

    @Autowired
    NoticeMassService noticeMassService;

    @Autowired
    AliyunSmsService aliyunSmsService;

    @Autowired
    ParametersConfigService parametersConfigService;

    @Autowired
    MerchantService merchantService;

    @Autowired
    QrPayService qrPayService;

    @Autowired
    MailTemplateService mailTemplateService;

    @Autowired
    ReconciliationService reconciliationService;

    @Autowired
    ServerService serverService;

    @Autowired
    StaticDataService staticDataService;

    @Autowired
    SplitService splitService;

    @Value("${latpay.tieOnCardUrl}")
    private String tieOnCardUrl;

    @Value("${latpay.payUrl}")
    private String payUrl;

    @Autowired
    RedisUtils redisUtils;

    @Value("${google.mapGeocodingAPI}")
    private String googleMapsApi;

    @Value("${google.mapGeocodingAPIKey}")
    private String googleApiKey;

    @Value("${spring.pushFirebaseFilePath}")
    private String pushFirebaseFilePath;

    @Value("${spring.pushFirebaseUrl}")
    private String pushFirebaseUrl;

    @Value("${uWallet.credit}")
    private String creditUrl;

    @Autowired
    private DocuSignService docuSignService;

    @Autowired
    private HolidaysConfigService holidaysConfigService;

    @Autowired
    private QrPayFlowService qrPayFlowService;

    @Autowired
    private WholeSalesFlowService wholeSalesFlowService;

    @Autowired
    private NoticeService noticeService;
    @Autowired
    private TagService tagService;
    @Autowired
    private LoginMissService loginMissService;

    @Test
    public void getBorrowList(){
        try {
            /*List<BorrowDTO> borrowList = this.getRes(); //serverService.getBorrowList(Arrays.asList(513915369360674816L, 513653164648124416L, 513652946317824000L, 513652394880094208L, 513650783126507520L)).toJavaList(BorrowDTO.class);
            POIUtils.createExcel(borrowList,BorrowDTO.class,"tessssst");*/
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    private List<T> getRes() {
        String x ="[\n" +
                "        {\n" +
                "            \"id\":\"513650783126507520\",\n" +
                "            \"createdBy\":\"0\",\n" +
                "            \"createdDate\":\"19:51:40 25/01/2021\",\n" +
                "            \"modifiedBy\":\"511429469322039296\",\n" +
                "            \"modifiedDate\":\"10:45:45 26/01/2021\",\n" +
                "            \"status\":1,\n" +
                "            \"ip\":\"27.213.62.173\",\n" +
                "            \"code\":\"INVT2021012500000010\",\n" +
                "            \"userId\":\"470406657698648064\",\n" +
                "            \"userName\":\"zez yu\",\n" +
                "            \"merchantId\":\"502001941046054912\",\n" +
                "            \"merchantName\":\"测试下载图片3\",\n" +
                "            \"borrowAmount\":1.68,\n" +
                "            \"settleAmount\":1.62,\n" +
                "            \"orderAmount\":2,\n" +
                "            \"redEnvelopeAmount\":0,\n" +
                "            \"serviceFee\":0,\n" +
                "            \"truelyInterestAmount\":\"\",\n" +
                "            \"overdueDays\":0,\n" +
                "            \"state\":10,\n" +
                "            \"settleStatus\":20,\n" +
                "            \"settleDelay\":0,\n" +
                "            \"productId\":397253654985920512,\n" +
                "            \"productRepaymentSort\":1,\n" +
                "            \"productRepaymentWay\":1,\n" +
                "            \"productInterestPenaltyWay\":\"\",\n" +
                "            \"productInterestRate\":\"\",\n" +
                "            \"productServiceRate\":\"\",\n" +
                "            \"productInterestPenaltyRate\":0,\n" +
                "            \"productInterestPenaltyType\":1,\n" +
                "            \"productGracePeriodDays\":1,\n" +
                "            \"productGracePeriodRate\":0.0001,\n" +
                "            \"productPenaltyUpperLimit\":\"\",\n" +
                "            \"productOverdueFine\":1,\n" +
                "            \"productPeriod\":1,\n" +
                "            \"productBorrowUnit\":2,\n" +
                "            \"productFixedRepaymentDate\":3,\n" +
                "            \"borrowCloseReasonId\":\"\",\n" +
                "            \"periodQuantity\":1,\n" +
                "            \"repayList\":[\n" +
                "                {\n" +
                "                    \"id\":\"513650783143284736\",\n" +
                "                    \"createdBy\":\"0\",\n" +
                "                    \"createdDate\":\"19:51:40 25/01/2021\",\n" +
                "                    \"modifiedBy\":\"0\",\n" +
                "                    \"modifiedDate\":\"19:51:40 25/01/2021\",\n" +
                "                    \"status\":1,\n" +
                "                    \"ip\":\"103.149.26.226\",\n" +
                "                    \"borrowId\":513650783126507520,\n" +
                "                    \"periodSort\":1,\n" +
                "                    \"billDate\":1612108800000,\n" +
                "                    \"expectRepayTime\":\"00:00:00 03/02/2021\",\n" +
                "                    \"shouldPayAmount\":1.68,\n" +
                "                    \"payPrincipal\":1.68,\n" +
                "                    \"payInterest\":0,\n" +
                "                    \"payInterestPenalty\":0,\n" +
                "                    \"truelyPenaltyInterestAmount\":0,\n" +
                "                    \"gracePeriodAmount\":0,\n" +
                "                    \"paidAmount\":0,\n" +
                "                    \"truelyRepayTime\":\"\",\n" +
                "                    \"truelyRepayAmount\":0,\n" +
                "                    \"truelyPayPrincipal\":0,\n" +
                "                    \"truelyInterestAmount\":0,\n" +
                "                    \"truelyGracyPeriodDaysReal\":1,\n" +
                "                    \"truelyGracyPeriodAmountReal\":0,\n" +
                "                    \"truelyOverdueDays\":0,\n" +
                "                    \"truelyOverdueAmount\":\"\",\n" +
                "                    \"violateAmount\":0,\n" +
                "                    \"truelyViolateAmount\":0,\n" +
                "                    \"repayCount\":0,\n" +
                "                    \"state\":0,\n" +
                "                    \"transactionId\":\"\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"autoRepayFailTimes\":0\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\":\"513652394880094208\",\n" +
                "            \"createdBy\":\"0\",\n" +
                "            \"createdDate\":\"19:58:04 25/01/2021\",\n" +
                "            \"modifiedBy\":\"511429469322039296\",\n" +
                "            \"modifiedDate\":\"10:45:45 26/01/2021\",\n" +
                "            \"status\":1,\n" +
                "            \"ip\":\"27.213.62.173\",\n" +
                "            \"code\":\"INVT2021012500000011\",\n" +
                "            \"userId\":\"470406657698648064\",\n" +
                "            \"userName\":\"zez yu\",\n" +
                "            \"merchantId\":\"502001941046054912\",\n" +
                "            \"merchantName\":\"测试下载图片3\",\n" +
                "            \"borrowAmount\":0.84,\n" +
                "            \"settleAmount\":0.81,\n" +
                "            \"orderAmount\":1,\n" +
                "            \"redEnvelopeAmount\":0,\n" +
                "            \"serviceFee\":0,\n" +
                "            \"truelyInterestAmount\":\"\",\n" +
                "            \"overdueDays\":0,\n" +
                "            \"state\":10,\n" +
                "            \"settleStatus\":20,\n" +
                "            \"settleDelay\":0,\n" +
                "            \"productId\":397253654985920512,\n" +
                "            \"productRepaymentSort\":1,\n" +
                "            \"productRepaymentWay\":1,\n" +
                "            \"productInterestPenaltyWay\":\"\",\n" +
                "            \"productInterestRate\":\"\",\n" +
                "            \"productServiceRate\":\"\",\n" +
                "            \"productInterestPenaltyRate\":0,\n" +
                "            \"productInterestPenaltyType\":1,\n" +
                "            \"productGracePeriodDays\":1,\n" +
                "            \"productGracePeriodRate\":0.0001,\n" +
                "            \"productPenaltyUpperLimit\":\"\",\n" +
                "            \"productOverdueFine\":1,\n" +
                "            \"productPeriod\":1,\n" +
                "            \"productBorrowUnit\":2,\n" +
                "            \"productFixedRepaymentDate\":3,\n" +
                "            \"borrowCloseReasonId\":\"\",\n" +
                "            \"periodQuantity\":1,\n" +
                "            \"repayList\":[\n" +
                "                {\n" +
                "                    \"id\":\"513652394896871424\",\n" +
                "                    \"createdBy\":\"0\",\n" +
                "                    \"createdDate\":\"19:58:04 25/01/2021\",\n" +
                "                    \"modifiedBy\":\"0\",\n" +
                "                    \"modifiedDate\":\"19:58:04 25/01/2021\",\n" +
                "                    \"status\":1,\n" +
                "                    \"ip\":\"103.149.26.226\",\n" +
                "                    \"borrowId\":513652394880094208,\n" +
                "                    \"periodSort\":1,\n" +
                "                    \"billDate\":1612108800000,\n" +
                "                    \"expectRepayTime\":\"00:00:00 03/02/2021\",\n" +
                "                    \"shouldPayAmount\":0.84,\n" +
                "                    \"payPrincipal\":0.84,\n" +
                "                    \"payInterest\":0,\n" +
                "                    \"payInterestPenalty\":0,\n" +
                "                    \"truelyPenaltyInterestAmount\":0,\n" +
                "                    \"gracePeriodAmount\":0,\n" +
                "                    \"paidAmount\":0,\n" +
                "                    \"truelyRepayTime\":\"\",\n" +
                "                    \"truelyRepayAmount\":0,\n" +
                "                    \"truelyPayPrincipal\":0,\n" +
                "                    \"truelyInterestAmount\":0,\n" +
                "                    \"truelyGracyPeriodDaysReal\":1,\n" +
                "                    \"truelyGracyPeriodAmountReal\":0,\n" +
                "                    \"truelyOverdueDays\":0,\n" +
                "                    \"truelyOverdueAmount\":\"\",\n" +
                "                    \"violateAmount\":0,\n" +
                "                    \"truelyViolateAmount\":0,\n" +
                "                    \"repayCount\":0,\n" +
                "                    \"state\":0,\n" +
                "                    \"transactionId\":\"\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"autoRepayFailTimes\":0\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\":\"513652946317824000\",\n" +
                "            \"createdBy\":\"0\",\n" +
                "            \"createdDate\":\"20:00:15 25/01/2021\",\n" +
                "            \"modifiedBy\":\"511429469322039296\",\n" +
                "            \"modifiedDate\":\"10:45:45 26/01/2021\",\n" +
                "            \"status\":1,\n" +
                "            \"ip\":\"27.213.62.173\",\n" +
                "            \"code\":\"INVT2021012500000012\",\n" +
                "            \"userId\":\"503701741164515328\",\n" +
                "            \"userName\":\"123 123\",\n" +
                "            \"merchantId\":\"502001941046054912\",\n" +
                "            \"merchantName\":\"测试下载图片3\",\n" +
                "            \"borrowAmount\":4.2,\n" +
                "            \"settleAmount\":4.05,\n" +
                "            \"orderAmount\":5,\n" +
                "            \"redEnvelopeAmount\":0,\n" +
                "            \"serviceFee\":0,\n" +
                "            \"truelyInterestAmount\":\"\",\n" +
                "            \"overdueDays\":0,\n" +
                "            \"state\":10,\n" +
                "            \"settleStatus\":20,\n" +
                "            \"settleDelay\":0,\n" +
                "            \"productId\":397253654985920512,\n" +
                "            \"productRepaymentSort\":1,\n" +
                "            \"productRepaymentWay\":1,\n" +
                "            \"productInterestPenaltyWay\":\"\",\n" +
                "            \"productInterestRate\":\"\",\n" +
                "            \"productServiceRate\":\"\",\n" +
                "            \"productInterestPenaltyRate\":0,\n" +
                "            \"productInterestPenaltyType\":1,\n" +
                "            \"productGracePeriodDays\":1,\n" +
                "            \"productGracePeriodRate\":0.0001,\n" +
                "            \"productPenaltyUpperLimit\":\"\",\n" +
                "            \"productOverdueFine\":1,\n" +
                "            \"productPeriod\":1,\n" +
                "            \"productBorrowUnit\":2,\n" +
                "            \"productFixedRepaymentDate\":3,\n" +
                "            \"borrowCloseReasonId\":\"\",\n" +
                "            \"periodQuantity\":1,\n" +
                "            \"repayList\":[\n" +
                "                {\n" +
                "                    \"id\":\"513652946334601216\",\n" +
                "                    \"createdBy\":\"0\",\n" +
                "                    \"createdDate\":\"20:00:15 25/01/2021\",\n" +
                "                    \"modifiedBy\":\"0\",\n" +
                "                    \"modifiedDate\":\"20:00:15 25/01/2021\",\n" +
                "                    \"status\":1,\n" +
                "                    \"ip\":\"103.149.26.226\",\n" +
                "                    \"borrowId\":513652946317824000,\n" +
                "                    \"periodSort\":1,\n" +
                "                    \"billDate\":1612108800000,\n" +
                "                    \"expectRepayTime\":\"00:00:00 03/02/2021\",\n" +
                "                    \"shouldPayAmount\":4.2,\n" +
                "                    \"payPrincipal\":4.2,\n" +
                "                    \"payInterest\":0,\n" +
                "                    \"payInterestPenalty\":0,\n" +
                "                    \"truelyPenaltyInterestAmount\":0,\n" +
                "                    \"gracePeriodAmount\":0,\n" +
                "                    \"paidAmount\":0,\n" +
                "                    \"truelyRepayTime\":\"\",\n" +
                "                    \"truelyRepayAmount\":0,\n" +
                "                    \"truelyPayPrincipal\":0,\n" +
                "                    \"truelyInterestAmount\":0,\n" +
                "                    \"truelyGracyPeriodDaysReal\":1,\n" +
                "                    \"truelyGracyPeriodAmountReal\":0,\n" +
                "                    \"truelyOverdueDays\":0,\n" +
                "                    \"truelyOverdueAmount\":\"\",\n" +
                "                    \"violateAmount\":0,\n" +
                "                    \"truelyViolateAmount\":0,\n" +
                "                    \"repayCount\":0,\n" +
                "                    \"state\":0,\n" +
                "                    \"transactionId\":\"\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"autoRepayFailTimes\":0\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\":\"513653164648124416\",\n" +
                "            \"createdBy\":\"0\",\n" +
                "            \"createdDate\":\"20:01:07 25/01/2021\",\n" +
                "            \"modifiedBy\":\"511429469322039296\",\n" +
                "            \"modifiedDate\":\"10:45:45 26/01/2021\",\n" +
                "            \"status\":1,\n" +
                "            \"ip\":\"27.213.62.173\",\n" +
                "            \"code\":\"INVT2021012500000013\",\n" +
                "            \"userId\":\"470406657698648064\",\n" +
                "            \"userName\":\"zez yu\",\n" +
                "            \"merchantId\":\"502001941046054912\",\n" +
                "            \"merchantName\":\"测试下载图片3\",\n" +
                "            \"borrowAmount\":1.68,\n" +
                "            \"settleAmount\":1.62,\n" +
                "            \"orderAmount\":2,\n" +
                "            \"redEnvelopeAmount\":0,\n" +
                "            \"serviceFee\":0,\n" +
                "            \"truelyInterestAmount\":\"\",\n" +
                "            \"overdueDays\":0,\n" +
                "            \"state\":10,\n" +
                "            \"settleStatus\":20,\n" +
                "            \"settleDelay\":0,\n" +
                "            \"productId\":397253654985920512,\n" +
                "            \"productRepaymentSort\":1,\n" +
                "            \"productRepaymentWay\":1,\n" +
                "            \"productInterestPenaltyWay\":\"\",\n" +
                "            \"productInterestRate\":\"\",\n" +
                "            \"productServiceRate\":\"\",\n" +
                "            \"productInterestPenaltyRate\":0,\n" +
                "            \"productInterestPenaltyType\":1,\n" +
                "            \"productGracePeriodDays\":1,\n" +
                "            \"productGracePeriodRate\":0.0001,\n" +
                "            \"productPenaltyUpperLimit\":\"\",\n" +
                "            \"productOverdueFine\":1,\n" +
                "            \"productPeriod\":1,\n" +
                "            \"productBorrowUnit\":2,\n" +
                "            \"productFixedRepaymentDate\":3,\n" +
                "            \"borrowCloseReasonId\":\"\",\n" +
                "            \"periodQuantity\":1,\n" +
                "            \"repayList\":[\n" +
                "                {\n" +
                "                    \"id\":\"513653164660707328\",\n" +
                "                    \"createdBy\":\"0\",\n" +
                "                    \"createdDate\":\"20:01:07 25/01/2021\",\n" +
                "                    \"modifiedBy\":\"0\",\n" +
                "                    \"modifiedDate\":\"20:01:07 25/01/2021\",\n" +
                "                    \"status\":1,\n" +
                "                    \"ip\":\"103.149.26.226\",\n" +
                "                    \"borrowId\":513653164648124416,\n" +
                "                    \"periodSort\":1,\n" +
                "                    \"billDate\":1612108800000,\n" +
                "                    \"expectRepayTime\":\"00:00:00 03/02/2021\",\n" +
                "                    \"shouldPayAmount\":1.68,\n" +
                "                    \"payPrincipal\":1.68,\n" +
                "                    \"payInterest\":0,\n" +
                "                    \"payInterestPenalty\":0,\n" +
                "                    \"truelyPenaltyInterestAmount\":0,\n" +
                "                    \"gracePeriodAmount\":0,\n" +
                "                    \"paidAmount\":0,\n" +
                "                    \"truelyRepayTime\":\"\",\n" +
                "                    \"truelyRepayAmount\":0,\n" +
                "                    \"truelyPayPrincipal\":0,\n" +
                "                    \"truelyInterestAmount\":0,\n" +
                "                    \"truelyGracyPeriodDaysReal\":1,\n" +
                "                    \"truelyGracyPeriodAmountReal\":0,\n" +
                "                    \"truelyOverdueDays\":0,\n" +
                "                    \"truelyOverdueAmount\":\"\",\n" +
                "                    \"violateAmount\":0,\n" +
                "                    \"truelyViolateAmount\":0,\n" +
                "                    \"repayCount\":0,\n" +
                "                    \"state\":0,\n" +
                "                    \"transactionId\":\"\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"autoRepayFailTimes\":0\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\":\"513915369360674816\",\n" +
                "            \"createdBy\":\"0\",\n" +
                "            \"createdDate\":\"13:23:02 26/01/2021\",\n" +
                "            \"modifiedBy\":\"0\",\n" +
                "            \"modifiedDate\":\"13:23:02 26/01/2021\",\n" +
                "            \"status\":1,\n" +
                "            \"ip\":\"103.149.26.226\",\n" +
                "            \"code\":\"INVT20210126000000-2\",\n" +
                "            \"userId\":\"470406657698648064\",\n" +
                "            \"userName\":\"zez yu\",\n" +
                "            \"merchantId\":\"502001941046054912\",\n" +
                "            \"merchantName\":\"测试下载图片3\",\n" +
                "            \"borrowAmount\":1.68,\n" +
                "            \"settleAmount\":1.62,\n" +
                "            \"orderAmount\":2,\n" +
                "            \"redEnvelopeAmount\":0,\n" +
                "            \"serviceFee\":0,\n" +
                "            \"truelyInterestAmount\":\"\",\n" +
                "            \"overdueDays\":0,\n" +
                "            \"state\":10,\n" +
                "            \"settleStatus\":10,\n" +
                "            \"settleDelay\":0,\n" +
                "            \"productId\":397253654985920512,\n" +
                "            \"productRepaymentSort\":1,\n" +
                "            \"productRepaymentWay\":1,\n" +
                "            \"productInterestPenaltyWay\":\"\",\n" +
                "            \"productInterestRate\":\"\",\n" +
                "            \"productServiceRate\":\"\",\n" +
                "            \"productInterestPenaltyRate\":0,\n" +
                "            \"productInterestPenaltyType\":1,\n" +
                "            \"productGracePeriodDays\":1,\n" +
                "            \"productGracePeriodRate\":0.0001,\n" +
                "            \"productPenaltyUpperLimit\":\"\",\n" +
                "            \"productOverdueFine\":1,\n" +
                "            \"productPeriod\":1,\n" +
                "            \"productBorrowUnit\":2,\n" +
                "            \"productFixedRepaymentDate\":3,\n" +
                "            \"borrowCloseReasonId\":\"\",\n" +
                "            \"periodQuantity\":1,\n" +
                "            \"repayList\":[\n" +
                "                {\n" +
                "                    \"id\":\"513915369394229248\",\n" +
                "                    \"createdBy\":\"0\",\n" +
                "                    \"createdDate\":\"13:23:02 26/01/2021\",\n" +
                "                    \"modifiedBy\":\"0\",\n" +
                "                    \"modifiedDate\":\"13:23:02 26/01/2021\",\n" +
                "                    \"status\":1,\n" +
                "                    \"ip\":\"103.149.26.226\",\n" +
                "                    \"borrowId\":513915369360674816,\n" +
                "                    \"periodSort\":1,\n" +
                "                    \"billDate\":1612195200000,\n" +
                "                    \"expectRepayTime\":\"00:00:00 03/02/2021\",\n" +
                "                    \"shouldPayAmount\":1.68,\n" +
                "                    \"payPrincipal\":1.68,\n" +
                "                    \"payInterest\":0,\n" +
                "                    \"payInterestPenalty\":0,\n" +
                "                    \"truelyPenaltyInterestAmount\":0,\n" +
                "                    \"gracePeriodAmount\":0,\n" +
                "                    \"paidAmount\":0,\n" +
                "                    \"truelyRepayTime\":\"\",\n" +
                "                    \"truelyRepayAmount\":0,\n" +
                "                    \"truelyPayPrincipal\":0,\n" +
                "                    \"truelyInterestAmount\":0,\n" +
                "                    \"truelyGracyPeriodDaysReal\":1,\n" +
                "                    \"truelyGracyPeriodAmountReal\":0,\n" +
                "                    \"truelyOverdueDays\":0,\n" +
                "                    \"truelyOverdueAmount\":\"\",\n" +
                "                    \"violateAmount\":0,\n" +
                "                    \"truelyViolateAmount\":0,\n" +
                "                    \"repayCount\":0,\n" +
                "                    \"state\":0,\n" +
                "                    \"transactionId\":\"\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"autoRepayFailTimes\":0\n" +
                "        }\n" +
                "    ]\n";
        return null;//JSONObject.parseArray(x,BorrowDTO.class);
    }

    @Test
    public void accountVerifyTest(){
        try {
            TroubleMakerUtil.makeTrouble("1","ops trouble");
            JSONObject param = new JSONObject(5);
            param.put("a","a");
            JSONObject data = new JSONObject(5);
            data.put("b","b");
            param.put("data",data);
            serverService.testVerifyAccountServer(param)  ;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
    @Test
    public void tagAppInterface(){
        System.out.println(tagService.getTagInfo(null));
    }
    @Test
    public void addMerchantDistanceInfo(){
        List<MerchantDTO> merchantDTOList = merchantService.find(new JSONObject(), null, null);
        System.out.println(merchantDTOList.size());
    }
    @Test
    public void tagTest(){
        String tags = "Afghan,African,American,Arabian,Asian,Asian Fusion,Australian,Austrian,BBQ,Bakery,Bar Food,Belgian,Beverages,Brasserie,Brazilian,British,Bubble Tea,Burger,Burmese,Cabinet Food,Cafe Food,Cambodian,Canadian,Cantonese,Caribbean,Charcoal Chicken,Chinese,Coffee and Tea,Colombian,Contemporary,Continental,Creole,Crepes,Deli,Desserts,Drinks Only,Dumplings,Dutch,Eastern European,Ethiopian,European,Filipino,Finger Food,Fish and Chips,French,Fried Chicken,Frozen Yogurt,Fusion,German,Gluten Free,Greek,Grill,Hawaiian,Healthy Food,Hot Pot,Ice Cream,Indian,Indonesian,International,Iranian,Irish,Italian,Japanese,Japanese BBQ,Juices,Kebab,Kiwi,Korean,Korean BBQ,Laotian,Latin American,Lebanese,Malatang,Malaysian,Meat Pie,Mediterranean,Mexican,Middle Eastern,Modern Australian,Modern European,Mongolian,Moroccan,Nepalese,North Indian,Pakistani,Pan Asian,Patisserie,Peruvian,Pho,Pizza,Poké,Polish,Portuguese,Pub Food,Ramen,Roast,Salad,Sandwich,Seafood,Sichuan,Singaporean,Soul Food,South African,South Indian,Spanish,Sri Lankan,Steak,Street Food,Sushi,Taiwanese,Tapas,Tea,Teppanyaki,Teriyaki,Tex-Mex,Thai,Tibetan,Turkish,Uyghur,Vegan,Vegetarian,Vietnamese,Yum Cha";
        String[] tagList = tags.split(",");
        for (String tag : tagList) {
            try {
                TagDTO tagDTO = new TagDTO();
                tagDTO.setCnName(tag);
                tagDTO.setEnValue(tag);
                tagDTO.setPopular(BigDecimal.ZERO);
                tagDTO.setShowState(1);
                tagDTO.setParentId(0L);
                tagService.saveTag(tagDTO, null);
            }catch (Exception e){
                System.out.println(tag);
            }
        }
    }
    @Test
    public void loginErrorTest() throws BizException {
        List<Long> longs = Arrays.asList(474413919824433152L,474425178712100864L,474453973397590016L,474465378976845824L,474528396414734336L,474536174097190912L,474546837255278592L,474624110398459904L,474634594593984512L);
        JSONObject params = new JSONObject();
        for (Long aLong : longs) {
            params.put("userId",aLong);
            LoginMissDTO loginMissDTO = loginMissService.findOneLoginMiss(params);
            Locale locale = new Locale("zh_CN");
            long currentTimeMillis = System.currentTimeMillis();
            BigDecimal remainTime = (new BigDecimal(currentTimeMillis).subtract(new BigDecimal(loginMissDTO.getLastErrorTime())));
            if (loginMissDTO.getLastErrorTime() != null && remainTime .compareTo( new BigDecimal(1000 * 60 * 60))<=0) {
                //log.info("user info , user data:{}, log in info:{}", userDTO, loginData);
                //剩余冻结时间
                long frozenTimeMin = 60 - ( currentTimeMillis- loginMissDTO.getLastErrorTime()) / (1000 * 60);
                String s = I18nUtils.get("login.lock", locale, new String[]{frozenTimeMin + " min"});
                System.out.println(s);
            }
        }
    }


    @Test
    public void test() throws Exception {
//        BufferedImage bufferedImage = QRCodeUtil.createQRCode("https://h5.uwallet.net.au/html/app.html", "C:\\Users\\Lenovo\\Pictures\\Camera Roll\\13928177_195158772185_2.jpg");
//        QRCodeUtil.writeFile(bufferedImage, "logo", "D:\\");
        omiPayService.statusCheck("");
    }

    @Test
    public void test0 () throws Exception {
        JSONObject requestInfo = new JSONObject();
        //请求第三方接口
        requestInfo.put("merchant_User_Id", "610055901");
        requestInfo.put("merchantpwd", "XSEdRGXjPO9tPq5M0L49");
        requestInfo.put("merchant_ipaddress", "54.66.135.254");
        requestInfo.put("customer_firstname", "Ning");
        requestInfo.put("customer_lastname", "Lin");
        requestInfo.put("customer_phone", "15684101026");
        requestInfo.put("customer_email", "66.llnn@gmail.com");
        requestInfo.put("customer_ipaddress", "127.0.0.1");
        requestInfo.put("bill_firstname", "Ning");
        requestInfo.put("bill_lastname", "Lin");
        requestInfo.put("bill_address1", "4 Kumnka PL");
        requestInfo.put("bill_address2", "Kuraby");
        requestInfo.put("bill_city", "Brisbane");
        requestInfo.put("bill_country", "Australia");
        requestInfo.put("bill_zip", "4112");
        requestInfo.put("CrdStrg_Token", "0x032a6b84bb901cb0f5e4fb23dae89b6461cd64d9");
        requestInfo.put("customer_cc_cvc", "164");
        requestInfo.put("currencydesc", "AUD");
        requestInfo.put("merchant_ref_number", SnowflakeUtil.generateId().toString());
        requestInfo.put("amount", "0.1");
//        if (fee != null) {
//            requestInfo.put("fees", fee.toString());
//        }
        requestInfo.put("scsscheck", "D");
        log.info("send msg to latpay, data:{}", requestInfo);
        Map params = requestInfo;
        String data = HttpClientUtils.sendPostForm(payUrl, params);
        // 解析响应结果
        JSONObject returnData = JSONResultHandle.resultHandle(data);
        log.info("get msg from latpay, data:{}", returnData);
        System.out.println(returnData.toJSONString());
    }

    @Test
    public void test1() throws Exception {
        String a = new BigDecimal("0.01").setScale(2,BigDecimal.ROUND_HALF_UP).toString() + "AUD" + "01" + "mJvY5Qwm";
        System.out.println(a);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = messageDigest.digest(a.getBytes());
        String s = Hex.encodeHexString(bytes);
        System.out.println(s);

        JSONObject pro = new JSONObject();
        pro.put("productid", "1891");
        pro.put("productname", "Smart card");
        pro.put("productsku", "shop_dept_item_7895");
        pro.put("productdescription", "Kingston Smart SD Card # sd43545");
        pro.put("productcategory", "Electronics");
        pro.put("productURL", "http://lateralpaymentsolutions.com");
        pro.put("quantity", 1);
        pro.put("priceperunit", 0.2);
        JSONArray f = new JSONArray();
        f.add(pro);


        JSONObject requestInfo = new JSONObject();
        requestInfo.put("accountid", "test_JSYCapital");
        requestInfo.put("storeid", "001");
        requestInfo.put("deviceid", "01");
        requestInfo.put("merchantkey", s);
        JSONObject consumer = new JSONObject();
        consumer.put("firstname", "Ning");
        consumer.put("lastname", "Lin");
        consumer.put("phone", "0416298188");
        consumer.put("email", "66.llnn@gmail.com");
        requestInfo.put("consumer", consumer);
        JSONObject order = new JSONObject();
//        order.put("orderid", SnowflakeUtil.generateId().toString());
        order.put("reference", SnowflakeUtil.generateId());
        order.put("currency", "AUD");
        order.put("amount", new BigDecimal("0.01").setScale(2,BigDecimal.ROUND_HALF_UP));
        order.put("purchasesummary", "uwallet purchase by account");
        requestInfo.put("order", order);
        JSONObject callbackParams = new JSONObject();
        callbackParams.put("orderNo", "testOrderNo");
        requestInfo.put("callback_params", callbackParams);
        JSONObject billing = new JSONObject();
        billing.put("type", "dd");
        JSONObject directDebit = new JSONObject();
        directDebit.put("bsb", "064001");
        directDebit.put("accountnumber", "12013175");
        directDebit.put("accountname", "Ning Lin");
        billing.put("directdebit", directDebit);
        JSONObject address = new JSONObject();
        address.put("line1", "4 Kumnka PL");
        address.put("line2", "Kuraby");
        address.put("city", "Brisbane");
        address.put("state", "QLD");
        address.put("country", "AU");
        address.put("zipcode", "4112");
        billing.put("address", address);
        JSONObject fees = new JSONObject();
        fees.put("processingfee", new BigDecimal("0.00").toString());
        billing.put("fees", fees);
        requestInfo.put("billing", billing);
        requestInfo.put("notifyurl", "http://paytest-api.loancloud.cn/latPay/notify");
        System.out.println("send message: " + requestInfo.toJSONString());
        String result = HttpClientUtils.post("https://l4p2s7p2r4o3c9e3ss.com/POS/POSProcessRequest/Checkoutnav.aspx", requestInfo.toJSONString());
        System.out.println("result:" + result);
    }

    @Test
    public void test2 () throws Exception {
        String a = "{\n" +
                "\t\"bill_city\": \"Brisbane\",\n" +
                "\t\"bill_zip\": \"4112\",\n" +
                "\t\"amount\": \"0.1\",\n" +
                "\t\"fees\": \"0.01\",\n" +
                "\t\"customer_lastname\": \"Lin\",\n" +
                "\t\"customer_ipaddress\": \"112.237.85.252\",\n" +
                "\t\"CrdStrg_Token\": \"0xeff372e593bfb900add691c294d207b44cc7b8d6\",\n" +
                "\t\"merchant_ipaddress\": \"117.50.21.70\",\n" +
                "\t\"customer_firstname\": \"Ning\",\n" +
                "\t\"customer_phone\": \"61123456781\",\n" +
                "\t\"bill_address1\": \"4 Kumnka PL\",\n" +
                "\t\"bill_lastname\": \"Lin\",\n" +
                "\t\"bill_country\": \"AU\",\n" +
                "\t\"merchantpwd\": \"83pE2r8K0zkU0Rgxbwfk\",\n" +
                "\t\"bill_firstname\": \"Ning\",\n" +
                "\t\"currencydesc\": \"AUD\",\n" +
                "\t\"customer_cc_cvc\": \"164\",\n" +
                "\t\"customer_email\": \"66.llnn@gmail.com\",\n" +
                "\t\"scsscheck\": \"D\",\n" +
                "\t\"merchant_User_Id\": \"test_JSYCapital\",\n" +
                "\t\"merchant_ref_number\": \"408140367018938368\"\n" +
                "}";

        JSONObject b = JSONObject.parseObject(a);
        Map params = b;
        String data = HttpClientUtils.sendPostForm(payUrl, params);
        System.out.println(data);
    }


    public void test3 () throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet1 = workbook.createSheet("应收数据");
        // 设置缺省列高
        sheet1.setDefaultRowHeightInPoints(12);
        // 设置缺省列宽
        sheet1.setDefaultColumnWidth(20);
        HSSFFont font1 = workbook.createFont();
        font1.setFontName("宋体");
        // 字体大小
        font1.setFontHeightInPoints((short)12);
        HSSFCellStyle style1 = workbook.createCellStyle();
        // 单元格居中对齐
        style1.setAlignment(HorizontalAlignment.CENTER);
        style1.setFont(font1);
        for (int i = 0; i < 5; i ++) {
            HSSFRow row = sheet1.createRow(i);
            switch (i) {
                case 0:
                    HSSFCell date = row.createCell(0);
                    HSSFRichTextString text = new HSSFRichTextString("应收日期");
                    date.setCellValue(text);
                    HSSFCell dateValue = row.createCell(1);
                    dateValue.setCellValue("应收日期");
                    break;
                case 1:
                    HSSFCell platform = row.createCell(0);
                    HSSFRichTextString text1 = new HSSFRichTextString("支付平台");
                    platform.setCellValue(text1);
                    HSSFCell platformValue = row.createCell(1);
                    platformValue.setCellValue("支付平台");
                    break;
                case 2:
                    HSSFCell wechat = row.createCell(0);
                    HSSFRichTextString text2 = new HSSFRichTextString("WechatPay");
                    wechat.setCellValue(text2);
                    HSSFCell wechatValue = row.createCell(1);
                    wechatValue.setCellValue("WechatPay");
                    break;
                case 3:
                    HSSFCell aliPay = row.createCell(0);
                    HSSFRichTextString text3 = new HSSFRichTextString("Alipay");
                    aliPay.setCellValue(text3);
                    HSSFCell aliPayValue = row.createCell(1);
                    aliPayValue.setCellValue("Alipay");
                    break;
                case 4:
                    HSSFCell total = row.createCell(0);
                    total.setCellValue("合计");
                    HSSFCell totalValue = row.createCell(1);
                    totalValue.setCellValue(new BigDecimal("14.99").setScale(2, RoundingMode.UP).doubleValue());
                    break;
            }
        }
        HSSFSheet sheet2 = workbook.createSheet("应付数据");
        // 设置缺省列高
        sheet2.setDefaultRowHeightInPoints(12);
        // 设置缺省列宽
        sheet2.setDefaultColumnWidth(20);
        HSSFFont font2 = workbook.createFont();
        font2.setFontName("宋体");
        // 字体大小
        font2.setFontHeightInPoints((short)12);
        HSSFCellStyle style2 = workbook.createCellStyle();
        // 单元格居中对齐
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setFont(font2);
        //应付日期
        HSSFRow row1 = sheet1.createRow(0);
        HSSFCell aliPay = row1.createCell(0);
        HSSFRichTextString text = new HSSFRichTextString("应付日期");
        aliPay.setCellValue(text);
        HSSFCell aliPayValue = row1.createCell(1);
        aliPayValue.setCellValue("2020/04/22");
        //设置头
        String[] headers = {"商户名称", "商户ABN", "付款银行户名", "银行账户编号", "BSB编号", "金额", "备注"};
        HSSFRow row2 = sheet1.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            HSSFCell createCell = row2.createCell(i);
            createCell.setCellStyle(style2);
            HSSFRichTextString rowText = new HSSFRichTextString(headers[i]);
            createCell.setCellValue(rowText);
        }
        for (int i = 1; i < 2 + 1; i++) {
            HSSFRow row3 = sheet2.createRow(i);
            if (i == 1) {
                for (int j = 0; j < headers.length; j++) {
                    HSSFCell createCell = row2.createCell(j);
                    createCell.setCellStyle(style2);
                    HSSFRichTextString data = new HSSFRichTextString(headers[j]);
                    createCell.setCellValue(data);
                }
                continue;
            }
            //应付笔数、合计应付金额
            switch (i) {
                case 2:
                    HSSFCell textOut1 = row3.createCell(8);
                    HSSFRichTextString totalAmount = new HSSFRichTextString("应付笔数");
                    textOut1.setCellValue(totalAmount);
                    HSSFCell dataOut1 = row3.createCell(9);
                    dataOut1.setCellValue(new Integer(2));
                    break;
                case 3:
                    HSSFCell textOut2 = row3.createCell(8);
                    HSSFRichTextString paySum = new HSSFRichTextString("应付笔数");
                    textOut2.setCellValue(paySum);
                    HSSFCell dataOut2 = row3.createCell(9);
                    dataOut2.setCellValue(new BigDecimal(2).setScale(2, RoundingMode.UP).doubleValue());
                    break;
            }
        }
    }

    @Test
    public void test4() throws Exception {
//        try {
//            Session session = MailUtil.getSession("2722806393@qq.com");
////            MimeMessage mimeMessage = MailUtil.getMimeMessage("2722806393@qq.com", "2412620990@qq.com", "aaa", "fdasfdas" , null, session);
//            List<String> email = new ArrayList<>(1);
//            email.add("2722806393@qq.com");
//            MailUtil.sendMail("2722806393@qq.com", "2412620990@qq.com", "2722806393@qq.com","tapplbtmeilrdeeb", "dfasfad", "fdafda", null, email);
//            //记录邮件流水
////            saveMailLog(email,sendMsg,0,request);
//        } catch (Exception e) {
//
//        }
//        String data = EncryptUtil.decrypt("+wKivUtOUmx47vdko1Khpwu2vzUS7HHTfhAOc8GMAlKDu/hk5EybxfzPaTXW0mpGSW7gCoSDJDxg\n" +
//                "KUkH5bu4e8ahbbVE5ugfZfhiOO4LMtfyFMAgSp6ItZy4f5hQqxteStEAnBIYh0jFf50u0m+m4IpY\n" +
//                "M27Eet3S2jdpR8SW1/qsXjWpHW7h6B/Jp6lRIcWnPwrW8X7LBwY+x7DSTHzAWGT9uBoKfmfFLLf0\n" +
//                "xFf2l+AIf55lqQDTpTgQiKjF/zL02pYOiX7zQaxrdfu4ygdZYnR6yMu3VVS3atwHMEPeEhJhcm0R\n" +
//                "98vRCPEp/XIYok7ZHgSGRjYpiCp6/tqdTlsw7fpmHmUjYsGovJwkEu+cszA=", EncryptUtil.aesKey, EncryptUtil.aesIv);
//        log.info("data:{}", data);
        List<JSONObject> a = new ArrayList<>(1);
//        JSONObject r1 = new JSONObject();
//        r1.put("order_no", "TR2005182192345006415");
//        r1.put("out_refund_no", "422284211402231808");
//        r1.put("amount", new BigDecimal("0.45"));
        JSONObject r2 = new JSONObject();
        r2.put("order_no", "TR2005182192345006361");
        r2.put("out_refund_no", "422282690002767872");
        r2.put("amount", new BigDecimal("0.01"));
        JSONObject r3 = new JSONObject();
        r3.put("order_no", "TR2005182192345006288");
        r3.put("out_refund_no", "422280536764534784");
        r3.put("amount", new BigDecimal("0.01"));
//        a.add(r1);
        a.add(r2);
        a.add(r3);
        for (JSONObject jsonObject : a) {
            omiPayService.refundApply(jsonObject);
        }
//        omiPayService.statusCheck("TR2005122192345004453");
    }

    @Test
    public void test5() throws Exception {
        String a = null;
        String b = "x";
        System.out.println(a + " " + b);
    }

    @Test
    public void test6() throws Exception {
        try {
//            String mapsApi = googleMapsApi + merchantDTO.getAddress().replaceAll(" ", "+") + ",+" + merchantDTO.getCity().replaceAll(" ", "+") + ",+" + staticDataDTO.getEnName() + "&key=" + googleApiKey;
            String mapsApi = googleMapsApi + "fdsafda" + "&key=" + googleApiKey;
            JSONObject location = JSONObject.parseObject(HttpClientUtils.sendGet(mapsApi));
            System.out.println(location);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Test
    public void test12() throws Exception {
        JSONObject textTabs = new JSONObject();
        textTabs.put("companyInformation_businessRegisteredName", "LV");
        textTabs.put("companyInformation_tradingName", "L·V");
        textTabs.put("companyInformation_streetAddress", "三站");
//        textTabs.put("companyInformation_city", "烟台");
        textTabs.put("companyInformation_suburb", "烟台");
        textTabs.put("companyInformation_state", "山东省");
        textTabs.put("companyInformation_postalCode", "1888");
        textTabs.put("companyInformation_companyABNACN", "1222");
        textTabs.put("companyInformation_companyPhone", "15684101026");
        textTabs.put("companyInformation_companyWebsite", "www.baidu.com");

        textTabs.put("authorisedPerson_name_a", "Jack Brown");
        textTabs.put("authorisedPerson_DOB_a", "01/01/1999");
        textTabs.put("authorisedPerson_driverLicenceNo_a", "10000");
        textTabs.put("authorisedPerson_passportNumber_a", "11111");
        textTabs.put("authorisedPerson_address_a", "240 Railway Pde, Cabramatta NSW 2166, Australia");
        textTabs.put("authorisedPerson_driverLicenseState_a", "ELS");

        textTabs.put("authorisedPerson_name_b", "Jack Brown");
        textTabs.put("authorisedPerson_DOB_b", "01/01/1999");
        textTabs.put("authorisedPerson_driverLicenceNo_b", "10000");
        textTabs.put("authorisedPerson_passportNumber_b", "11111");
        textTabs.put("authorisedPerson_address_b", "240 Railway Pde, Cabramatta NSW 2166, Australia");
        textTabs.put("authorisedPerson_driverLicenseState_b", "ELS");

        textTabs.put("authorisedPerson_name_c", "Jack Brown");
        textTabs.put("authorisedPerson_DOB_c", "01/01/1999");
        textTabs.put("authorisedPerson_driverLicenceNo_c", "10000");
        textTabs.put("authorisedPerson_passportNumber_c", "11111");
        textTabs.put("authorisedPerson_address_c", "240 Railway Pde, Cabramatta NSW 2166, Australia");
        textTabs.put("authorisedPerson_driverLicenseState_c", "ELS");

        textTabs.put("owners_firstName_a", "Jack");
        textTabs.put("owners_lastName_a", "Alen");
        textTabs.put("owners_DOB_a", "01/01/1999");
        textTabs.put("owners_passportNumber_a", "1");
        textTabs.put("owners_driverLicenceNumber_a", "2");
        textTabs.put("owners_ownership_a", "19");
        textTabs.put("owners_driverLicenseState_a", "ELS");

        textTabs.put("owners_firstName_b", "Jack");
        textTabs.put("owners_lastName_b", "Alen");
        textTabs.put("owners_DOB_b", "01/01/1999");
        textTabs.put("owners_passportNumber_b", "1");
        textTabs.put("owners_driverLicenceNumber_b", "2");
        textTabs.put("owners_ownership_b", "19");
        textTabs.put("owners_driverLicenseState_b", "ELS");

        textTabs.put("owners_firstName_c", "Jack");
        textTabs.put("owners_lastName_c", "Alen");
        textTabs.put("owners_DOB_c", "Alen");
        textTabs.put("owners_passportNumber_c", "1");
        textTabs.put("owners_driverLicenceNumber_c", "1");
        textTabs.put("owners_ownership_c", "1");
        textTabs.put("owners_driverLicenseState_c", "ELS");

        textTabs.put("owners_firstName_d", "Jack");
        textTabs.put("owners_lastName_d", "Alen");
        textTabs.put("owners_DOB_d", "1");
        textTabs.put("owners_passportNumber_d", "1");
        textTabs.put("owners_driverLicenceNumber_d", "1");
        textTabs.put("owners_ownership_d", "1");
        textTabs.put("owners_driverLicenseState_d", "ELS");

        textTabs.put("contactInformation_name", "Jack Brown");
        textTabs.put("contactInformation_title", "Chief");
        textTabs.put("contactInformation_mobile", "15684101026");
        textTabs.put("contactInformation_email", "2412620990@qq.com");
        textTabs.put("contactInformation_wechat", "aaaa");

        textTabs.put("bankingInformation_accountName", "bank");
        textTabs.put("bankingInformation_accountNumber", "1000");
        textTabs.put("bankingInformation_bsb", "10000");

        textTabs.put("agreementDetails_appChargeFee", "1");
        textTabs.put("agreementDetails_uPaymentSolutionDiscount", "2");

        JSONObject requestInfo = new JSONObject();
        requestInfo.put("signerName", "xxx");
        requestInfo.put("signerEmail", "xxx@qq.com");
        requestInfo.put("clientUserId", SnowflakeUtil.generateId());
        requestInfo.put("callBackUrl", "https://www.baidu.com");
        requestInfo.put("textTabs", textTabs);

        log.info("docusign request data:{}", requestInfo);

        JSONObject requsetResult = null;

        try {
            requsetResult = docuSignService.genSignUrl(requestInfo, null);
        } catch (Exception e) {
            log.info("error message:{}, e:{}", e.getMessage(), e);
        }

        log.info("docusign request result:{}", requsetResult);

    }

    @Test
    public void test13() throws Exception {
        List<String> list = new ArrayList<>();
        // LocalDate默认的时间格式为2020-02-02
        LocalDate startDate = LocalDate.parse("2020-01-01");
        LocalDate endDate = LocalDate.parse("2020-01-04");
        long distance = ChronoUnit.DAYS.between(startDate, endDate);
        List<String> finalList = list;
        Stream.iterate(startDate, d -> d.plusDays(1)).limit(distance + 1).forEach(f -> finalList.add(f.toString()));
        if (!list.isEmpty()) {
            list = finalList.stream()
                    .filter(date -> !holidaysConfigService.isItAHoliday(date))
                    .filter(date -> {
                        boolean isWeekend = false;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar calendar = Calendar.getInstance();
                        try {
                            calendar.setTime(sdf.parse(date));
                            isWeekend = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return !isWeekend;
                    })
                    .collect(Collectors.toList());
        }
        System.out.println(list);

    }

    @Test
    public void test14() throws Exception {
//        String key = new BigDecimal("1.00").setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "USD" + "ST1" + "bK5NDRFgLv3";
//        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
//        byte[] bytes = messageDigest.digest(key.getBytes());
//        key = Hex.encodeHexString(bytes);
//        System.out.println(key);
//        System.out.println(key.equals("025a7953b87b3677bb058222abb3482933e8d66dfe6206d26720e64646402bed"));
//        JSONObject a = new JSONObject();
//        a.put("envelopId", "c0ae8ce9-85a0-4f3d-a2b8-462cd8e72a43");
//        System.out.println(docuSignService.getDocument(a, null));
        JSONObject accountData = serverService.getAccountInfo(423708857482170368l);
        JSONObject amountIn = new JSONObject();
        amountIn.put("userId", 423708857482170368l);
        amountIn.put("amountInUserId", 423708857482170368l);
        amountIn.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getMessage());
        amountIn.put("channelSerialnumber", SnowflakeUtil.generateId());
        amountIn.put("accountId", accountData.getLongValue("id"));
        amountIn.put("transAmount", new BigDecimal("100"));
        amountIn.put("transType", 1);
        JSONObject msg = serverService.amountIn(amountIn);
    }

    @Test
    public void test15() throws Exception {
        JSONObject fileRequest = new JSONObject();
        fileRequest.put("envelopId", "a073cabd-36d9-4d87-9fce-59c514fa2a54");
        fileRequest.put("contractType", 1);
        JSONObject docuSignFiles = null;
        try {
            docuSignFiles = docuSignService.getDocument(fileRequest, null);
        } catch (Exception e) {
            log.info("docuSignFiles download failed, merchantId:{}, error message:{}, e:{}", "f", e.getMessage(), e);
        }
    }

    @Test
    public void test16() throws Exception {
        try {
            String mapsApi = googleMapsApi + "19b/803 Stanley St.".replaceAll(" ", "+") + ",+" + "Woolloongabba+Brisbane" + ",+" + "QLD" + "&key=" + googleApiKey;
            JSONObject location = JSONObject.parseObject(HttpClientUtils.sendGet(mapsApi));
            location = location.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
            System.out.println("lat:" + location.getString("lat") + "lng" + location.getString("lng"));
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void test17() throws Exception {
//        splitService.splitTransactionDoubleHandle();
//        File file = new File("C:\\Users\\Lenovo\\Desktop\\新建文件夹 (2)\\Merchant Application Form.pdf");
//        InputStream inputStream = new FileInputStream(file);
//        MultipartFile multipartFile = new MockMultipartFile(file.getName(), inputStream);
//        String uploadPath = AmazonAwsUploadUtil.upload(multipartFile, "docuSignContract"+"/"+"Merchant Application Form_498273822934487040.pdf");
//        System.out.println(uploadPath);
//        Session session = MailUtil.getSession("info@payo.com.au");
//        MimeMessage mimeMessage = MailUtil.getMimeMessage("U-Biz", "info@payo.com.au", "2412620990@qq.com", "sendTitle", "sendMsg" , null, session);
//        MailUtil.sendMail(session, mimeMessage, "info@payo.com.au", "P@yo2025");

//        noticeService.noticeTitleChange();
//        wholeSalesFlowService.wholeSaleAmountInFailedHandle();

//        UserDTO userDTO = userService.findUserById(498278848892981248L);
//        FirebaseDTO firebaseDTO = new FirebaseDTO();
//        firebaseDTO.setAppName("UWallet");
//        firebaseDTO.setUserId(userDTO.getId());
//        firebaseDTO.setTitle("wwwxxx");
//        firebaseDTO.setBody("qqqxxx");
//        firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
//        firebaseDTO.setRoute(StaticDataEnum.PUSH_ROUTE_2.getCode());
//        serverService.pushFirebase(firebaseDTO);

//        UserDTO userDTO1 = userService.findUserById(479061680720662528L);
        FirebaseDTO firebaseDTO1 = new FirebaseDTO();
        firebaseDTO1.setAppName("UWallet");
        firebaseDTO1.setUserId(468544216048029696L);
        firebaseDTO1.setTitle("【U-Biz】New order -voice");
        firebaseDTO1.setBody("【U-Biz】You have a new order! Order No. :INVT2020122100000039.");
        firebaseDTO1.setVoice(StaticDataEnum.VOICE_0.getCode());
        firebaseDTO1.setRoute(StaticDataEnum.PUSH_ROUTE_1.getCode());
        serverService.pushFirebase(firebaseDTO1,null);
    }

}
