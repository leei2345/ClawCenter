package com.aizhizu.service.proxy.wrapper;

import com.aizhizu.service.proxy.BaseProxyChecker;

public class ProxyCheckerOfanjuke extends BaseProxyChecker
{
  private static String indentidy = "web_anjuke";
  private static String url = "http://beijing.anjuke.com/";

  public ProxyCheckerOfanjuke() {
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