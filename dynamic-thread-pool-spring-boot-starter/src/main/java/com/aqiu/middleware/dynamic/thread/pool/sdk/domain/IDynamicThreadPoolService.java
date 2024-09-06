package com.aqiu.middleware.dynamic.thread.pool.sdk.domain;

import com.aqiu.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态线程池服务
 */
public interface IDynamicThreadPoolService {
    List<ThreadPoolConfigEntity> queryThreadPoolList();

    ThreadPoolConfigEntity queryThreadPoolConfigByName(String threadPoolName);

    void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);
}
