package com.aizhizu.dao;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Redis
{
  private JedisPool jedisPool;
  private static Logger logger = LoggerFactory.getLogger("ClawerLogger");

  private Redis() {
    logger.info("Redis Initializing from config.properties.......");
    ResourceBundle config = ResourceBundle.getBundle("data");
    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    jedisPoolConfig.setMaxActive(Integer.valueOf(config.getString("redis.pool.maxActive")).intValue());
    jedisPoolConfig.setMaxIdle(Integer.valueOf(config.getString("redis.pool.maxIdle")).intValue());
    jedisPoolConfig.setMaxWait(Integer.valueOf(config.getString("redis.pool.maxWait")).intValue());
    jedisPoolConfig.setTestOnBorrow(Boolean.valueOf(config.getString("redis.pool.testOnBorrow")).booleanValue());
    jedisPoolConfig.setTestOnReturn(Boolean.valueOf(config.getString("redis.pool.testOnReturn")).booleanValue());
    this.jedisPool = 
      new JedisPool(jedisPoolConfig, config.getString("redis.ip"), 
      Integer.valueOf(config.getString("redis.port")).intValue(), 
      Integer.valueOf(config.getString("redis.timeout")).intValue(), null, 1);
  }

  public static Redis getInstance() {
    return RedisContainer.instance;
  }

  public boolean hasNewsUrl(String url)
  {
    Jedis jedis = (Jedis)this.jedisPool.getResource();
    String result = jedis.hget("house_url", url);
    this.jedisPool.returnResource(jedis);

    return (result != null) && (!result.isEmpty());
  }

  public void pushNewsUrl(String url)
  {
    Jedis jedis = (Jedis)this.jedisPool.getResource();
    jedis.hset("house_url", url, "1");
    this.jedisPool.returnResource(jedis);
  }

  @SuppressWarnings("unchecked")
public void refreshPlotMap()
  {
    String sql = "select plot,area,district,yx from tb_plot";
    DBDataReader reader = new DBDataReader(sql);
    List<Map<String, Object>> list = reader.readAll();
    Jedis jedis = (Jedis)this.jedisPool.getResource();
    for (Map<String, Object> map : list) {
      String plot = (String)map.get("plot");
      String area = (String)map.get("area");
      String district = (String)map.get("district");
      String yx = (String)map.get("yx");
      String value = area.trim() + "|" + district.trim() + "|" + yx.trim();
      jedis.hset("plot", plot.trim(), value);
    }
    this.jedisPool.returnResource(jedis);
  }

  public String getPlotData(String plot) {
    Jedis jedis = (Jedis)this.jedisPool.getResource();
    String result = jedis.hget("plot", plot);
    this.jedisPool.returnResource(jedis);
    return result == null ? "" : result;
  }

  public void test() {
    Jedis jedis = (Jedis)this.jedisPool.getResource();
    jedis.sadd("myset", new String[] { "1" });
    jedis.sadd("myset", new String[] { "2" });
    jedis.sadd("myset", new String[] { "3" });
    jedis.sadd("myset", new String[] { "4" });
    Set<String> setValues = jedis.smembers("myset");
    System.out.println(setValues);
  }

  public static void main(String[] args)
  {
    Redis r = getInstance();
    r.test();
  }

  private static class RedisContainer
  {
    private static Redis instance = new Redis();
  }
}