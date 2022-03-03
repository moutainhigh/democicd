package com.uwallet.pay.core.config;

/**
 * @description: 线程池配置类
 * @author: Rainc
 * @date: Created in 2019-05-15 13:33:58
 * @version: V1.0
 */
public class CustomThreadPoolConfig {

    /**
     * 每个线程处理数据数量
     */
    public static final int TREATMENT_NUMBER = 100_000;

    /**
     * 核心线程池大小
     */
    public static final int CORE_POOL_SIZE = 8;

    /**
     * 最大线程池大小
     */
    public static final int MAX_POOL_SIZE = 20;

    /**
     *基于链表的先进先出队列大小
     */
    public static final int LINKED_BLOCKING_QUEUE_SIZE = 100;

    /**
     * 线程池中超过核心线程池大小数目的空闲线程最大存活时间(单位为秒)
     */
    public static final int KEEP_ALIVE_SECONDS = 60;

    /**
     * 线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁(单位为秒)
     */

    public static final int AWAIT_TERMINATION_SECONDS = 60;

    /**
     * 分批数据操作量（每批操作的数量）
     */
    public static final int GROUP_TEN_THOUSAND_NUM = 20000;
    /**
     * 分批发送消息操作量（每批操作的数量）
     */
    public static final int GROUP_TEN_THOUSAND_NUM_NEW = 1000;
    /**
     * 核心线程池大小
     */
    public static final int BATCH_CORE_POOL_SIZE = 6;
    /**
     * 最大线程池大小
     */
    public static final int BATCH_MAX_POOL_SIZE = 10;


}
