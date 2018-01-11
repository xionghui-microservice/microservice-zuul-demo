package com.github.xionghui.microservice.zuul.utils;

import java.lang.reflect.Field;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.cloud.client.discovery.event.HeartbeatMonitor;
import org.springframework.stereotype.Component;

import com.github.xionghuicoder.microservice.common.BusinessException;

/**
 * 修改默认的HeartbeatMonitor，控制更新心跳时不刷新route
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class ZuulDiscoveryRefreshRoutesListenerUtils {
  private static final String ZUUL_LISTENER = "zuulDiscoveryRefreshRoutesListener";
  private static final String ZUUL_LISTENER_FIELD = "monitor";

  @Resource(name = ZUUL_LISTENER)
  private Object zuulListener;

  @PostConstruct
  public void init() {
    try {
      Field f = this.zuulListener.getClass().getDeclaredField(ZUUL_LISTENER_FIELD);
      f.setAccessible(true);
      f.set(this.zuulListener, new DynamicHeartbeatMonitor());
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new BusinessException(e);
    }
  }

  /**
   * 该HeartbeatMonitor控制zuulDiscoveryRefreshRoutesListener检测心跳后不刷新route
   *
   * @author xionghui
   * @version 1.0.0
   * @since 1.0.0
   */
  private class DynamicHeartbeatMonitor extends HeartbeatMonitor {

    @Override
    public boolean update(Object value) {
      return false;
    }
  }
}
