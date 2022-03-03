package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.PosSecretConfigDTO;
import com.uwallet.pay.main.model.entity.PosSecretConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * pos商户秘钥配置表
 * </p>
 *
 * @package: com.fenmi.generator.mapper
 * @description: pos商户秘钥配置表
 * @author: zhangzeyuan
 * @date: Created in 2021-03-24 14:32:28
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface PosSecretConfigDAO extends BaseDAO<PosSecretConfig> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params
     * @return
     */
    List<PosSecretConfigDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 PosSecretConfigDTO
     *
     * @param params
     * @return
     */
    PosSecretConfigDTO selectOneDTO(Map<String, Object> params);

}
