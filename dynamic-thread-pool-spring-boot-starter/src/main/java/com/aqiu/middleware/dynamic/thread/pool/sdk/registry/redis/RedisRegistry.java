package com.aqiu.middleware.dynamic.thread.pool.sdk.registry.redis;

import com.aqiu.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import com.aqiu.middleware.dynamic.thread.pool.sdk.domain.valueobj.RegistryEnumVO;
import com.aqiu.middleware.dynamic.thread.pool.sdk.registry.IRegistry;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.List;

/**
 * redis注册中心
 */
public class RedisRegistry implements IRegistry {

    private final RedissonClient redissonClient;

    private final String ip;
    private final String port;

    public RedisRegistry(RedissonClient redissonClient,String ip,String port) {
        this.redissonClient = redissonClient;
        this.ip=ip;
        this.port=port;
    }

    @Override
    public void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolConfigEntities) {
        RList<ThreadPoolConfigEntity> list = redissonClient.getList(RegistryEnumVO.THREAD_POOL_CONFIG_LIST_KEY.getKey()+"_"+ip+":_"+port);
        list.clear();
        list.addAll(threadPoolConfigEntities);
    }

    @Override
    public void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity) {
        String cacheKey = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + threadPoolConfigEntity.getAppName() + "_"+ ip + ":" +port + "_" + threadPoolConfigEntity.getThreadPoolName();
        RBucket<ThreadPoolConfigEntity> bucket = redissonClient.getBucket(cacheKey);
        bucket.set(threadPoolConfigEntity, Duration.ofDays(30));
    }
}
