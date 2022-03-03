package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.AccountFlowDAO;
import com.uwallet.pay.main.model.dto.AccountFlowDTO;
import com.uwallet.pay.main.model.entity.AccountFlow;
import com.uwallet.pay.main.service.AccountFlowService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 账户动账交易流水表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 账户动账交易流水表
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:49:00
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class AccountFlowServiceImpl extends BaseServiceImpl implements AccountFlowService {

    @Autowired
    private AccountFlowDAO accountFlowDAO;

    @Override
    public Long saveAccountFlow(@NonNull AccountFlowDTO accountFlowDTO, HttpServletRequest request) throws BizException {
        AccountFlow accountFlow = BeanUtil.copyProperties(accountFlowDTO, new AccountFlow());
        log.info("save AccountFlow:{}", accountFlow);
        if (request != null) {
            accountFlow = (AccountFlow) this.packAddBaseProps(accountFlow, request);
        } else {
            long now = System.currentTimeMillis();
            accountFlow.setId(SnowflakeUtil.generateId());
            accountFlow.setCreatedDate(now);
            accountFlow.setModifiedDate(now);
            accountFlow.setStatus(1);
        }
        if (accountFlowDAO.insert(accountFlow) != 1) {
            log.error("insert error, data:{}", accountFlow);
            throw new BizException("Insert accountFlow Error!");
        }
        return accountFlow.getId();
    }

    @Override
    public void saveAccountFlowList(@NonNull List<AccountFlow> accountFlowList, HttpServletRequest request) throws BizException {
        if (accountFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = accountFlowDAO.insertList(accountFlowList);
        if (rows != accountFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, accountFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateAccountFlow(@NonNull Long id, @NonNull AccountFlowDTO accountFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update accountFlowDTO:{}", accountFlowDTO);
        AccountFlow accountFlow = BeanUtil.copyProperties(accountFlowDTO, new AccountFlow());
        accountFlow.setId(id);
        if (request != null) {
            accountFlow = (AccountFlow) this.packModifyBaseProps(accountFlow, request);
        } else {
            accountFlow.setModifiedDate(System.currentTimeMillis());
        }
        int cnt = accountFlowDAO.update(accountFlow);
        if (cnt != 1) {
            log.error("update error, data:{}", accountFlowDTO);
            throw new BizException("update accountFlow Error!");
        }
    }

    @Override
    public void updateAccountFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        accountFlowDAO.updatex(params);
    }

    @Override
    public void logicDeleteAccountFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = accountFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteAccountFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = accountFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public AccountFlowDTO findAccountFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AccountFlowDTO accountFlowDTO = accountFlowDAO.selectOneDTO(params);
        return accountFlowDTO;
    }

    @Override
    public AccountFlowDTO findOneAccountFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        AccountFlow accountFlow = accountFlowDAO.selectOne(params);
        AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
        if (null != accountFlow) {
            BeanUtils.copyProperties(accountFlow, accountFlowDTO);
        }
        return accountFlowDTO;
    }

    @Override
    public List<AccountFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AccountFlowDTO> resultList = accountFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return accountFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return accountFlowDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = accountFlowDAO.groupCount(conditions);
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Map<String, Object> m : maps) {
            String key = m.get("group") != null ? m.get("group").toString() : "group";
            Object value = m.get("count");
            int count = 0;
            if (StringUtils.isNotBlank(value.toString())) {
                count = Integer.parseInt(value.toString());
            }
            map.put(key, count);
        }
        return map;
    }

    @Override
    public Double sum(String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("sumfield", sumField);
        return accountFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = accountFlowDAO.groupSum(conditions);
        Map<String, Double> map = new LinkedHashMap<>();
        for (Map<String, Object> m : maps) {
            String key = m.get("group") != null ? m.get("group").toString() : "group";
            Object value = m.get("sum");
            double sum = 0d;
            if (StringUtils.isNotBlank(value.toString())) {
                sum = Double.parseDouble(value.toString());
            }
            map.put(key, sum);
        }
        return map;
    }

    @Override
    public AccountFlowDTO selectLatestByFlowId(Long id, Integer transType) {
        return accountFlowDAO.selectLatestByFlowId(id,transType);
    }

    @Override
    public void updateAccountFlowForConcurrency(@NonNull Long id, @NonNull AccountFlowDTO accountFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full updateForConcurrency accountFlowDTO:{}", accountFlowDTO);
        AccountFlow accountFlow = BeanUtil.copyProperties(accountFlowDTO, new AccountFlow());
        accountFlow.setId(id);
        if (request != null) {
            accountFlow = (AccountFlow) this.packModifyBaseProps(accountFlow, request);
        } else {
            accountFlow.setModifiedDate(System.currentTimeMillis());
        }
        int cnt = accountFlowDAO.updateForConcurrency(accountFlow);
        if (cnt != 1) {
            log.error("update error, data:{}", accountFlowDTO);
            throw new BizException("update accountFlow Error!");
        }
    }

    @Override
    public List<AccountFlowDTO> selectAccountRollbackDoubtFlow() {
        return accountFlowDAO.selectAccountRollbackDoubtFlow();
    }

    @Override
    public List<AccountFlowDTO> selectAccountOutDoubtFlow() {
        return accountFlowDAO.selectAccountOutDoubtFlow();
    }

    @Override
    public List<AccountFlowDTO> selectWalletBooked(Map<String, Object> params) {
        return accountFlowDAO.selectWalletBooked(params);
    }

    @Override
    public List<JSONObject> selectWalletTransaction(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<String> transactionDates = accountFlowDAO.selectWalletTransaction(params);
        List<JSONObject> transactions = new ArrayList<>(transactionDates.size());
        Long userId = Long.valueOf(params.get("userId").toString());
        params.clear();
        Map<String, Object> finalParams = params;
        transactionDates.stream().forEach(date -> {
            finalParams.put("userId", userId);
            finalParams.put("transactionDate", date);
            finalParams.put("transType", StaticDataEnum.STATUS_0.getCode());
            List<AccountFlowDTO> income = accountFlowDAO.selectWalletTransactionDetail(finalParams);
            BigDecimal incomeSum = accountFlowDAO.selectWalletIncomeAndExpend(finalParams);
            finalParams.put("transType", StaticDataEnum.STATUS_1.getCode());
            List<AccountFlowDTO> expend = accountFlowDAO.selectWalletTransactionDetail(finalParams);
            BigDecimal expendSum = accountFlowDAO.selectWalletIncomeAndExpend(finalParams);
            // 合并集合
            income.addAll(expend);
            List<AccountFlowDTO> allTransactions = income.stream().sorted(Comparator.comparing(AccountFlowDTO::getCreatedDate).reversed()).collect(Collectors.toList());
            JSONObject transaction = new JSONObject();
            transaction.put("date", date);
            transaction.put("income", incomeSum);
            transaction.put("expend", expendSum);
            transaction.put("transactionDetails", allTransactions);
            transactions.add(transaction);
        });
        return transactions;
    }

    @Override
    public int selectWalletTransactionCount(Map<String, Object> params) {
        return accountFlowDAO.selectWalletTransactionCount(params);
    }

    @Override
    public int updateAccountFlowByOrderNo(Long orderNo, AccountFlowDTO accountFlowDTO, HttpServletRequest request) {
        accountFlowDTO.setOrderNo(orderNo);
        accountFlowDTO.setModifiedDate(System.currentTimeMillis());
        if (request != null) {
            Long currentLoginId = getUserId(request);
            accountFlowDTO.setModifiedBy(currentLoginId);
        }
        int i = accountFlowDAO.updateAccountFlowByOrderNo(accountFlowDTO);
        return i;
    }

    @Override
    public List<AccountFlowDTO> getQrPayBatAmtOutRollbackDoubtFlow() {
        return accountFlowDAO.getQrPayBatAmtOutRollbackDoubtFlow();
    }

    @Override
    public List<AccountFlowDTO> getQrPayBatAmtOutRollbackFailFlow() {
        return accountFlowDAO.getQrPayBatAmtOutRollbackFailFlow();
    }

    @Override
    public List<AccountFlowDTO> getBatchAmountOutDoubtFlow() {
        return accountFlowDAO.getBatchAmountOutDoubtFlow();
    }
}
