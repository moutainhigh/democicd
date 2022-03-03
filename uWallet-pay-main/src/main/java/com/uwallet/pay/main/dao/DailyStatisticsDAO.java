package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.PosInfoDTO;
import com.uwallet.pay.main.model.entity.PosInfo;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * pos基本信息
 * </p>
 *
 * @package: com.fenmi.generator.mapper
 * @description: pos基本信息
 * @author: zhangzeyuan
 * @date: Created in 2021-03-19 15:17:59
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface DailyStatisticsDAO extends BaseDAO<PosInfo> {


    Long queryMerchantMarkting1();

    Long queryMerchantMarkting2();

    Long queryMerchantMarkting3(long startTime, long endTime);


    Long queryMerchantMarkting4(long startTime, long endTime);


    Long queryMerchantMarkting5(long startTime, long endTime);


    Long queryMerchantMarkting6(long startTime, long endTime);


    Long queryMerchantMarkting7(long startTime, long endTime);



    Long queryUserMarkting1(long startTime, long endTime);

    Long queryUserMarkting2(long startTime, long endTime);

    Long queryUserMarkting3(long startTime, long endTime);

    Long queryUserMarkting4(long startTime, long endTime);

    Long queryUserMarkting5(long startTime, long endTime);

    Long queryUserMarkting6(long startTime, long endTime);
    Long queryUserMarkting7(long startTime, long endTime);
    Long queryUserMarkting8(long startTime, long endTime);
    Long queryUserMarkting9(long startTime, long endTime);
    Long queryUserMarkting10(long startTime, long endTime);
    Long queryUserMarkting11(long startTime, long endTime);
    Long queryUserMarkting12(long startTime, long endTime);
    Long queryUserMarkting13(long startTime, long endTime);
    BigDecimal queryUserMarkting14(long startTime, long endTime);
    BigDecimal queryUserMarkting15(long startTime, long endTime);


    long queryOrder1(long startTime, long endTime);

    BigDecimal queryOrder2(long startTime, long endTime);
    long queryOrder3(long startTime, long endTime);
    BigDecimal queryOrder4(long startTime, long endTime);
    long queryOrder5(long startTime, long endTime);
    BigDecimal queryOrder6(long startTime, long endTime);
    BigDecimal queryOrder7(long startTime, long endTime);
    BigDecimal queryOrder8(long startTime, long endTime);
    BigDecimal queryOrder9(long startTime, long endTime);
    BigDecimal queryOrder10(long startTime, long endTime);
    BigDecimal queryOrder11(long startTime, long endTime);
    BigDecimal queryOrder12(long startTime, long endTime);
    BigDecimal queryOrder13(long startTime, long endTime);
    BigDecimal queryOrder14(long startTime, long endTime);
    BigDecimal queryOrder15(long startTime, long endTime);
    BigDecimal queryOrder16(long startTime, long endTime);
    BigDecimal queryOrder17(long startTime, long endTime);
    BigDecimal queryOrder18(long startTime, long endTime);
    BigDecimal queryOrder19(long startTime, long endTime);
    BigDecimal queryOrder20(long startTime, long endTime);
    BigDecimal queryOrder21(long startTime, long endTime);
    BigDecimal queryOrder22(long startTime, long endTime);
    BigDecimal queryOrder23(long startTime, long endTime);
    BigDecimal queryOrder24(long startTime, long endTime);
    BigDecimal queryOrder25(long startTime, long endTime);

    Map<String, Object> queryOrder26();
    Map<String, Object> queryOrder27(long startTime);

    BigDecimal queryOrder28();


    long queryRepay1(long startTime, long endTime);
    long queryRepay2(long startTime, long endTime);
    BigDecimal queryRepay3(long startTime, long endTime);
    long queryRepay4(long startTime, long endTime);
    BigDecimal queryRepay42(long startTime, long endTime);
    long queryRepay5(long startTime, long endTime);
    long queryRepay6(long startTime, long endTime);
    long queryRepay7(long startTime, long endTime);
    long queryRepay8(long startTime, long endTime);

    long queryMerchantSettle1(long startTime, long endTime);
    long queryMerchantSettle2(long startTime, long endTime);
    BigDecimal queryMerchantSettle3(long startTime, long endTime);
    long queryMerchantSettle4(long startTime, long endTime);
    Map<String, Object> queryMerchantSettle5();
    Map<String, Object> queryMerchantSettle6();

}
