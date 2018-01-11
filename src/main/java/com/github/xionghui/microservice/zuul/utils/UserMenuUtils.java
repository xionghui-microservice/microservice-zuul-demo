package com.github.xionghui.microservice.zuul.utils;

/**
 * mock员工信息和菜单
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class UserMenuUtils {

  public static final String EMPID = "100";
  public static final String NAME = "熊辉";
  public static final String EMAIL = "xionghui_coder@163.com";

  public static final String MENUS =
      "[{\"subMenu\":[{\"code\":\"menu-base-employee\",\"menuCode\":\"base-employee\",\"isLeaf\":true}],\"code\":\"menu-base\",\"menuCode\":\"base\",\"isLeaf\":false},{\"subMenu\":[{\"code\":\"menu-business-project\",\"menuCode\":\"business-project\",\"isLeaf\":true}],\"code\":\"menu-business\",\"menuCode\":\"business\",\"isLeaf\":false}]";
}
