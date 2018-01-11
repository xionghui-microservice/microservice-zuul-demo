package com.github.xionghui.microservice.zuul.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.xionghuicoder.microservice.common.bean.CommonConstants;
import com.github.xionghuicoder.microservice.common.bean.HttpResult;
import com.github.xionghuicoder.microservice.common.bean.enums.HttpResultEnum;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "改接口不会被调用到,该方法用作swagger服务,因为/api/{service}会被ZuulServlet匹配到")
@RestController
@RequestMapping("/api")
public class ApiController {

  @ApiOperation(value = "restful服务接口入口",
      notes = "所有restful服务都通过该接口调用,通过service区分不同的服务,通过function区分服务的不同接口",
      response = HttpResult.class)
  @RequestMapping(value = "/{service}", method = {RequestMethod.GET, RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ResponseBody
  public HttpResult<?> restful(
      @ApiParam(name = "service", value = "服务的相对地址", required = true) @PathVariable String service,
      @ApiParam(name = CommonConstants.FUNCTION, value = "服务接口的唯一标识",
          required = true) @RequestParam String function,
      @ApiParam(name = CommonConstants.BODY, value = "服务接口的json参数", defaultValue = "{}",
          required = true, type = "json") @RequestParam String[] body) {
    return HttpResult.custom(HttpResultEnum.Success).build();
  }

  @ApiOperation(value = "upload服务接口入口",
      notes = "所有upload服务都通过该接口调用,通过service区分不同的服务,通过function区分服务的不同接口",
      response = HttpResult.class)
  @RequestMapping(value = "/{service}/" + CommonConstants.UPLOAD_URI, method = RequestMethod.POST,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseBody
  public HttpResult<?> upload(
      @ApiParam(name = "service", value = "服务的相对地址", required = true) @PathVariable String service,
      @ApiParam(name = CommonConstants.FUNCTION, value = "服务接口的唯一标识",
          required = true) @RequestParam String function,
      @ApiParam(name = CommonConstants.BODY, value = "服务接口的json参数", defaultValue = "{}",
          required = true, type = "json") @RequestParam String[] body,
      HttpServletRequest request, @RequestParam(value = "files") MultipartFile files) {
    return HttpResult.custom(HttpResultEnum.Success).build();
  }

  @ApiOperation(value = "download服务接口入口",
      notes = "所有download服务都通过该接口调用,通过service区分不同的服务,通过function区分服务的不同接口")
  @RequestMapping(value = "/{service}/" + CommonConstants.DOWNLOAD_URI, method = RequestMethod.GET)
  public void download(
      @ApiParam(name = "service", value = "服务的相对地址", required = true) @PathVariable String service,
      @ApiParam(name = CommonConstants.FUNCTION, value = "服务接口的唯一标识",
          required = true) @RequestParam String function,
      @ApiParam(name = CommonConstants.BODY, value = "服务接口的json参数", defaultValue = "{}",
          required = true, type = "json") @RequestParam String[] body,
      HttpServletRequest request) {}
}
