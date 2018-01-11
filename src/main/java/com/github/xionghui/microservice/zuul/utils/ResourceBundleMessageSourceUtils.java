package com.github.xionghui.microservice.zuul.utils;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.github.xionghuicoder.microservice.common.utils.CommonResourceBundleMessageSourceUtils;

@Component
public class ResourceBundleMessageSourceUtils extends CommonResourceBundleMessageSourceUtils {

  @Override
  @PostConstruct
  public void init() {
    super.init();
  }
}
