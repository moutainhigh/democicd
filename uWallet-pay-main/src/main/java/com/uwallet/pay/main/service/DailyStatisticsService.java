package com.uwallet.pay.main.service;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.UserMonthlyDataDTO;
import com.uwallet.pay.main.model.entity.UserMonthlyData;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 每日统计
 * @author zhangzeyuan
 * @date 2021/12/14 10:23
 * @return null
 */
public interface DailyStatisticsService extends BaseService {


    void dailyStatistics();

}
