package com.uwallet.pay.main.service.impl;

import com.google.common.collect.Maps;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.main.dao.DailyStatisticsDAO;
import com.uwallet.pay.main.service.DailyStatisticsService;
import com.uwallet.pay.main.util.POIUtils;
import com.uwallet.pay.main.util.TestEnvUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 接入方商户表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 接入方商户表
 * @author: zhoutt
 * @date: Created in 2020-09-25 08:55:53
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class DailyStatisticsServiceImpl extends BaseServiceImpl implements DailyStatisticsService {

    @Resource
    private DailyStatisticsDAO dailyStatisticsDAO;

    @Override
    public void dailyStatistics() {
        if(TestEnvUtil.isTestEnv()){
            return;
        }
//        String filePath = "D:\\PAYO每日运营\\";
        String filePath = "/data/wwwroot/uwallet/payoDailyStatistics/";
        //获取开始结束时间戳
        Map<String, Object> timeMap = getCHNTime();
//        Map<String, Object> timeMap = getAUSTime();

        long startTime = (long)timeMap.get("startTime");
        long endTime = (long)timeMap.get("endTime");

        //获取当天格式化时间
        String todayStr = new SimpleDateFormat("dd-MMM-yy", Locale.US).format(startTime);
        String fileName = todayStr + "-DailyStatistics.xlsx";
        // 创建 Workbook 对象(excel 的文档对象)
        XSSFWorkbook workbook = new XSSFWorkbook();

        //商户运营统计
        long sumMm1 = dailyStatisticsDAO.queryMerchantMarkting1();
        long sumMm2 = dailyStatisticsDAO.queryMerchantMarkting2();
        long sumMm3 = dailyStatisticsDAO.queryMerchantMarkting3(startTime, endTime);
        long sumMm4 = dailyStatisticsDAO.queryMerchantMarkting4(startTime, endTime);
        long sumMm5 = dailyStatisticsDAO.queryMerchantMarkting5(startTime, endTime);
        long sumMm6 = dailyStatisticsDAO.queryMerchantMarkting6(startTime, endTime);
        long sumMm7 = dailyStatisticsDAO.queryMerchantMarkting7(startTime, endTime);

        XSSFSheet sheet1 = workbook.createSheet("商户运营统计");
        Row row1 = sheet1.createRow(0);
        row1.createCell(0).setCellValue(sumMm1);
        row1.createCell(1).setCellValue(sumMm2);
        row1.createCell(2).setCellValue("appsflyer");
        row1.createCell(3).setCellValue("appsflyer");
        row1.createCell(4).setCellValue(sumMm3);
        row1.createCell(5).setCellValue(sumMm4);
        row1.createCell(6).setCellValue(sumMm5);
        row1.createCell(7).setCellValue(sumMm6);
        row1.createCell(8).setCellValue(sumMm7);

        //用户运营统计
        long sumUM1 = dailyStatisticsDAO.queryUserMarkting1(startTime, endTime);
        long sumUM2 = dailyStatisticsDAO.queryUserMarkting2(startTime, endTime);
        long sumUM3 = dailyStatisticsDAO.queryUserMarkting3(startTime, endTime);
        long sumUM4 = dailyStatisticsDAO.queryUserMarkting4(startTime, endTime);
        long sumUM5 = dailyStatisticsDAO.queryUserMarkting5(startTime, endTime);
        long sumUM6 = dailyStatisticsDAO.queryUserMarkting6(startTime, endTime);
        long sumUM7 = dailyStatisticsDAO.queryUserMarkting7(startTime, endTime);
        long sumUM8 = dailyStatisticsDAO.queryUserMarkting8(startTime, endTime);
        long sumUM9 = dailyStatisticsDAO.queryUserMarkting9(startTime, endTime);
        long sumUM10 = dailyStatisticsDAO.queryUserMarkting10(startTime, endTime);
        long sumUM11 = dailyStatisticsDAO.queryUserMarkting11(startTime, endTime);
        long sumUM12 = dailyStatisticsDAO.queryUserMarkting12(startTime, endTime);
        long sumUM13 = dailyStatisticsDAO.queryUserMarkting13(startTime, endTime);
        BigDecimal sumUM14 = dailyStatisticsDAO.queryUserMarkting14(startTime, endTime);
        BigDecimal sumUM15 = dailyStatisticsDAO.queryUserMarkting15(startTime, endTime);

        XSSFSheet sheet2 = workbook.createSheet("用户运营统计");
        XSSFRow row2 = sheet2.createRow(0);
        row2.createCell(0).setCellValue("appsflyer");
        row2.createCell(1).setCellValue("appsflyer");
        row2.createCell(2).setCellValue("appsflyer");
        row2.createCell(3).setCellValue("appsflyer");
        row2.createCell(4).setCellValue(sumUM1);
        row2.createCell(5).setCellValue(sumUM2);
        row2.createCell(6).setCellValue(sumUM3);
        row2.createCell(7).setCellValue(sumUM4);
        row2.createCell(8).setCellValue(sumUM5);
        row2.createCell(9).setCellValue(sumUM5);
        row2.createCell(10).setCellValue(sumUM6);
        row2.createCell(11).setCellValue(sumUM7);
        row2.createCell(12).setCellValue(sumUM8);
        row2.createCell(13).setCellValue(sumUM9);
        row2.createCell(14).setCellValue(sumUM10);
        row2.createCell(15).setCellValue(sumUM11);
        row2.createCell(16).setCellValue(sumUM12);
        row2.createCell(17).setCellValue(sumUM13);
        row2.createCell(18).setCellValue(sumUM14.add(sumUM15).toString());

        //订单统计
        long orderR1 = dailyStatisticsDAO.queryOrder1(startTime, endTime);
        BigDecimal orderR2 = dailyStatisticsDAO.queryOrder2(startTime, endTime);
        long orderR3 = dailyStatisticsDAO.queryOrder3(startTime, endTime);
        BigDecimal orderR4 = dailyStatisticsDAO.queryOrder4(startTime, endTime);
        long orderR5 = dailyStatisticsDAO.queryOrder5(startTime, endTime);
        BigDecimal orderR6 = dailyStatisticsDAO.queryOrder6(startTime, endTime);
        BigDecimal orderR7 = dailyStatisticsDAO.queryOrder7(startTime, endTime);
        BigDecimal orderR8 = dailyStatisticsDAO.queryOrder8(startTime, endTime);
        BigDecimal orderR9 = dailyStatisticsDAO.queryOrder9(startTime, endTime);
        BigDecimal orderR10 = dailyStatisticsDAO.queryOrder10(startTime, endTime);
        BigDecimal orderR11 = dailyStatisticsDAO.queryOrder11(startTime, endTime);
        BigDecimal orderR12 = dailyStatisticsDAO.queryOrder12(startTime, endTime);
        BigDecimal orderR13 = dailyStatisticsDAO.queryOrder13(startTime, endTime);
        BigDecimal orderR14 = dailyStatisticsDAO.queryOrder14(startTime, endTime);
        BigDecimal orderR15 = dailyStatisticsDAO.queryOrder15(startTime, endTime);
        BigDecimal  orderR16 = dailyStatisticsDAO.queryOrder16(startTime, endTime);
        BigDecimal  orderR17 = dailyStatisticsDAO.queryOrder17(startTime, endTime);
        BigDecimal  orderR18 = dailyStatisticsDAO.queryOrder18(startTime, endTime);
        BigDecimal  orderR19 = dailyStatisticsDAO.queryOrder19(startTime, endTime);
        BigDecimal  orderR20 = dailyStatisticsDAO.queryOrder20(startTime, endTime);
        BigDecimal  orderR21 = dailyStatisticsDAO.queryOrder21(startTime, endTime);
        BigDecimal  orderR22 = dailyStatisticsDAO.queryOrder22(startTime, endTime);
        BigDecimal  orderR23 = dailyStatisticsDAO.queryOrder23(startTime, endTime);
        BigDecimal  orderR24 = dailyStatisticsDAO.queryOrder24(startTime, endTime);
        BigDecimal  orderR25 = dailyStatisticsDAO.queryOrder25(startTime, endTime);

        Map<String,Object> orderRM1 = dailyStatisticsDAO.queryOrder26();
        Map<String,Object> orderRM2 = dailyStatisticsDAO.queryOrder27(startTime);
        BigDecimal orderR28 = dailyStatisticsDAO.queryOrder28();

        XSSFSheet sheet3 = workbook.createSheet("订单统计");
        XSSFRow row3 = sheet3.createRow(0);
        row3.createCell(0).setCellValue(orderR1);
        row3.createCell(1).setCellValue(orderR2.toString());
        row3.createCell(2).setCellValue(orderR3);
        row3.createCell(3).setCellValue(orderR4.toString());
        row3.createCell(4).setCellValue(orderR5);
        row3.createCell(5).setCellValue(orderR6.toString());
        row3.createCell(6).setCellValue(orderR7.toString());
        row3.createCell(7).setCellValue(orderR8.toString());
        row3.createCell(8).setCellValue(orderR9.toString());
        row3.createCell(9).setCellValue(orderR10.toString());
        row3.createCell(10).setCellValue(orderR11.toString());
        row3.createCell(11).setCellValue((orderR12.add(orderR13)).toString());
        row3.createCell(12).setCellValue(orderR14.toString());
        row3.createCell(13).setCellValue(orderR15.toString());
        row3.createCell(14).setCellValue(orderR16.toString());
        row3.createCell(15).setCellValue(orderR17.toString());
        row3.createCell(16  ).setCellValue(orderR18.toString());
        row3.createCell(17).setCellValue(orderR19.toString());
        row3.createCell(18).setCellValue(orderR20.toString());
        row3.createCell(19).setCellValue(orderR21.toString());
        row3.createCell(20).setCellValue(orderR22.toString());
        row3.createCell(21).setCellValue(orderR23.add(orderR24).toString());
        row3.createCell(22).setCellValue(orderR25.toString());

        BigDecimal s1 = (BigDecimal) orderRM1.get("s");
        BigDecimal s2 = (BigDecimal) orderRM2.get("s");
        Long c1 = (Long) orderRM1.get("c");
        Long c2 = (Long) orderRM2.get("c");
        row3.createCell(23).setCellValue(c1.longValue() - c2.longValue());
        row3.createCell(24).setCellValue(s1.subtract(s2).toString());
        row3.createCell(25).setCellValue(orderR28.toString());

        //还款统计
        long repayR1 = dailyStatisticsDAO.queryRepay1(startTime, endTime);
        long repayR2 = dailyStatisticsDAO.queryRepay2(startTime, endTime);
        BigDecimal repayR3 = dailyStatisticsDAO.queryRepay3(startTime, endTime);
        long repayR4 = dailyStatisticsDAO.queryRepay4(startTime, endTime);
        BigDecimal repayR42 = dailyStatisticsDAO.queryRepay42(startTime, endTime);
        long repayR5 = dailyStatisticsDAO.queryRepay5(startTime, endTime);
        long repayR6 = dailyStatisticsDAO.queryRepay6(startTime, endTime);
        long repayR7 = dailyStatisticsDAO.queryRepay7(startTime, endTime);
        long repayR8 = dailyStatisticsDAO.queryRepay8(startTime, endTime);

        XSSFSheet sheet4 = workbook.createSheet("还款统计");
        XSSFRow row4 = sheet4.createRow(0);
        row4.createCell(0).setCellValue(repayR1);
        row4.createCell(1).setCellValue(repayR2);
        row4.createCell(2).setCellValue(repayR3.toString());
        row4.createCell(3).setCellValue(repayR4);
        row4.createCell(4).setCellValue(repayR42.toString());
        row4.createCell(5).setCellValue(repayR5);
        row4.createCell(6).setCellValue(repayR6);
        row4.createCell(7).setCellValue(repayR7);
        row4.createCell(8).setCellValue(repayR8);

        //商户结算统计
        XSSFSheet sheet5 = workbook.createSheet("商户结算");
        XSSFRow row5 = sheet5.createRow(0);

        long merchantSR1 = dailyStatisticsDAO.queryMerchantSettle1(startTime, endTime);
        long merchantSR2 = dailyStatisticsDAO.queryMerchantSettle2(startTime, endTime);
        BigDecimal merchantSR3 = dailyStatisticsDAO.queryMerchantSettle3(startTime, endTime);
        long merchantSR4 = dailyStatisticsDAO.queryMerchantSettle4(startTime, endTime);
        Map<String,Object> merchantSR5 = dailyStatisticsDAO.queryMerchantSettle5();
        Map<String,Object> merchantSR6 =dailyStatisticsDAO.queryMerchantSettle6();

        row5.createCell(0).setCellValue(merchantSR1);
        row5.createCell(1).setCellValue(merchantSR2);
        row5.createCell(2).setCellValue(merchantSR3.toString());
        row5.createCell(3).setCellValue(merchantSR4);
        row5.createCell(4).setCellValue((Long)merchantSR5.get("c"));
        row5.createCell(5).setCellValue(((BigDecimal)merchantSR5.get("s")).toString());
        row5.createCell(6).setCellValue((Long)merchantSR6.get("c"));
        row5.createCell(7).setCellValue(((BigDecimal)merchantSR6.get("s")).toString());

        //导出
        try {
            POIUtils.writeExcel(workbook, filePath , fileName);
        }catch (Exception e){
            log.error("导出每日运营统计出错，e:{}",e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -2);
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();
        calendar.clear();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long endTime = calendar.getTimeInMillis();

        System.out.println(startTime);
        System.out.println(endTime);
    }


    private Map<String, Object> getCHNTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();
        calendar.clear();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long endTime = calendar.getTimeInMillis();

        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(2);

        map.put("startTime", startTime);
        map.put("endTime", endTime);
        return map;
    }


    private Map<String, Object> getAUSTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -2);
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();
        calendar.clear();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long endTime = calendar.getTimeInMillis();

        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(2);

        map.put("startTime", startTime);
        map.put("endTime", endTime);
        return map;
    }


}
