package justlive.earth.breeze.frost.core.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事件通知抽象类
 * 
 * @author wubo
 *
 */
public abstract class AbstractEventNotifier implements Notifier {

  /**
   * 是否启用
   */
  private boolean enabled = true;

  @Override
  public void notify(Event event) {
    if (enabled && shouldNotify(event)) {
      try {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("通知事件:{}", event);
        }
        doNotify(event);
      } catch (Exception ex) {
        getLogger().error("事件通知失败 {} ", event, ex);
      }
    }
  }


  protected boolean shouldNotify(Event event) {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("通知事件:{}", event);
    }
    return true;
  }

  protected abstract void doNotify(Event event);

  private Logger getLogger() {
    return LoggerFactory.getLogger(this.getClass());
  }
}
