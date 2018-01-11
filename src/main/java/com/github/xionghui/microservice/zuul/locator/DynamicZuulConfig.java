package com.github.xionghui.microservice.zuul.locator;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamicZuulConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicZuulConfig.class);

  @Value("${api.mapping}")
  private String apiMapping;

  @Autowired
  private ServerProperties server;
  @Autowired
  private DiscoveryClient discovery;
  @Autowired
  private ZuulProperties zuulProperties;
  @Autowired
  private ServiceInstance localServiceInstance;

  @Bean
  public DynamicRouteLocator routeLocator() {
    DynamicRouteLocator routeLocator = new DynamicRouteLocator(this.server.getServletPrefix(),
        this.discovery, this.zuulProperties, this.localServiceInstance);
    return routeLocator;
  }

  /**
   * 动态刷新配置
   *
   * @author xionghui
   * @version 1.0.0
   * @since 1.0.0
   */
  private class DynamicRouteLocator extends DiscoveryClientRouteLocator
      implements RefreshableRouteLocator {

    DynamicRouteLocator(String servletPath, DiscoveryClient discovery, ZuulProperties properties,
        ServiceInstance localServiceInstance) {
      super(servletPath, discovery, properties, localServiceInstance);
    }

    @Override
    public void refresh() {
      this.doRefresh();
    }

    @Override
    public LinkedHashMap<String, ZuulRoute> locateRoutes() {
      LOGGER.info("locateRoutes begin");
      LinkedHashMap<String, ZuulRoute> routesMap = new LinkedHashMap<String, ZuulRoute>();
      // 默认路由信息
      // routesMap.putAll(super.locateRoutes());
      // 从配置中加载路由信息
      routesMap.putAll(this.refreshApiMapping());
      LOGGER.info("locateRoutes end: {}", routesMap);
      return routesMap;
    }

    private Map<String, ZuulRoute> refreshApiMapping() {
      LOGGER.info("refreshApiMapping begin");
      Map<String, ZuulRoute> routeMap = new LinkedHashMap<>();
      LOGGER.info("refreshApiMapping apiMapping: {}", DynamicZuulConfig.this.apiMapping);
      if (DynamicZuulConfig.this.apiMapping == null) {
        LOGGER.error("refreshApiMapping apiMapping is null");
        return routeMap;
      }
      DynamicZuulConfig.this.apiMapping = DynamicZuulConfig.this.apiMapping.trim();
      String[] mapArray = DynamicZuulConfig.this.apiMapping.split(",");
      for (String map : mapArray) {
        String[] mapping = map.split(":");
        if (mapping.length != 2) {
          LOGGER.error("refreshApiMapping mapping's length is not 2: {}", map);
          continue;
        }
        String path = mapping[0];
        String serviceId = mapping[1];
        if (this.isIllegal(path) || this.isIllegal(serviceId)) {
          LOGGER.error("refreshApiMapping path or serviceId is illegal: {}, {}", path, serviceId);
          continue;
        }
        ZuulRoute zuulRoute = new ZuulRoute();
        zuulRoute.setId(serviceId);
        zuulRoute.setServiceId(serviceId);
        path = "/" + path + "/**";
        zuulRoute.setPath(path);
        routeMap.put(path, zuulRoute);
      }
      LOGGER.info("refreshApiMapping end: {}", routeMap);
      return routeMap;
    }

    /**
     * 不能包含/,*,空格(whitespace)等字符
     *
     * @param s s
     * @return boolean
     */
    private boolean isIllegal(String s) {
      int len = s.length();
      if (len == 0) {
        return true;
      }
      for (int i = 0; i < len; i++) {
        char c = s.charAt(i);
        if ('/' == c || '*' == c || Character.isWhitespace(c)) {
          return true;
        }
      }
      return false;
    }
  }
}
