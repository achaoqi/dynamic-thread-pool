package com.aqiu.middleware.dynamic.thread.pool.sdk.domain;

import com.aqiu.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class DynamicThreadPoolService implements IDynamicThreadPoolService{

    private final Logger logger= LoggerFactory.getLogger(DynamicThreadPoolService.class);

    private final String applicationName;
    private final String ip;
    private final String port;

    private final Map<String,ThreadPoolExecutor> threadPoolExecutorMap;

    public DynamicThreadPoolService(String applicationName,Map<String, ThreadPoolExecutor> threadPoolExecutorMap,String ip, String port) {
        this.applicationName = applicationName;
        this.ip = ip;
        this.port = port;
        this.threadPoolExecutorMap = threadPoolExecutorMap;
    }

    @Override
    public List<ThreadPoolConfigEntity> queryThreadPoolList() {
        List<ThreadPoolConfigEntity> threadPoolVOS=new ArrayList<>(threadPoolExecutorMap.size());
        for (Map.Entry<String, ThreadPoolExecutor> threadPoolEntity : threadPoolExecutorMap.entrySet()) {
            String key = threadPoolEntity.getKey();
            ThreadPoolExecutor value = threadPoolEntity.getValue();
            ThreadPoolConfigEntity config = threadPoolExecutorToEntity(key, value);
            threadPoolVOS.add(config);
        }
        return threadPoolVOS;
    }

    @Override
    public ThreadPoolConfigEntity queryThreadPoolConfigByName(String threadPoolName) {
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolName);
        if (threadPoolExecutor==null){
            ThreadPoolConfigEntity config = new ThreadPoolConfigEntity();
            config.setAppName(applicationName);
            config.setThreadPoolName(threadPoolName);
            return config;
        }
        return threadPoolExecutorToEntity(threadPoolName,threadPoolExecutor);
    }

    @Override
    public void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity) {
        if (threadPoolConfigEntity==null || !applicationName.equals(threadPoolConfigEntity.getAppName()) || !ip.equals(threadPoolConfigEntity.getIp())||!port.equals(threadPoolConfigEntity.getPort())) return;
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolConfigEntity.getThreadPoolName());
        if (threadPoolExecutor==null) return;

//        更新参数
        threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
        threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
    }

    private ThreadPoolConfigEntity threadPoolExecutorToEntity(String threadPoolName,ThreadPoolExecutor threadPoolExecutor){
        ThreadPoolConfigEntity config = new ThreadPoolConfigEntity();
        config.setAppName(applicationName);
        config.setIp(ip);
        config.setPort(port);
        config.setThreadPoolName(threadPoolName);
        config.setCorePoolSize(threadPoolExecutor.getCorePoolSize());
        config.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize());
        config.setActiveCount(threadPoolExecutor.getActiveCount());
        config.setPoolSize(threadPoolExecutor.getPoolSize());
        config.setQueueType(threadPoolExecutor.getQueue().getClass().getSimpleName());
        config.setQueueSize(threadPoolExecutor.getQueue().size());
        config.setRemainingCapacity(threadPoolExecutor.getQueue().remainingCapacity());
        return config;
    }
}
