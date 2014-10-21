package com.aizhizu.core.scheduled;

import com.aizhizu.core.BaseHandler;
import com.aizhizu.service.proxy.ProxyChecker;

/**
 * 代理检测处理控制中心
 * @author leei
 *
 */
public class ProxyCheckHandle extends BaseHandler
{
  private static String identidy = "proxy_check";

  public ProxyCheckHandle() {
    super(identidy);
  }

  protected void StartHandle()
  {
    ProxyChecker checker = new ProxyChecker();
    checker.Implement();
  }
}