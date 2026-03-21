package org.redis;

import redis.clients.jedis.JedisPooled;

import java.util.Map;

public class Main {
    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    private static final String REDIS_MAP_KEY = "demo-map";

    public static void main(String[] args) {
        try (JedisPooled jedis = new JedisPooled(HOST, PORT)) {
            jedis.del(REDIS_MAP_KEY);

            RedisMap map = new RedisMap(jedis, REDIS_MAP_KEY);

            printSection("Начальное состояние");
            printMapState(map);

            printSection("put / get / size");
            System.out.println("put(\"a\", \"1\") -> " + map.put("a", "1"));
            System.out.println("get(\"a\") -> " + map.get("a"));
            System.out.println("size() -> " + map.size());
            printMapState(map);

            printSection("Перезапись существующего значения");
            System.out.println("put(\"a\", \"100\") -> " + map.put("a", "100"));
            System.out.println("get(\"a\") -> " + map.get("a"));
            printMapState(map);

            printSection("Добавление новых элементов");
            map.put("b", "2");
            map.put("c", "3");
            System.out.println("containsKey(\"b\") -> " + map.containsKey("b"));
            System.out.println("containsValue(\"2\") -> " + map.containsValue("2"));
            printMapState(map);

            printSection("putAll");
            map.putAll(Map.of(
                    "x", "10",
                    "y", "20"
            ));
            printMapState(map);

            printSection("remove");
            System.out.println("remove(\"b\") -> " + map.remove("b"));
            System.out.println("containsKey(\"b\") -> " + map.containsKey("b"));
            printMapState(map);

            printSection("clear");
            map.clear();
            System.out.println("isEmpty() -> " + map.isEmpty());
            printMapState(map);

            jedis.del(REDIS_MAP_KEY);
        } catch (Exception e) {
            System.err.println("Ошибка при работе с RedisMap");
            e.printStackTrace();
        }
    }

    private static void printSection(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    private static void printMapState(RedisMap map) {
        System.out.println("size      = " + map.size());
        System.out.println("isEmpty   = " + map.isEmpty());
        System.out.println("keySet    = " + map.keySet());
        System.out.println("values    = " + map.values());
        System.out.println("entrySet  = " + map.entrySet());
    }
}