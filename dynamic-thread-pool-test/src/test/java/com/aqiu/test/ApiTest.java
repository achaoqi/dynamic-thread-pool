package com.aqiu.test;

import com.aqiu.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RTopic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Resource
    private RTopic dynamicThreadPoolTopic;

    @Test
    public void test_dynamicThreadPoolRedisTopic() throws InterruptedException {
        ThreadPoolConfigEntity config = new ThreadPoolConfigEntity();
        config.setAppName("dynamic-thread-pool-test-app");
        config.setIp("192.168.245.1");
        config.setPort("8093");
        config.setThreadPoolName("threadPoolExecutor01");

        config.setCorePoolSize(10);
        config.setMaximumPoolSize(20);
        dynamicThreadPoolTopic.publish(config);

        new CountDownLatch(1).await();
    }

}
