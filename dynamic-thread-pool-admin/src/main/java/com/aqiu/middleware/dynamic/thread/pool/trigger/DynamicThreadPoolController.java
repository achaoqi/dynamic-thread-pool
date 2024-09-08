package com.aqiu.middleware.dynamic.thread.pool.trigger;

import com.alibaba.fastjson.JSON;
import com.aqiu.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import com.aqiu.middleware.dynamic.thread.pool.types.Response;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/dynamic/thread/pool")
public class DynamicThreadPoolController {
    @Resource
    public RedissonClient redissonClient;

    /**
     * 查询线程池数据
     * curl --request GET \
     * --url 'http://localhost:8089/api/v1/dynamic/thread/pool/query_thread_pool_list'
     */
    @RequestMapping(value = "query_thread_pool_list", method = RequestMethod.GET)
    public Response<Map<String,List<ThreadPoolConfigEntity>>> queryThreadPoolList() {
        try {
            Iterable<String> listKeys = redissonClient.getKeys().getKeysByPattern("THREAD_POOL_CONFIG_LIST_KEY"+"*");
            Iterator<String> keysIterable = listKeys.iterator();
            Map<String,List<ThreadPoolConfigEntity>> threadPoolConfigEntityMap = new HashMap<>();
            while (keysIterable.hasNext()) {
                String key = keysIterable.next();
                log.info("key:{}", key);
                RList<ThreadPoolConfigEntity> cacheList = redissonClient.getList(key);
                List<ThreadPoolConfigEntity> threadPoolConfigEntityList = cacheList.readAll();
                threadPoolConfigEntityMap.put(key, threadPoolConfigEntityList);
            }
//            RList<ThreadPoolConfigEntity> cacheList = redissonClient.getList("THREAD_POOL_CONFIG_LIST_KEY");
            return Response.<Map<String,List<ThreadPoolConfigEntity>>>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(threadPoolConfigEntityMap)
                    .build();
        } catch (Exception e) {
            log.error("查询线程池数据异常", e);
            return Response.<Map<String,List<ThreadPoolConfigEntity>>>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 查询线程池配置
     * curl --request GET \
     * --url 'http://localhost:8089/api/v1/dynamic/thread/pool/query_thread_pool_config?appName=dynamic-thread-pool-test-app&threadPoolName=threadPoolExecutor'
     */
    @RequestMapping(value = "query_thread_pool_config", method = RequestMethod.GET)
    public Response<ThreadPoolConfigEntity> queryThreadPoolConfig(@RequestParam String appName, @RequestParam String threadPoolName,@RequestParam String ip,@RequestParam String port) {
        try {
            String cacheKey = "THREAD_POOL_CONFIG_PARAMETER_LIST_KEY" + "_" + appName + "_"+ip+":"+port+"_" + threadPoolName;
            ThreadPoolConfigEntity threadPoolConfigEntity = redissonClient.<ThreadPoolConfigEntity>getBucket(cacheKey).get();
            return Response.<ThreadPoolConfigEntity>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(threadPoolConfigEntity)
                    .build();
        } catch (Exception e) {
            log.error("查询线程池配置异常", e);
            return Response.<ThreadPoolConfigEntity>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 修改线程池配置
     * curl --request POST \
     * --url http://localhost:8089/api/v1/dynamic/thread/pool/update_thread_pool_config \
     * --header 'content-type: application/json' \
     * --data '{
     * "appName":"dynamic-thread-pool-test-app",
     * "threadPoolName": "threadPoolExecutor",
     * "corePoolSize": 1,
     * "maximumPoolSize": 10
     * }'
     */
    @RequestMapping(value = "update_thread_pool_config", method = RequestMethod.POST)
    public Response<Boolean> updateThreadPoolConfig(@RequestBody ThreadPoolConfigEntity request) {
        try {
            log.info("修改线程池配置开始 {} {} {}", request.getAppName(), request.getThreadPoolName(), JSON.toJSONString(request));
            RTopic topic = redissonClient.getTopic("DYNAMIC_THREAD_POOL_REDIS_TOPIC" + "_" + request.getAppName()+"_"+request.getIp()+":"+request.getPort());
            topic.publish(request);
            log.info("修改线程池配置完成 appName:{} threadPoolName:{} ip:{} port:{}", request.getAppName(), request.getThreadPoolName(),request.getIp(),request.getPort());
            return Response.<Boolean>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("修改线程池配置异常 {}", JSON.toJSONString(request), e);
            return Response.<Boolean>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

}
