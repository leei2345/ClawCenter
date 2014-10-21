package com.aizhizu.service.proxy.wrapper;

import com.aizhizu.service.proxy.BaseProxyChecker;

public class ProxyCheckerOfsoufang extends BaseProxyChecker
{
  private static String indentidy = "web_soufang";
  private static String url = "http://bj.fang.com/";

  public ProxyCheckerOfsoufang() {
    super(indentidy, url);
  }

  public boolean analyze(Object[] objects)
  {
    boolean res = false;
    int statusLineCode = ((Integer)objects[0]).intValue();
    String html = (String)objects[1];
    if ((statusLineCode == 200) && (html.length() > 20)) {
      res = true;
    }
    return res;
  }
}