package com.aqiu.middleware.dynamic.thread.pool.sdk.config;

import com.alibaba.fastjson.JSON;
import com.aqiu.middleware.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import com.aqiu.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.aqiu.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import com.aqiu.middleware.dynamic.thread.pool.sdk.domain.valueobj.RegistryEnumVO;
import com.aqiu.middleware.dynamic.thread.pool.sdk.registry.IRegistry;
import com.aqiu.middleware.dynamic.thread.pool.sdk.registry.redis.RedisRegistry;
import com.aqiu.middleware.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import com.aqiu.middleware.dynamic.thread.pool.sdk.trigger.listener.ThreadPoolConfigAdjustListener;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态配置入口
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(DynamicThreadPoolAutoProperties.class)
public class DynamicThreadPoolAutoConfig {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    private String applicationName;

    @Bean("dynamicThreadPoolService")
    public IDynamicThreadPoolService dynamicThreadPoolService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap,RedissonClient redissonClient) {
        applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        if (StringUtils.isBlank(applicationName)){
            applicationName="缺省的";
            logger.warn("动态线程池，启动警告,未配备应用名称");
        }
//        应用重启如果缓存中有相关配置，则使用缓存中的配置
        Set<String> threadPoolKeys = threadPoolExecutorMap.keySet();
        for (String threadPoolKey : threadPoolKeys) {
            String key = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + applicationName + "_" + threadPoolKey;
            RBucket<ThreadPoolConfigEntity> cacheConfig = redissonClient.getBucket(key);
            if (cacheConfig!=null&&cacheConfig.get()!=null){
                ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolKey);
                threadPoolExecutor.setMaximumPoolSize(cacheConfig.get().getMaximumPoolSize());
                threadPoolExecutor.setCorePoolSize(cacheConfig.get().getCorePoolSize());
            }
        }
        logger.info("{}应用 线程信息:{}",applicationName, JSON.toJSONString(threadPoolKeys));
        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }

    @Bean("dynamicThreadRedissonClient")
    public RedissonClient redissonClient(DynamicThreadPoolAutoProperties properties) {
        Config config = new Config();
        // 根据需要可以设定编解码器；https://github.com/redisson/redisson/wiki/4.-%E6%95%B0%E6%8D%AE%E5%BA%8F%E5%88%97%E5%8C%96
        config.setCodec(JsonJacksonCodec.INSTANCE);

        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive())
        ;

        RedissonClient redissonClient = Redisson.create(config);

        logger.info("动态线程池，注册器（redis）链接初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());

        return redissonClient;
    }

    @Bean
    public IRegistry redisRegistry(RedissonClient redissonClient) {
        return new RedisRegistry(redissonClient);
    }

    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry) {
        return new ThreadPoolDataReportJob(dynamicThreadPoolService, registry);
    }

    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry){
        return new ThreadPoolConfigAdjustListener(dynamicThreadPoolService, registry);
    }

    @Bean("dynamicThreadPoolTopic")
    public RTopic threadPoolConfigAdjustListener(RedissonClient redissonClient,ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener) {
        RTopic rTopic = redissonClient.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey()+"_"+applicationName);
        rTopic.addListener(ThreadPoolConfigEntity.class, threadPoolConfigAdjustListener);
        return rTopic;
    }


}
