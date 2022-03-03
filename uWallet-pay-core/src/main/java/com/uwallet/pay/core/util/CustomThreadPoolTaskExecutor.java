package com.uwallet.pay.core.util;

import com.uwallet.pay.core.config.CustomThreadPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;

/**
 * @description: Spring对java.util.concurrent.ThreadPoolExecutor进行过封装
 * @author: Rainc
 * @date: Created in 2019-05-15 13:33:58
 * @version: V1.0
 */
@Configuration
public class CustomThreadPoolTaskExecutor {

    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：线程池创建时初始化的线程数
        executor.setCorePoolSize(CustomThreadPoolConfig.CORE_POOL_SIZE);
        // 最大线程数：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(CustomThreadPoolConfig.MAX_POOL_SIZE);
        // 缓冲队列：用来缓冲执行任务的队列
        executor.setQueueCapacity(CustomThreadPoolConfig.LINKED_BLOCKING_QUEUE_SIZE);
        // 允许线程的空闲时间：超过了核心线程数之外的线程，在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(CustomThreadPoolConfig.KEEP_ALIVE_SECONDS);
        // 线程池对拒绝任务的处理策略
        executor.setRejectedExecutionHandler(new CustomRejectedExecutionHandler());
        // 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setThreadNamePrefix("taskExecutor-");
        // 设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 设置线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住
        executor.setAwaitTerminationSeconds(CustomThreadPoolConfig.AWAIT_TERMINATION_SECONDS);
        return executor;
    }

    /**
     * 自定义拒绝策略:保证任务不会被丢失
     */
    private class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, java.util.concurrent.ThreadPoolExecutor executor) {
            try {
                // 核心改造点，由BlockingQueue的offer改成put阻塞方法
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}