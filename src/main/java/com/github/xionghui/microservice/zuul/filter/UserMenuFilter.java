package com.github.xionghui.microservice.zuul.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.xionghui.microservice.zuul.utils.UserMenuUtils;
import com.github.xionghuicoder.microservice.common.bean.CommonConstants;
import com.github.xionghuicoder.microservice.common.bean.enums.LanguageEnum;
import com.github.xionghuicoder.microservice.common.utils.UserPermissionUtils;
import com.netflix.zuul.context.RequestContext;

@WebFilter(urlPatterns = "/*", filterName = "userMenuFilter")
public class UserMenuFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserMenuFilter.class);

  private static final Set<String> SPECIAL_SET = new HashSet<>();;

  static {
    SPECIAL_SET.add("/health");
    SPECIAL_SET.add("/info");
    SPECIAL_SET.add("/favicon.ico");
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) arg0;
    String uri = request.getRequestURI();
    if (SPECIAL_SET.contains(uri)) {
      arg2.doFilter(arg0, arg1);
      return;
    }

    RequestContext ctx = RequestContext.getCurrentContext();
    // 标识来源是否是网关
    ctx.addZuulRequestHeader(CommonConstants.ZUUL_HEAD, Boolean.toString(Boolean.TRUE));

    this.addCookie(request, ctx);

    JSONObject userJson = new JSONObject();
    String empId = UserMenuUtils.EMPID;
    userJson.put(CommonConstants.USER_EMPID, empId);
    userJson.put(CommonConstants.USER_NAME, UserMenuUtils.NAME);
    userJson.put(CommonConstants.USER_EMAIL, UserMenuUtils.EMAIL);
    ctx.addZuulRequestHeader(CommonConstants.USER_HEAD,
        UserPermissionUtils.encode(userJson.toString()));
    LOGGER.info("doFilter userJson: {}", userJson);

    this.fetchAllPermissions(empId, ctx);

    arg2.doFilter(arg0, arg1);
  }

  /**
   * 透传以supplychain_开头的cookie,透传多语cookie
   *
   * @param request request
   * @param ctx ctx
   */
  private void addCookie(HttpServletRequest request, RequestContext ctx) {
    String language = null;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        String cookieName = cookie.getName();
        if (CommonConstants.LANGUAGE_COOKIE_HEADER.equals(cookieName)) {
          language = cookie.getValue();
        } else if (cookieName != null
            && cookieName.startsWith(CommonConstants.PASS_COOKIE_PREFIX)) {
          ctx.addZuulRequestHeader(cookieName, cookie.getValue());
        }
      }
    }

    LanguageEnum languageEnum = LanguageEnum.getLanguageEnum(language);
    LOGGER.info("languageCookie language: {}", languageEnum);
    ctx.addZuulRequestHeader(CommonConstants.LANGUAGE_COOKIE_HEADER, languageEnum.code);
  }

  private void fetchAllPermissions(String empId, RequestContext ctx)
      throws UnsupportedEncodingException {
    LOGGER.info("fetchAllPermissions begin: {}", empId);
    JSONObject aclJson = new JSONObject();
    aclJson.put(CommonConstants.PERMISSION_MENU, this.fetchMenu(empId));
    ctx.addZuulRequestHeader(CommonConstants.PERMISSION_HEAD,
        UserPermissionUtils.encode(aclJson.toString()));
    LOGGER.info("fetchAllPermissions end: {}", aclJson);
  }

  private JSONObject fetchMenu(String empId) {
    LOGGER.info("fetchMenu begin: {}", empId);
    JSONObject menuJson = new JSONObject();
    JSONArray menuArray = JSON.parseArray(UserMenuUtils.MENUS);
    this.convertLeafMenu(menuJson, menuArray);
    LOGGER.info("fetchMenu end: {}", menuJson);
    return menuJson;
  }

  private void convertLeafMenu(JSONObject menuJson, JSONArray menuArray) {
    if (menuArray == null) {
      return;
    }
    for (Object obj : menuArray) {
      JSONObject menu = (JSONObject) obj;
      if (menu.getBoolean("isLeaf")) {
        String menuCode = menu.getString("menuCode");
        menuCode = menuCode.replaceAll("-", "/");
        menuJson.put(menuCode, true);
        continue;
      }
      JSONArray subMenus = menu.getJSONArray("subMenu");
      this.convertLeafMenu(menuJson, subMenus);
    }
  }

  @Override
  public void destroy() {}
}
