package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.MarketingManagement;
import com.uwallet.pay.main.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 用户
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 用户
 * @author: baixinyue
 * @date: Created in 2019-12-10 17:57:14
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface UserDAO extends BaseDAO<User> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<UserDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<UserExcelDTO> findList(Map<String, Object> params);

    /**
     * 根据id查询一条 UserDTO
     * @param params
     * @return
     */
    UserDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询用户id
     * @param params
     * @return
     */
    List<Long> selectUserIds(Map<String, Object> params);

    int updateEmail(Map<String, Object> params);
    /**
     * 更新手机号
     * @param params
     * @return
     */
    int updatePhone(Map<String, Object> params);

    /**
     * 查询用户PINnumber
     * @param id
     * @return
     */
    String queryPinNumber(@Param("id") Long id);

    /**
     * 修改pinNumber
     * @param id
     * @param pinNumber
     * @return
     */
    int updatePinNumber(@Param("id") Long id, @Param("pinNumber") String pinNumber, @Param("modifyUserId") Long modifyUserId, @Param("modifyDate") Long modifyDate);

    /**
     * 更新消费人数
     * @param id
     * @return
     */
    int updateRegister(@Param("id") Long id);

    /**
     * 更新消费人数
     * @param id
     * @return
     */
    int updateConsumption(@Param("id") Long id);

    /**
     * 更新红包累计
     * @param id
     * @return
     */
    int updateWalletGrandTotal(@Param("id") Long id, @Param("expectAmount")BigDecimal expectAmount, @Param("actualAmount")BigDecimal actualAmount);

    /**
     * 邀请注册用户
     * @param params
     * @return
     */
    int walletFriendsInvitedCount(Map<String, Object> params);

    /**
     * 邀请注册用户列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<UserDTO> walletFriendsInvitedList(Map<String, Object> params);

    /**
     * 邀请注册消费用户
     * @param params
     * @return
     */
    int walletFriendsPurchaseCount(Map<String, Object> params);

    /**
     * 邀请注册消费用户列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<JSONObject> walletFriendsPurchaseList(Map<String, Object> params);

    /**
     * 更新用户首次消费标志
     * @param id
     * @return
     */
    int updateFirstDealState(@Param("id")Long id);

    /**
     * 根据用户id查询当前用户邀请人信息
     * @param params
     * @return
     */
    List<JSONObject> getReceived(Map<String, Object> params);

    /**
     * 获取分期付用户信息, 卡数量
     * @param userId
     * @return
     */
    JSONObject verifyCardState(@Param("userId") Long userId);

    int countCard(@Param("userId")Long userId);





    /**
     * 更新用户邀请人
     * @author zhangzeyuan
     * @date 2021/8/26 10:34
     * @param userId
     * @param inviteId
     * @return int
     */
    int updateInviteId(@Param("userId")Long userId, @Param("inviteId")Long inviteId);

    /**
     * 查询用户列表
     * @param param
     * @return
     */
    List<UserListDTO> findUserList(JSONObject param);

    /**
     * 查询用户列表
     * @param param
     * @return
     */
    List<Long> countUserList(JSONObject param);


    /**
     * 获取用户详情还款计划列表
     * @author zhangzeyuan
     * @date 2021/9/17 17:43
     * @param borrowId
     * @return java.lang.Object
     */
    List<UserDetailsRepayListDTO> getUserDetailRepayListById(@Param("borrowId")Long borrowId);


    /**
     * 用户详情 - 推荐用户数量
     * @author zhangzeyuan
     * @date 2021/9/13 16:09
     * @param params
     * @return int
     */
    List<UserDTO> inviteUserCount(Map<String, Object> params);


    /**
     * 用户详情- 推荐用户列表
     * @author zhangzeyuan
     * @date 2021/9/22 10:38
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailInviteUserDTO>
     */
    List<UserDetailInviteUserDTO> inviteUserList(Map<String, Object> params);



    /**
     * 用户详情 - 用户使用红包记录数量
     * @author zhangzeyuan
     * @date 2021/9/13 16:09
     * @param params
     * @return int
     */
    int usedPayoMoneyCount(Map<String, Object> params);


    /**
     * 用户详情= 用户使用红包记录列表
     * @author zhangzeyuan
     * @date 2021/9/22 14:06
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailInviteUserDTO>
     */
    List<UserDetailUsedPayoMoneyDTO> usedPayoMoneyList(Map<String, Object> params);



    /**
     * 用户详情 - 用户还款记录数量
     * @author zhangzeyuan
     * @date 2021/9/22 16:52
     * @param params
     * @return int
     */
    List<UserDetailRepaymentHistoryDTO> repaymentHistoryCount(Map<String, Object> params);



    /**
     *
     * @author zhangzeyuan
     * @date 2021/9/22 16:53
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailUsedPayoMoneyDTO>
     */
    List<UserDetailRepaymentHistoryDTO> repaymentHistoryList(Map<String, Object> params);


    /**
     * 用户详情 - 用户还款记录详情数量
     * @author zhangzeyuan
     * @date 2021/9/22 16:52
     * @param params
     * @return int
     */
    List<Long> repaymentHistoryDetailCount(Map<String, Object> params);



    /**
     * 用户详情 - 用户还款记录详情列表
     * @author zhangzeyuan
     * @date 2021/9/22 16:57
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailUsedPayoMoneyDTO>
     */
    List<UserDetailRepaymentHistoryDetailDTO> repaymentHistoryDetailList(Map<String, Object> params);



    /**
     * 获取用户 未还欠款 还款日 数量
     * @author zhangzeyuan
     * @date 2021/9/23 10:49
     * @param params
     * @return int
     */
    int userDetailRepayDateCount(Map<String, Object> params);


    /**
     * 获取用户 未还欠款 还款日 列表
     * @author zhangzeyuan
     * @date 2021/9/23 11:03
     * @param params
     * @return java.util.List<java.lang.Long>
     */
    List<Long> userDetailRepayDateList(Map<String, Object> params);



    /**
     *
     * @author zhangzeyuan
     * @date 2021/9/23 14:08
     * @param userId
     * @param expectRepayTime
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailRepaymentHistoryDetailDTO>
     */
    List<UserDetailRepaymentDetailDTO> userRepaymentDetailByDate(@Param("userId")String userId, @Param("expectRepayTime")Long expectRepayTime);


    /**
     * 获取发送邮件用户信息
     * @author zhangzeyuan
     * @date 2021/9/24 16:13
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDTO>
     */
    List<UserDTO> userListBySendMail(Map<String, Object> params);


    /**
     * 更新用户首页阅读协议状态
     * @author zhangzeyuan
     * @date 2021/9/28 15:35
     * @param id
     * @return int
     */
    int updateUserAgreementState(@Param("id")Long id);


    /**
     * 更新用户分期付卡还款协议状态
     * @author zhangzeyuan
     * @date 2021/9/28 16:16
     * @param id
     * @return int
     */
    int updateUserCreditCardAgreementState(@Param("id")Long id);


    //todo  待删除
    List<UserDTO> balanceGreaterThan0UserList(@Param("id")Long id);
    List<UserDTO> getAllNotSplitUser();
    int updateBalance(@Param("id")Long id);
    int updateSplitRedState(@Param("id")Long id);
    int updateUserSplitState(@Param("id")Long id, @Param("state")Integer state , @Param("orgState")Integer orgState);
    List<UserDTO> sendEmailToAllUser(Map<String, Object> params);
    List<MarketingManagementDTO> queryAllMarketingManagement();
    int countMarketingReceiveNumberByCode(@Param("code")String code);
    int updateReceiverNumber(@Param("id")Long id , @Param("number")Integer number);

    /**
     * 查询用户app版本信息
     * @param userId
     * @return
     */
    UserDTO findOneUserAppInfoById(Long userId);

    /**
     * 查询逾期订单信息
     * @param userId
     * @return
     */
    List<JSONObject> findUserOverdueOrders(Long userId);
}
