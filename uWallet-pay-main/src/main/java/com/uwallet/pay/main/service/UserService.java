package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.User;
import com.uwallet.pay.main.model.entity.UserAction;
import com.uwallet.pay.main.model.entity.UserInfoUpdateLog;
import lombok.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Future;

/**
 * <p>
 * 用户
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 用户
 * @author: baixinyue
 * @date: Created in 2019-12-10 17:57:14
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
public interface UserService extends BaseService {

    /**
     * 保存一条 User 数据
     *
     * @param userDTO 待保存的数据
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveUser(UserDTO userDTO, HttpServletRequest request) throws Exception;

    /**
     * 用户注册
     *
     * @param userDTO
     * @param request
     * @throws Exception
     */
    Long doUserRegister(UserDTO userDTO, HttpServletRequest request) throws Exception;

    /**
     * 保存一条 User 数据 v1版本
     *
     * @param userDTO 待保存的数据
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveUserV1(UserDTO userDTO, HttpServletRequest request) throws Exception;

    /**
     * 用户注册 v1版本
     *
     * @param userDTO
     * @param request
     * @throws Exception
     */
    void doUserRegisterV1(UserDTO userDTO, HttpServletRequest request) throws Exception;

    /**
     * 保存多条 User 数据
     *
     * @param userList 待保存的数据列表
     * @throws BizException 保存失败异常
     */
    void saveUserList(List<User> userList, HttpServletRequest request) throws BizException;

    /**
     * 修改一条 User 数据
     *
     * @param id      数据唯一id
     * @param userDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateUser(Long id, UserDTO userDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 user
     *
     * @param dataMap      需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateUserSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 User
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteUser(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 User
     *
     * @param id 数据唯一id
     * @throws BizException 物理删除异常
     */
    void deleteUser(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id查询一条 User
     *
     * @param id 数据唯一id
     * @return 查询到的 User 数据
     */
    UserDTO findUserById(Long id);

    /**
     * 根据条件查询得到第一条 user
     *
     * @param params 查询条件
     * @return 符合条件的一个 user
     */
    UserDTO findOneUser(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<UserDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<UserExcelDTO> findList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params  查询条件
     * @param columns 需要查询的字段信息
     * @param scs     排序信息
     * @param pc      分页信息
     * @return 查询结果的数据集合
     * @throws BizException 查询异常
     */
    List<Map> findMap(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException;

    /**
     * 统计符合条件的数据条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int count(Map<String, Object> params);

    /**
     * 根据给定字段以及查询条件进行分组查询，并统计id的count
     *
     * @param group      分组的字段
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的统计条数
     */
    Map<String, Integer> groupCount(String group, Map<String, Object> conditions);

    /**
     * 根据给定字段查询统计字段的sum结果
     *
     * @param sumField   sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 返回sum计算的结果值
     */
    Double sum(String sumField, Map<String, Object> conditions);

    /**
     * 根据给定字段以及查询条件进行分组查询，并sum统计Field
     *
     * @param group      分组的字段。
     * @param sumField   sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的求和
     */
    Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions);

    /**
     * 创建商户员工
     *
     * @param userDTO
     * @param request
     */
    Long createMerchantStaff(UserDTO userDTO, HttpServletRequest request) throws BizException;

    /**
     * 发送验证码
     *
     * @param email
     * @param request
     * @throws Exception
     */
    void sendSecurityCode(String sendNode, String email, Integer userType, HttpServletRequest request) throws Exception;

    /**
     * @param phone
     * @param request
     * @throws Exception
     */
    void sendSecuritySMS(String phone, String sendNode, Integer userType, HttpServletRequest request) throws Exception;

    /**
     * 忘记密码
     *
     * @param userDTO
     * @param request
     * @throws Exception
     */
    void forgetPassword(UserDTO userDTO, HttpServletRequest request) throws Exception;

    /**
     * APP登陆
     *
     * @param loginData
     * @param request
     * @return
     */
    JSONObject appLogin(JSONObject loginData, HttpServletRequest request) throws Exception;

    /**
     * 验证当前用户 密码是否正确
     *
     * @param pwd
     * @param request
     * @return
     * @throws Exception
     */
    Integer verifyUserPassword(String pwd, HttpServletRequest request) throws Exception;

    /**
     * 绑卡
     *
     * @param cardInfo
     * @param request
     * @throws Exception
     */
    Long tieOnCard(JSONObject cardInfo, HttpServletRequest request) throws Exception;

    /**
     * 获取卡列表信息
     *
     * @param userId
     * @param request
     * @return
     * @throws Exception
     */
    JSONArray getCardList(Long userId, Integer cardType, HttpServletRequest request) throws Exception;

    /**
     * 获取卡列表信息
     *
     * @param userId
     * @param request
     * @return
     * @throws Exception
     */
    List<JSONObject> getCardListEpoch(Long userId, Integer cardType, HttpServletRequest request) throws Exception;

    /**
     * 获取单张卡信息
     *
     * @param id
     * @return
     * @throws Exception
     */
    JSONObject getCardInfo(Long id, HttpServletRequest request) throws Exception;

    /**
     * 修改登陆密码
     *
     * @param userDTO
     * @param request
     * @throws BizException
     */
    void modifyPassword(UserDTO userDTO, HttpServletRequest request) throws BizException;

    /**
     * 支付密码校验
     *
     * @param userDTO
     * @param request
     * @throws BizException
     */
    void checkPayPassword(UserDTO userDTO, HttpServletRequest request) throws BizException;

    /**
     * 设置支付密码
     *
     * @param userDTO
     * @param request
     * @throws BizException
     */
    void updatePayPassword(UserDTO userDTO, HttpServletRequest request) throws BizException;


    /**
     * 理财用户注册
     *
     * @param userInfo
     * @param request
     * @return
     * @throws Exception
     */
    Long investLogin(JSONObject userInfo, HttpServletRequest request) throws Exception;


    /**
     * 查询一个用户一个子户的余额
     *
     * @param id
     * @param accountType
     * @return
     */
    BigDecimal getBalance(@NonNull Long id, Integer accountType) throws Exception;

    /**
     * 记录邮件
     *
     * @param email
     * @param sendMsg
     * @param sendType
     * @param request
     * @throws BizException
     */
    void saveMailLog(String email, String sendMsg, int sendType, HttpServletRequest request) throws BizException;

    /**
     * 查询商户员工
     *
     * @param merchantId
     * @param request
     * @return
     */
    List<JSONObject> findMerchantStaff(Long merchantId, HttpServletRequest request) throws BizException;

    /**
     * 重置商户员工信息, type为0重置密码,1修改电话
     *
     * @param requestInfo
     * @param request
     * @throws BizException
     */
    void resetMerchantStaffInfo(JSONObject requestInfo, HttpServletRequest request) throws BizException;

    /**
     * 定位
     *
     * @param data
     * @param request
     * @throws Exception
     */
    void location(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * @param replaceContentParams
     * @param content
     * @return
     */
    String templateContentReplace(Object[] replaceContentParams, String content);

    /**
     * 向旧手机发送验证码
     *
     * @param phone
     * @param request
     * @throws Exception
     */
    void sendSecuritySMSToOld(String phone, String sendNode, Integer userType, HttpServletRequest request) throws Exception;

    /**
     * 向新手机发送验证码
     *
     * @param phone
     * @param request
     * @throws Exception
     */
    void sendSecuritySMSToNew(String phone, String sendNode, Integer userType, HttpServletRequest request) throws Exception;

    /**
     * 验证旧手机验证码
     *
     * @param request
     * @throws Exception
     */
    void checkOldPhoneCode(String oldPhone, Integer signCode, Integer userType, HttpServletRequest request) throws Exception;

    /**
     * 修改手机号
     *
     * @param phone    新手机号
     * @param signCode
     * @param userType
     * @param request
     * @throws Exception
     */
    String updatePhone(String phone, String oldPhone, Integer signCode, Integer userType, HttpServletRequest request) throws Exception;

    /**
     * 查询用户PINnumber
     *
     * @param id
     * @param request
     * @return
     * @throws
     */
    String queryPinNumber(Long id, HttpServletRequest request) throws BizException;

    /**
     * 修改PINnumber
     *
     * @param id
     * @param request
     * @throws Exception
     */
    void updatePinNumber(Long id, String pinNumber, HttpServletRequest request) throws Exception;

    /**
     * 校验PINnumber
     *
     * @param id
     * @param request
     * @throws Exception
     */
    void checkPinNumber(Long id, String pinNumber, HttpServletRequest request) throws Exception;

    /**
     * 创建商户
     *
     * @param data
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject addMerchant(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 商户店铺选择
     *
     * @param data
     * @param request
     * @return
     * @throws BizException
     */
    JSONObject merchantLogin(JSONObject data, HttpServletRequest request) throws BizException;

    int updateEmail(Map<String, Object> map, HttpServletRequest request);

    /**
     * 风控校验
     *
     * @param data
     * @param request
     * @return
     * @throws Exception
     */
    Future<JSONObject> riskCheck(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * kyc风控
     *
     * @throws Exception
     */
    JSONObject kycRisk(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 获取协议状态
     *
     * @param userId
     * @return
     * @throws Exception
     */
    JSONObject getAgreementState(Long userId) throws Exception;

    /**
     * 更新协议状态
     *
     * @param data
     * @param request
     * @throws Exception
     */
    void updateAgreementState(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * KYC参数校验
     *
     * @param data
     * @param request
     * @return
     */
    void riskCheckParamsCheck(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 卡解绑
     *
     * @param
     * @param request
     * @throws Exception
     */
    JSONObject cardUnbundling(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 账户解绑卡失败在处理
     *
     * @throws Exception
     */
    void cardAccountSystemUnbundlingHandle() throws Exception;

    /**
     * 查询用户基本信息，认证阶段
     *
     * @param userId
     * @return
     * @throws Exception
     */
    JSONObject findOneUserInfo(Long userId) throws Exception;

    /**
     * 用户分期付重新认证
     *
     * @param userId
     * @throws Exception
     */
    void installmentRecertification(Long userId, HttpServletRequest request) throws Exception;

    /**
     * 修改邮箱
     *
     * @param data
     * @param userId
     * @param request
     * @return
     */
    void modifyEmail(JSONObject data, Long userId, HttpServletRequest request) throws Exception;

    /**
     * 绑卡参数校验
     *
     * @param data
     * @param request
     * @throws Exception
     */
    void tieOnCardParamsCheck(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 用户身份验证
     *
     * @param data
     * @param request
     * @throws Exception
     */
    JSONObject userValidation(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 用户注册入参校验
     *
     * @param userInfo
     * @param request
     * @throws BizException
     */
    void userRegisterParamsCheck(UserDTO userInfo, HttpServletRequest request) throws BizException;

    /**
     * 手机校验
     *
     * @param phone
     * @param request
     * @throws BizException
     */
    void phoneParamsCheck(String phone, HttpServletRequest request) throws BizException;

    /**
     * 获取银行logo列表
     *
     * @return
     */
    JSONArray getBankLogoList() throws Exception;

    /**
     * 获取银行logo
     *
     * @param bankName
     * @param type
     * @return
     * @throws Exception
     */
    String getBankLogo(String bankName, Integer type) throws Exception;

    /**
     * 查询KYC结果
     *
     * @param userId
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject getKycResult(String userId, HttpServletRequest request) throws Exception;

    /**
     * 图片上传
     *
     * @param jsonObject
     * @param path
     * @param request
     * @return
     * @throws Exception
     */
    String multiUploadFile(JSONObject jsonObject, String path, HttpServletRequest request) throws Exception;

    /**
     * 获取用户邀请码
     *
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject getInviteCode(HttpServletRequest request) throws Exception;

    /**
     * 红包入账可疑处理
     *
     * @throws Exception
     */
    void walletBookedDoubleHandle() throws Exception;

    /**
     * 红包入账失败处理
     *
     * @throws Exception
     */
    void walletBookedFailedHandle() throws Exception;

    /**
     * 消费成功红包入账、红包出账记录
     *
     * @throws Exception
     */
    void walletBookedConsumption(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 累加推荐人数
     *
     * @param id
     * @return
     */
    int updateRegister(Long id);

    /**
     * 累加预计红包和实得红包
     *
     * @param id
     * @param expectAmount
     * @param actualAmount
     * @return
     */
    int updateWalletGrandTotal(Long id, BigDecimal expectAmount, BigDecimal actualAmount);

    /**
     * 累加消费人数
     *
     * @param id
     * @return
     */
    int updateConsumption(Long id);

    /**
     * 红包入账方法
     *
     * @param amountInUserId
     * @param transType
     * @param markingCode
     * @param request
     * @param flowId
     * @throws Exception
     */
    void walletBooked(Long amountInUserId, Integer transType, String markingCode, HttpServletRequest request, Long flowId) throws Exception;


    /**
     * 首次支付成功时邀请人和被邀请人红包入账
     *
     * @param userId
     * @param inviterId
     * @param transType
     * @param flowId
     * @param request
     * @author zhangzeyuan
     * @date 2021/8/26 11:17
     */
    void firstPaidSuccessAmountIn(Long userId, Long inviterId, Integer transType, Long flowId, HttpServletRequest request) throws Exception;

    void firstPaidSuccessAmountInV2(Long userId, Long inviterId, Integer transType, Long flowId, HttpServletRequest request) throws Exception;

    /**
     * 券码领红包
     *
     * @param requestInfo
     * @param request
     */
    void enterPromotionCode(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 更新卡信息参数校验
     *
     * @param data
     * @param request
     * @throws Exception
     */
    void changeCardMessageParamsCheck(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 卡信息修改
     *
     * @param data
     * @param request
     * @throws Exception
     */
    void changeCardMessage(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 绑定还款账户
     *
     * @param data
     * @param request
     * @throws Exception
     */
    void creditTieOnCard(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 修改用户首次消费标志
     *
     * @param id
     * @return
     * @throws Exception
     */
    int updateFirstDealState(Long id) throws BizException;

    /**
     * 邀请注册用户
     *
     * @param params
     * @return
     */
    int walletFriendsInvitedCount(Map<String, Object> params);

    /**
     * 邀请注册用户列表
     *
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<JSONObject> walletFriendsInvitedList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 邀请注册消费用户
     *
     * @param params
     * @return
     */
    int walletFriendsPurchaseCount(Map<String, Object> params);

    /**
     * 邀请注册消费用户列表
     *
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<JSONObject> walletFriendsPurchaseList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * Lat修改卡信息
     *
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject updateLatpayCardInfo(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 获取银行卡信息
     *
     * @param requestInfo
     * @param request
     * @return
     */
    JSONObject getCardDetails(String requestInfo, HttpServletRequest request) throws BizException;

    /**
     * 登录超时比较方法
     *
     * @param loginMissDTO
     * @param request
     * @throws BizException
     */
    void countFrozenTime(LoginMissDTO loginMissDTO, HttpServletRequest request) throws BizException;

    /**
     * latpay获取卡类型详情
     * 三方接口文档地址: https://docs.qq.com/pdf/DWlptcFVzanlIdEh4
     *
     * @param data
     * @param request
     * @return
     */
    JSONObject latpayGetCardType(JSONObject data, HttpServletRequest request) throws BizException;

    /**
     * 20201-03-03需求, 记录并更新卡失败次数到redis中
     * redis单卡失败次数, redis每天合计失败次数
     *
     * @param userId
     * @param cardId
     * @param isSuccess 卡交易是否成功, true: 成功, false:失败
     * @param request
     * @throws BizException
     */
    void logCardFailedTime(@NonNull Long userId, @NonNull Long cardId, Boolean isSuccess, HttpServletRequest request);

    /**
     * 校验卡失败次数
     * 用户所有卡总的失败次数, 单卡交易失败次数===> 是否超过配置的上限
     *
     * @param userId
     * @param cardId
     * @param request
     * @throws BizException
     */
    void verifyCardFailedTime(@NonNull Long userId, @NonNull Long cardId, HttpServletRequest request) throws BizException;

    /**
     * //todo 2021-03-03 上线完后撤除接口
     * @param request
     * @throws BizException
     */
    //Object initCardTypeInfo(HttpServletRequest request) throws BizException;

    /**
     * 临时接口 用token获取卡详情
     *
     * @param tokenList
     * @param request
     * @return
     */

    Object queryCardInfoByToken(String tokenList, HttpServletRequest request);

    Object queryCardTypeByBin(List<String> binList, HttpServletRequest request);

    /**
     * 将卡设置为默认卡
     *
     * @param data
     * @param request
     */
    void presetCard(JSONObject data,Long userId, HttpServletRequest request) throws Exception;

    /**
     * 风控校验
     *
     * @param data
     * @param request
     * @return
     * @throws Exception
     */
    Future<JSONObject> riskCheckNew(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 在UBiz商户登出后清空pushToken
     *
     * @param userId
     * @param request
     */
    void clearPushToken(Long userId, HttpServletRequest request) throws BizException;

    /**
     * 查询当前用户邀请所获得的红包
     *
     * @param data
     * @param request
     * @return
     */
    JSONObject getReceived(JSONObject data, HttpServletRequest request) throws BizException;

    /**
     * 修改记录状态为已读
     *
     * @param data
     * @param request
     * @return
     */
    JSONObject saveReceivedIsShow(JSONObject data, HttpServletRequest request) throws BizException;

    /**
     * 创建用户
     *
     * @param userDTO
     * @param request
     * @return
     * @throws BizException
     */
    Long userCreate(UserDTO userDTO, HttpServletRequest request) throws BizException;

    /**
     * 创建商户权限
     *
     * @param userId
     * @param request
     * @return
     * @throws BizException
     */
    List<UserAction> createMerchantUserAction(Long userId, HttpServletRequest request) throws BizException;

    /**
     * 批量发送消息
     */
    void batchSendMessage(List<UserDTO> list, MessageBatchSendLogDTO messageBatchSendLogDTO);

    /**
     * 根据用户经纬度修改用户所在州,城市
     *
     * @param param
     * @param request
     */
    void getUserStateCityByLongitude(JSONObject param, HttpServletRequest request);

    /**
     * 用户注册校验验证码是否正确
     *
     * @param param
     * @param request
     * @return
     */
    JSONObject verifyCode(JSONObject param, HttpServletRequest request) throws BizException;


    /**
     * 获取用户信息 新增分期付绑卡状态
     *
     * @param id
     * @return com.uwallet.pay.main.model.dto.UserDTO
     * @author zhangzeyuan
     * @date 2021/6/30 18:21
     */
    UserDTO findUserInfoV2(Long id);

    UserDTO findUserInfoV3(Long id);

    /**
     * h5发送短信验证码
     *
     * @param param
     * @param request
     */
    void sendMessage(JSONObject param, HttpServletRequest request) throws BizException;


    /**
     * @param data    H5登陆
     * @param request
     * @return
     */
    JSONObject h5Login(JSONObject data, HttpServletRequest request) throws Exception;


    /**
     * h5判断用户进行分期付状态
     *
     * @param data
     * @param request
     * @return
     */
    JSONObject verify(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 查询用户信息
     *
     * @param request
     * @return
     */
    JSONObject findUserInfo(HttpServletRequest request) throws Exception;

    /**
     * H5用户注册
     *
     * @param requestInfo
     * @param request
     */
    JSONObject userRegister(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 获取用户分期付额度
     *
     * @param requestInfo
     * @param request
     * @return
     */
    JSONObject userCreditMessage(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 手动触发用户跑风控
     *
     * @param requestInfo
     * @param request
     * @return
     */
    JSONObject activationInstallment(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * @param requestInfo
     * @param request
     * @return
     */
    JSONObject bankStatementsIsResult(JSONObject requestInfo, HttpServletRequest request);


    /**
     * 发送验证码 新
     *
     * @param nodeType
     * @param phoneNumber
     * @param request
     * @author zhangzeyuan
     * @date 2021/8/30 9:36
     */
    void sendSMSVerificationCode(Integer nodeType, String phoneNumber, HttpServletRequest request) throws Exception;


    /**
     * 校验验证码
     *
     * @param request
     * @author zhangzeyuan
     * @date 2021/8/30 10:55
     */
    Object checkVerificationCode(JSONObject jsonObject, HttpServletRequest request) throws Exception;


    /**
     * 用户注册
     *
     * @param jsonObject
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/8/31 19:09
     */
    JSONObject appUserRegisterNew(JSONObject jsonObject, HttpServletRequest request) throws Exception;
    /**
     * 用户注册v2
     *
     * @param jsonObject
     * @param request
     * @return java.lang.Object
     * @author zhoutt
     * @date 2021/11/29 10:20
     */
    JSONObject appUserRegisterNewV2(JSONObject jsonObject, HttpServletRequest request) throws Exception;


    /**
     * 查询用户列表（用户整合）
     *
     * @param param
     * @param request
     * @return
     */
    List<UserListDTO> findUserList(JSONObject param, HttpServletRequest request);

    /**
     * 查询用户列表总条数
     *
     * @param param
     * @return
     */
    int countUserList(JSONObject param);

    /**
     * 获取用户业务状态
     *
     * @param jsonObject
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/2 14:33
     */
    Object getUserBusinessStatus(JSONObject jsonObject, HttpServletRequest request) throws Exception;


    /**
     * 获取用户详情
     *
     * @param userId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/9 14:40
     */
    Object getUserDetailData(Long userId, HttpServletRequest request) throws Exception;


    /**
     * @param userId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/10 16:44
     */
    Object getCardAndAccountList(Long userId, HttpServletRequest request) throws Exception;


    /**
     * 冻结用户
     *
     * @param param
     * @param request
     */
    void frozenUser(JSONObject param, HttpServletRequest request) throws Exception;

    /**
     * 查询用户信息修改记录
     *
     * @param param
     * @return
     */
    int countUserUpdateLog(JSONObject param);

    /**
     * 查询用户信息修改记录
     *
     * @param param
     * @param scs
     * @param pc
     * @return
     */
    List<UserInfoUpdateLogDTO> findUserUpdateLog(JSONObject param, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 修改用户信息
     *
     * @param param
     * @param request
     */
    void updateUserInfo(JSONObject param, HttpServletRequest request) throws Exception;

    /**
     * 获取用户绑卡状态和绑卡时间
     *
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/15 10:57
     */
    Object getUserCardStateAndBindCardDate(Long userId, HttpServletRequest request) throws Exception;


    /**
     * 获取用户欠款列表 按还款时间分组
     *
     * @param requestInfo
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/16 20:10
     */
    Object getUserDetailRepayList(JSONObject requestInfo, HttpServletRequest request) throws Exception;


    /**
     * 获取订单的还款计划
     *
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/17 16:11
     */
    Object getUserDetailRepayListById(Long borrowId, HttpServletRequest request);


    /**
     * 用户详情 - 推荐用户数量
     *
     * @param params
     * @return int
     * @author zhangzeyuan
     * @date 2021/9/13 16:09
     */
    int inviteUserCount(Map<String, Object> params);


    /**
     * 用户详情- 推荐用户列表
     *
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailInviteUserDTO>
     * @author zhangzeyuan
     * @date 2021/9/22 10:39
     */
    List<UserDetailInviteUserDTO> inviteUserList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);


    /**
     * 查询app使用数据
     *
     * @param userId
     * @param request
     * @return
     */
    JSONObject findUserUseAPP(Long userId, HttpServletRequest request) throws Exception;


    /**
     * 用户详情 - 用户使用红包记录数量
     *
     * @param params
     * @return int
     * @author zhangzeyuan
     * @date 2021/9/13 16:09
     */
    int usedPayoMoneyCount(Map<String, Object> params);


    /**
     * 用户详情= 用户使用红包记录列表
     *
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailInviteUserDTO>
     * @author zhangzeyuan
     * @date 2021/9/22 14:06
     */
    List<UserDetailUsedPayoMoneyDTO> usedPayoMoneyList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);


    /**
     * 用户详情 - 用户还款记录数量
     *
     * @param params
     * @return int
     * @author zhangzeyuan
     * @date 2021/9/22 16:52
     */
    int repaymentHistoryCount(Map<String, Object> params);


    /**
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailUsedPayoMoneyDTO>
     * @author zhangzeyuan
     * @date 2021/9/22 16:57
     */
    List<UserDetailRepaymentHistoryDTO> repaymentHistoryList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);


    /**
     * 用户详情 - 用户还款记录详情数量
     *
     * @param params
     * @return int
     * @author zhangzeyuan
     * @date 2021/9/22 16:52
     */
    int repaymentHistoryDetailCount(Map<String, Object> params);


    /**
     * 用户详情 - 用户还款记录详情列表
     *
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailUsedPayoMoneyDTO>
     * @author zhangzeyuan
     * @date 2021/9/22 16:57
     */
    List<UserDetailRepaymentHistoryDetailDTO> repaymentHistoryDetailList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 获取用户 未还欠款 还款日 数量
     *
     * @param params
     * @return int
     * @author zhangzeyuan
     * @date 2021/9/23 10:49
     */
    int userDetailRepayDateCount(Map<String, Object> params);


    /**
     * 用户详情 - 分期付未还欠款列表
     *
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailUsedPayoMoneyDTO>
     * @author zhangzeyuan
     * @date 2021/9/22 16:57
     */
    List<UserDetailRepaymentDTO> userDetailRepayListGroupByDate(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 新用户 8am 邮件发送
     *
     * @author zhangzeyuan
     * @date 2021/9/24 15:42
     */
    void newUser8amEmailScheduled();

    /**
     * 新用户 4am 邮件发送
     *
     * @author zhangzeyuan
     * @date 2021/9/26 14:12
     */
    void newUser4pmEmailScheduled();

    /**
     * 新用户 1pm 邮件发送
     *
     * @author zhangzeyuan
     * @date 2021/9/26 14:12
     */
    void newUser1pmEmailScheduled();


    /**
     * 发送新用户首次交易2小时后邮件
     *
     * @param userId
     * @author zhangzeyuan
     * @date 2021/9/26 15:43
     */
    void sendNewUserFirstPayAfter2HoursEmail(Long userId);


    /**
     * 获取用户协议状态
     *
     * @param userId
     * @author zhangzeyuan
     * @date 2021/9/28 15:17
     */
    Object getUserAgreementState(Long userId, HttpServletRequest request) throws Exception;


    /**
     * 更新用户首页协议状态
     *
     * @param userId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/28 15:33
     */
    void updateUserAgreementState(Long userId, HttpServletRequest request) throws Exception;


    /**
     * 更新用户分期付卡还款协议状态
     *
     * @param userId
     * @param request
     * @author zhangzeyuan
     * @date 2021/9/28 16:14
     */
    void updateUserCreditCardAgreementState(Long userId, HttpServletRequest request) throws Exception;


    /**
     * 获取用户默认卡信息
     *
     * @param userId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/29 14:14
     */
    JSONObject getDefaultCardInfo(Long userId, HttpServletRequest request) throws Exception;


    /**
     * 获取APP首页弹窗提醒
     *
     * @param userId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/29 16:57
     */
    Object getAppHomePageReminder(Long userId, HttpServletRequest request) throws Exception;


    /**
     * 获取用户营销账户卡券列表
     *
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/10/28 9:54
     */
    JSONObject getMarketingCouponAccount(JSONObject params, HttpServletRequest request) throws Exception;

    /**
     * 激活卡券
     *
     * @param params
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/11/9 20:33
     */
    JSONObject activateMarketing(JSONObject params, HttpServletRequest request) throws Exception;












   /**
    * 协助注册用户
    * @param param
    * @param request
    */
    void AssistRegistrationUser(JSONObject param, HttpServletRequest request) throws Exception;

   /**
    * 提升/降低用户授信额度
    * @param param
    * @param request
    */
   void updateUserAmount(JSONObject param, HttpServletRequest request) throws Exception;

  /**
   * 修改用户修改信息备注
   * @param param
   * @param request
   */
   void updateUserInfoRemarks(JSONObject param, HttpServletRequest request) throws BizException;

   /**
    * 协助Kyc
    * @param param
    * @param request
    */
   JSONObject assistKyc(JSONObject param, HttpServletRequest request) throws Exception;

   /**
    * 延迟还款
    * @param param
    * @param request
    */
   void latePayment(JSONObject param, HttpServletRequest request) throws Exception;

   /**
    * 后台KYC校验方法
    * @param param
    * @param request
    */
   void riskCheckParamsCheckBySystem(JSONObject param, HttpServletRequest request) throws BizException;

    /**
     * 查询权限表
     * @param data
     * @param request
     * @return
     */
    JSONObject getUserDetail(JSONObject data, HttpServletRequest request);


   /**
    * 查询kyc记录条数
    * @param params
    * @return
    */
    int findKycLogListCount(Map<String, Object> params);

  /**
   * 查询kyc记录信息
   * @param params
   * @param scs
   * @param pc
   * @return
   */
  List<UserStepLogDTO> findKycLogList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);


    /**
     * 查询stripe弹窗标识
     * @param request
     * @return
     */
    Integer getStripeState(HttpServletRequest request);
    /**
     * 卡券列表数量
     * @param params
     * @return
     */
    int userPromotionCount(Map<String, Object> params);

    /**
     * 卡券列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    List<UserPromotionDTO> userPromotionList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);
}
