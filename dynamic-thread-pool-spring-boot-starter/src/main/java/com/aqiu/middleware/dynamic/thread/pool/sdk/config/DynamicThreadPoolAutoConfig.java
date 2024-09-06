package com.aqiu.middleware.dynamic.thread.pool.sdk.config;

import com.alibaba.fastjson.JSON;
import com.aqiu.middleware.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import com.aqiu.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态配置入口
 */
@Configuration
public class DynamicThreadPoolAutoConfig {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    @Bean("dynamicThreadPoolService")
    public IDynamicThreadPoolService dynamicThreadPoolService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) {
        String applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        if (StringUtils.isBlank(applicationName)){
            applicationName="缺省的";
            logger.warn("动态线程池，启动警告,未配备应用名称");
        }
        logger.info("{}应用 线程信息:{}",applicationName, JSON.toJSONString(threadPoolExecutorMap.keySet()));
        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }

}
