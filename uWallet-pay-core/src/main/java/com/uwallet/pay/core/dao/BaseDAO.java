package com.uwallet.pay.core.dao;

import java.util.List;
import java.util.Map;

/**
 *
 * @author faker
 * @param <T>
 */
public interface BaseDAO<T> {

    /**
     * 插入一条数据。
     *
     * @param t 数据实体
     * @return 影响的数据库行数
     */
    int insert(T t);

    /**
     * 插入多条数据。
     *
     * @param list 等待插入的实体列表
     * @return 影响的数据库行数
     */
    int insertList(List<T> list);

    /**
     * 根据Id更新实体，字段为空的选项会把数据库字段更新为空。
     *
     * @param t 数据实体
     * @return 影响的数据库行数
     */
    int update(T t);

    /**
     * 根据Id更新实体
     *
     * @param params 数据结构 Map<key, Map<key, value>>
     *               key:datas里放需要更新的键值对；
     *               conditions里放where条件筛选条件键值对
     * @return 影响的条数
     */
    int updatex(Map<String, Object> params);

    /**
     * 逻辑删除
     *
     * @param params 筛选条件的键值对
     * @return 影响的条数
     */
    int delete(Map<String, Object> params);

    /**
     * 物理删除
     *
     * @param params 筛选条件的键值对
     * @return 影响的条数
     */
    int pdelete(Map<String, Object> params);

    /**
     * 统计查询条数
     *
     * @param params 筛选条件的键值对
     * @return 统计的条数
     */
    int count(Map<String, Object> params);

    /**
     * 查询符合条件的实体列表（全部字段）, 分页和排序。
     *
     * @param params 筛选条件的键值对 Map<String, Object>
     *               key pc：分页， value类型：PagingContext
     *               scs:排序， value类型：Vector<SortingContext>
     * @return 符合条件的实体列表
     */
    List<T> select(Map<String, Object> params);

    /**
     * 查询符合条件的实体列表（指定字段）, 分页和排序。
     *
     * @param params 筛选条件的键值对 Map<String, Object>
     *               key pc：分页， value类型：PagingContext
     *               scs:排序， value类型：Vector<SortingContext>
     *               columns：指定字段，value类型：字符串数组
     * @return 符合条件的实体列表
     */
    List<Map> selectMap(Map<String, Object> params);

    /**
     * 查询符合条件的第一条数据
     *
     * @param params 筛选条件的键值对
     * @return 符合条件的实体
     */
    T selectOne(Map<String, Object> params);

    /**
     * 根据给定字段以及查询条件进行分组查询，并统计id的count。
     *
     * @param conditions 查询的条件。
     * @return 查询结果。
     */
    List<Map<String, Object>> groupCount(Map<String, Object> conditions);

    /**
     * 根据给定字段查询统计字段的sum结果。
     *
     * @param conditions 查询的条件。
     * @return 返回sum计算的结果值。
     */
    Double sum(Map<String, Object> conditions);

    /**
     * 根据给定字段以及查询条件进行分组查询，并sum统计Field。
     *
     * @param conditions 查询的条件。
     * @return 查询结果。
     */
    List<Map<String, Object>> groupSum(Map<String, Object> conditions);

}
