package com.aizhizu.dao;

import java.util.Set;

import org.jdiy.core.Ls;
import org.jdiy.core.Rs;

import com.aizhizu.util.ConfigUtil;
import com.aizhizu.util.LoggerUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Redis {
	private JedisPool jedisPool;

	private Redis() {
		LoggerUtil.ClawerLog("Redis Initializing from config.properties.......");
//		ResourceBundle config = ResourceBundle.getBundle("data");
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxActive(Integer.valueOf(
				ConfigUtil.getString("redis.pool.maxActive")).intValue());
		jedisPoolConfig.setMaxIdle(Integer.valueOf(
				ConfigUtil.getString("redis.pool.maxIdle")).intValue());
		jedisPoolConfig.setMaxWait(Integer.valueOf(
				ConfigUtil.getString("redis.pool.maxWait")).intValue());
		jedisPoolConfig.setTestOnBorrow(Boolean.valueOf(
				ConfigUtil.getString("redis.pool.testOnBorrow")).booleanValue());
		jedisPoolConfig.setTestOnReturn(Boolean.valueOf(
				ConfigUtil.getString("redis.pool.testOnReturn")).booleanValue());
		this.jedisPool = new JedisPool(jedisPoolConfig,
				ConfigUtil.getString("redis.ip"), Integer.valueOf(
						ConfigUtil.getString("redis.port")).intValue(), Integer
						.valueOf(ConfigUtil.getString("redis.timeout")).intValue(),
				null, 1);
	}

	public static Redis getInstance() {
		return RedisContainer.instance;
	}

	public boolean hasNewsUrl(String url) {
		Jedis jedis = (Jedis) this.jedisPool.getResource();
		String result = jedis.hget("house_url", url);
		this.jedisPool.returnResource(jedis);

		return (result != null) && (!result.isEmpty());
	}

	public void pushNewsUrl(String url) {
		Jedis jedis = (Jedis) this.jedisPool.getResource();
		jedis.hset("house_url", url, "1");
		this.jedisPool.returnResource(jedis);
	}

	public void refreshPlotMap() {
		String sql = "select plot,area,district,yx from tb_plot";
		Ls ls = DataBaseCenter.Dao.ls(sql, 0, 0);
		Rs[] items = ls.getItems();
		Jedis jedis = (Jedis) this.jedisPool.getResource();
		for (Rs map : items) {
			String plot = map.get("plot");
			String area = map.get("area");
			String district = map.get("district");
			String yx = map.get("yx");
			String value = area.trim() + "|" + district.trim() + "|" + yx.trim();
			jedis.hset("plot", plot.trim(), value);
		}
		this.jedisPool.returnResource(jedis);
	}

	public String getPlotData(String plot) {
		Jedis jedis = (Jedis) this.jedisPool.getResource();
		String result = jedis.hget("plot", plot);
		this.jedisPool.returnResource(jedis);
		return result == null ? "" : result;
	}

	public void test() {
		Jedis jedis = (Jedis) this.jedisPool.getResource();
		jedis.sadd("myset", new String[] { "1" });
		jedis.sadd("myset", new String[] { "2" });
		jedis.sadd("myset", new String[] { "3" });
		jedis.sadd("myset", new String[] { "4" });
		Set<String> setValues = jedis.smembers("myset");
		System.out.println(setValues);
	}


	private static class RedisContainer {
		private static Redis instance = new Redis();
	}
	
	public static void main(String[] args) {
		Redis r = getInstance();
		r.test();
	}
	
}