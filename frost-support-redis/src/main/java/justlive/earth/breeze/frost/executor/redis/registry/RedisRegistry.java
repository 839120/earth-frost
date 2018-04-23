package justlive.earth.breeze.frost.executor.redis.registry;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.redisson.api.RMapCache;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import justlive.earth.breeze.frost.core.config.JobProperties;
import justlive.earth.breeze.frost.core.model.JobExecutor;
import justlive.earth.breeze.frost.core.model.JobGroup;
import justlive.earth.breeze.frost.core.registry.AbstractRegistry;
import justlive.earth.breeze.frost.core.registry.HeartBeat;
import justlive.earth.breeze.frost.executor.redis.config.SystemProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * redis注册实现类
 * 
 * @author wubo
 *
 */
@Profile(SystemProperties.PROFILE_EXECUTOR)
@Slf4j
@Component
public class RedisRegistry extends AbstractRegistry {

  @Autowired
  RedissonClient redissonClient;

  @Autowired
  JobProperties executorProperties;

  RTopic<HeartBeat> topic;

  ScheduledFuture<?> beatFuture;

  ScheduledExecutorService scheduledExecutor =
      new ScheduledThreadPoolExecutor(1, new BasicThreadFactory.Builder()
          .namingPattern("heartbeat-schedule-pool-%d").daemon(true).build());

  @Override
  public void register() {

    // 订阅心跳检测
    topic = redissonClient.getTopic(String.join(SystemProperties.SEPERATOR,
        SystemProperties.EXECUTOR_PREFIX, HeartBeat.class.getName()));

    // 注册job执行器
    for (JobGroup jobGroup : jobExecutorBean.getGroups()) {
      String key = String.join(SystemProperties.SEPERATOR, SystemProperties.JOB_GROUP_PREFIX,
          jobGroup.getGroupKey(), jobGroup.getJobKey());
      log.info("register job [{}]", key);
      redissonClient.getExecutorService(key).registerWorkers(executorProperties.getParallel());
    }

    // script执行器
    if (jobProps.getExecutor().getScriptJobEnabled()) {
      redissonClient.getExecutorService(SystemProperties.JOB_SCRIPT_PREFIX)
          .registerWorkers(executorProperties.getParallel());
    }

    // 心跳任务
    beatFuture = scheduledExecutor.scheduleWithFixedDelay(() -> {
      try {
        long subscribes = topic.publish(new HeartBeat(jobExecutorBean.getAddress(),
            HeartBeat.TYPE.PING.name(), jobExecutorBean.getName()));
        if (subscribes == 0) {
          log.warn("未发现调度中心服务");
        }
        RMapCache<String, JobExecutor> cache =
            redissonClient.getMapCache(String.join(SystemProperties.SEPERATOR,
                SystemProperties.EXECUTOR_PREFIX, JobExecutor.class.getName()));
        cache.put(jobExecutorBean.getId(), jobExecutorBean, SystemProperties.HEARTBEAT,
            TimeUnit.SECONDS);
      } catch (Exception e) {
        log.error("心跳任务出错 ", e);
      }
    }, SystemProperties.HEARTBEAT, SystemProperties.HEARTBEAT, TimeUnit.SECONDS);

    // 注册事件
    topic.publish(new HeartBeat(jobExecutorBean.getAddress(), HeartBeat.TYPE.REGISTER.name(),
        jobExecutorBean.getName()));
  }

  @Override
  public void unregister() {
    beatFuture.cancel(true);
    topic.publish(new HeartBeat(jobExecutorBean.getAddress(), HeartBeat.TYPE.UNREGISTER.name(),
        jobExecutorBean.getName()));
  }

}
