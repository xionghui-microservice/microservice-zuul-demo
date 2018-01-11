package com.github.xionghui.microservice.zuul.controller;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.xionghui.microservice.zuul.utils.UserMenuUtils;
import com.github.xionghuicoder.microservice.common.bean.CommonConstants;
import com.github.xionghuicoder.microservice.common.bean.HttpResult;
import com.github.xionghuicoder.microservice.common.bean.enums.HttpResultEnum;
import com.github.xionghuicoder.microservice.common.bean.enums.LanguageEnum;
import com.github.xionghuicoder.microservice.common.utils.UserPermissionUtils;
import com.netflix.zuul.context.RequestContext;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "首页,用户信息,登出和菜单接口")
@RestController
@RequestMapping("/")
public class UserController {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

  @ApiOperation(value = "cookie", notes = "设置cookie(关闭浏览器后cookie失效)", response = HttpResult.class)
  @RequestMapping(value = "cookie", method = {RequestMethod.POST},
      produces = "application/json; charset=UTF-8")
  @ResponseBody
  public HttpResult<?> cookie(
      @ApiParam(name = "cookieName", value = "cookie名称", defaultValue = "language",
          required = true) @RequestParam String cookieName,
      @ApiParam(name = "cookieValue", value = "cookie值(simpchn,tradchn,english)",
          defaultValue = "simpchn", required = true) @RequestParam String cookieValue,
      HttpServletRequest request, HttpServletResponse response) {
    HttpResult<?> httpResult = null;
    try {
      LOGGER.info("cookie begin");
      LanguageEnum languageEnum = this.getLanguage(request);
      // 建立一个无生命周期的cookie，随着浏览器的关闭即消失
      Cookie cookie = new Cookie(cookieName, cookieValue);
      response.addCookie(cookie);
      httpResult = HttpResult.custom(HttpResultEnum.Success).setLocale(languageEnum.locale).build();
      LOGGER.info("cookie result: {}", httpResult);
    } catch (Throwable t) {
      LOGGER.error("cookie error: {}", t);
      LanguageEnum languageEnum = this.getLanguage(request);
      httpResult =
          HttpResult.custom(HttpResultEnum.Throwable).setLocale(languageEnum.locale).build();
    }
    return httpResult;
  }

  @ApiOperation(value = "语言", notes = "获取所有的语言", response = HttpResult.class)
  @RequestMapping(value = "language", method = {RequestMethod.GET},
      produces = "application/json; charset=UTF-8")
  @ResponseBody
  public HttpResult<?> language(HttpServletRequest request) {
    HttpResult<?> httpResult = null;
    try {
      LOGGER.info("language begin");
      LanguageEnum languageEnum = this.getLanguage(request);
      JSONArray result = LanguageEnum.getLanguageArray();
      for (Object obj : result) {
        JSONObject json = (JSONObject) obj;
        String code = json.getString("code");
        if (languageEnum.code.equals(code)) {
          json.put("isDefault", true);
        }
      }
      httpResult = HttpResult.custom(HttpResultEnum.Success).setLocale(languageEnum.locale)
          .setData(result).build();
      LOGGER.info("language result: {}", httpResult);
    } catch (Throwable t) {
      LOGGER.error("language error: {}", t);
      LanguageEnum languageEnum = this.getLanguage(request);
      httpResult =
          HttpResult.custom(HttpResultEnum.Throwable).setLocale(languageEnum.locale).build();
    }
    return httpResult;
  }

  private LanguageEnum getLanguage(HttpServletRequest request) {
    String language = null;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (CommonConstants.LANGUAGE_COOKIE_HEADER.equals(cookie.getName())) {
          language = cookie.getValue();
          break;
        }
      }
    }
    return LanguageEnum.getLanguageEnum(language);
  }

  @ApiOperation(value = "用户菜单", notes = "获取到的菜单是根据acl权限系统过滤的,并且排好序的", response = HttpResult.class)
  @RequestMapping(value = "menu", method = {RequestMethod.GET},
      produces = "application/json; charset=UTF-8")
  @ResponseBody
  public HttpResult<?> menu(HttpServletRequest request) {
    HttpResult<?> httpResult = null;
    try {
      LOGGER.info("menu begin");
      LanguageEnum languageEnum = this.getLanguage(request);
      httpResult = HttpResult.custom(HttpResultEnum.Success).setLocale(languageEnum.locale)
          .setData(JSON.parseArray(UserMenuUtils.MENUS)).build();
      LOGGER.info("menu result: {}", httpResult);
    } catch (Throwable t) {
      LOGGER.error("menu error: {}", t);
      LanguageEnum languageEnum = this.getLanguage(request);
      httpResult =
          HttpResult.custom(HttpResultEnum.Throwable).setLocale(languageEnum.locale).build();
    }
    return httpResult;
  }

  @ApiOperation(value = "用户信息", notes = "获取用户的个人信息,name默认获取花名,如果花名不存在则会获取本名",
      response = HttpResult.class)
  @RequestMapping(value = "user", method = {RequestMethod.GET},
      produces = "application/json; charset=UTF-8")
  @ResponseBody
  public HttpResult<?> user(HttpServletRequest request) {
    HttpResult<?> httpResult = null;
    try {
      LOGGER.info("user begin");
      RequestContext ctx = RequestContext.getCurrentContext();
      Map<String, String> zuulRequestHeaders = ctx.getZuulRequestHeaders();
      String user = zuulRequestHeaders.get(CommonConstants.USER_HEAD);
      JSONObject userJson = JSON.parseObject(UserPermissionUtils.decode(user));
      JSONObject result = new JSONObject();
      String empId = userJson.getString(CommonConstants.USER_EMPID);
      result.put("empId", empId);
      String name = userJson.getString(CommonConstants.USER_NAME);
      result.put("name", name);
      String email = userJson.getString(CommonConstants.USER_EMAIL);
      result.put("email", email);
      LanguageEnum languageEnum = this.getLanguage(request);
      httpResult = HttpResult.custom(HttpResultEnum.Success).setLocale(languageEnum.locale)
          .setData(result).build();
      LOGGER.info("user result: {}", httpResult);
    } catch (Throwable t) {
      LOGGER.error("user error: {}", t);
      LanguageEnum languageEnum = this.getLanguage(request);
      httpResult =
          HttpResult.custom(HttpResultEnum.Throwable).setLocale(languageEnum.locale).build();
    }
    return httpResult;
  }
}
