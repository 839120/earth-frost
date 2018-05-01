package justlive.earth.breeze.frost.executor.redis.notify;

import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import justlive.earth.breeze.frost.core.notify.Event;
import justlive.earth.breeze.frost.core.notify.EventPublisher;
import justlive.earth.breeze.frost.executor.redis.config.SystemProperties;

/**
 * redis实现的事件发布
 * 
 * @author wubo
 *
 */
public class RedisEventPublisherImpl implements EventPublisher {

  @Autowired
  RedissonClient redissonClient;

  @Override
  public void publish(Event event) {
    RScheduledExecutorService executor =
        redissonClient.getExecutorService(String.join(SystemProperties.SEPERATOR,
            SystemProperties.CENTER_PREFIX, EventPublisher.class.getName()));
    executor.execute(new EventExecuteWrapper(event));
  }

}
