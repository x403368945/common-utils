package com.utils.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * 线程池管理
 *
 * @author Jason Xie  2016-11-23
 */
@Slf4j
public final class ThreadPoolManager {

    private static ExecutorService executorService = null;
    private static ExecutorService singleService = null;

    private ThreadPoolManager() {
//		(1) newCachedThreadPool
//		创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。
//		线程池为无限大，当执行第二个任务时第一个任务已经完成，会复用执行第一个任务的线程，而不用每次新建线程。
//		(2) newFixedThreadPool
//		创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待。
//		定长线程池的大小最好根据系统资源进行设置。如Runtime.getRuntime().availableProcessors()
//		(3)  newScheduledThreadPool
//		创建一个定长线程池，支持定时及周期性任务执行。
//		(4) newSingleThreadExecutor
//		创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。
        executorService = Executors.newFixedThreadPool(20);
        singleService = Executors.newSingleThreadExecutor();
    }

    private static volatile ThreadPoolManager instance = null;

    public static ThreadPoolManager getInstance() {
        if (Objects.isNull(instance)) {
            synchronized (ThreadPoolManager.class) {
                if (Objects.isNull(instance)) {
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }


    /**
     * 启动线程，执行run方法
     *
     * @param runnable 线程方法
     */
    public void execute(final Runnable runnable) {
        executorService.execute(runnable);
        if (log.isDebugEnabled()) {
            log.debug("当前活动的线程数量：" + ((ThreadPoolExecutor) executorService).getActiveCount());
            log.debug("当前任务数量：" + ((ThreadPoolExecutor) executorService).getTaskCount());
            log.debug("线程总数：" + ((ThreadPoolExecutor) executorService).getPoolSize());
        }
    }

    /**
     * 启动单线程，执行run方法
     *
     * @param runnable 线程方法
     */
    public void single(final Runnable runnable) {
        singleService.execute(runnable);
        if (log.isDebugEnabled()) {
            log.debug("当前任务数量：" + ((ThreadPoolExecutor) executorService).getTaskCount());
            log.debug("线程总数：" + ((ThreadPoolExecutor) executorService).getPoolSize());
        }
    }

    /**
     * 执行任务，等待返回结果
     *
     * @param callable {@link Callable}
     * @return Future
     */
    public <T> Future<T> submit(final Callable<T> callable) {
        return executorService.submit(callable);
    }

    /**
     * 释放线程
     */
    public void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("当前活动的线程数量：" + ((ThreadPoolExecutor) executorService).getActiveCount());
            log.debug("当前任务数量：" + ((ThreadPoolExecutor) executorService).getTaskCount());
            log.debug("线程总数：" + ((ThreadPoolExecutor) executorService).getPoolSize());
        }
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (!singleService.isShutdown()) {
            singleService.shutdown();
        }
        if (log.isDebugEnabled()) {
            log.debug("已停止：" + executorService.isTerminated());
            log.debug("已关闭：" + executorService.isShutdown());
            log.debug("已停止：" + singleService.isTerminated());
            log.debug("已关闭：" + singleService.isShutdown());
        }
    }

}
