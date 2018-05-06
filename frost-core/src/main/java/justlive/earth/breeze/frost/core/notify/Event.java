package justlive.earth.breeze.frost.core.notify;

import justlive.earth.breeze.frost.api.model.JobExecuteParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

  public enum TYPE {
    /**
     * 调度失败
     */
    DISPATCH_FAIL,
    /**
     * 执行失败
     */
    EXECUTE_FAIL,
    /**
     * 调度失败重试
     */
    DISPATCH_FAIL_RETRY,
    /**
     * 执行失败重试
     */
    EXECUTE_FAIL_RETRY;
  }

  /**
   * data
   */
  private JobExecuteParam data;

  /**
   * 类型
   */
  private String type;

  /**
   * 消息
   */
  private String message;

  /**
   * 时间戳
   */
  private long timestamp;


}
