package com.uwallet.pay.core.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 *
 * @author faker
 * @param <T>
 */
@Data
@AllArgsConstructor
public class PageResult<T> {
    /**
     * 总条数
     */
    private Long total;

    /**
     * 页码
     */
    private int pageNumber;

    /**
     * 每页结果数
     */
    private int pageSize;

    /**
     * 结果集
     */
    private List<T> list;
}
