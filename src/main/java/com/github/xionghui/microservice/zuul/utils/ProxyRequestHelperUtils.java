package com.github.xionghui.microservice.zuul.utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.github.xionghuicoder.microservice.common.BusinessException;
import com.github.xionghuicoder.microservice.common.bean.CommonConstants;
import com.github.xionghuicoder.microservice.common.utils.CommonRequestUtils;
import com.netflix.zuul.util.HTTPRequestUtils;

@Component
public class ProxyRequestHelperUtils {
  private static final String RIBBONROUTING_FILTER = "ribbonRoutingFilter";
  private static final String RIBBONROUTING_FILTER_FIELD = "helper";

  @Resource(name = RIBBONROUTING_FILTER)
  private Object ribbonRoutingFilter;

  @Autowired
  protected ZuulProperties zuulProperties;

  @PostConstruct
  public void init() {
    ProxyRequestHelper helper = new EncodeBodyProxyRequestHelper();
    helper.setIgnoredHeaders(this.zuulProperties.getIgnoredHeaders());
    helper.setTraceRequestBody(this.zuulProperties.isTraceRequestBody());
    try {
      Field f = this.ribbonRoutingFilter.getClass().getDeclaredField(RIBBONROUTING_FILTER_FIELD);
      f.setAccessible(true);
      f.set(this.ribbonRoutingFilter, helper);
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
  private class EncodeBodyProxyRequestHelper extends ProxyRequestHelper {

    @Override
    public MultiValueMap<String, String> buildZuulRequestQueryParams(HttpServletRequest request) {
      Map<String, List<String>> queryMap = HTTPRequestUtils.getInstance().getQueryParams();
      if (queryMap == null) {
        queryMap = new HashMap<>();
        Map<String, String> paramsMap = CommonRequestUtils.getParameterMap(request);
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
          String name = entry.getKey();
          String value = entry.getValue();
          try {
            name = URLDecoder.decode(name, CommonConstants.UTF8_ENCODING);
          } catch (UnsupportedEncodingException e) {
          }
          try {
            value = URLDecoder.decode(value, CommonConstants.UTF8_ENCODING);
          } catch (Exception e) {
          }
          List<String> valueList = queryMap.get(name);
          if (valueList == null) {
            valueList = new LinkedList<String>();
            queryMap.put(name, valueList);
          }
          valueList.add(value);
        }
      }
      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      for (Map.Entry<String, List<String>> entry : queryMap.entrySet()) {
        String key = entry.getKey();
        List<String> values = entry.getValue();
        for (String value : values) {
          // if (CommonConstants.BODY.equals(key)) {
          // try {
          // value = URLEncoder.encode(value, CommonConstants.UTF8_ENCODING);
          // // 解决编码后空格变成加号问题：+号编码后会变成%2B，空格编码后会变成+号，所以可以吧+统一替换成空格就好
          // value = value.replaceAll("\\+", "%20");
          // } catch (UnsupportedEncodingException e) {
          // }
          // }
          params.add(key, value);
        }
      }
      return params;
    }
  }
}
