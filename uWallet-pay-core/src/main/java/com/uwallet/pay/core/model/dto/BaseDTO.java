package com.uwallet.pay.core.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import lombok.Data;

/**
 *
 * @author faker
 */
@Data
public class BaseDTO {

    /**
     * 数据库表的主键, 生成策略基于雪花算法
     * 获取示例：<code>AppContext.IdGen.nextId()</>
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long id;

    /**
     * 创建人ID，对应 用户表的id.
     * 获取示例：<code>BaseController#getUserId(request)</code>
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long createdBy;

    /**
     * 创建时间，存储时间戳。
     * 获取示例：<code>System.currentTimeMillis()</code>
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private Long createdDate;

    /**
     * 最后修改人ID，对应t_user表的id
     * 获取示例：<code>BaseController#getUserId(request)</code>
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long modifiedBy;

    /**
     * 最后修改时间，存储时间戳。
     * 获取示例：<code>System.currentTimeMillis()</code>
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private Long modifiedDate;

    /**
     * 此记录的状态，例如逻辑删除
     * 示例：1：正常：0：删除 等等
     */
    private Integer status;

    /**
     * 数据最后编辑人的ip地址。
     */
    private String ip;


}
