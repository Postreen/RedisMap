package org.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPooled;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RedisMapTest {
    private static final String REDIS_MAP_KEY = "test-redis-map";

    private JedisPooled jedis;
    private RedisMap map;

    @BeforeEach
    void setUp() {
        jedis = new JedisPooled("localhost", 6379);
        jedis.del(REDIS_MAP_KEY);
        map = new RedisMap(jedis, REDIS_MAP_KEY);
    }

    @AfterEach
    void tearDown() {
        jedis.del(REDIS_MAP_KEY);
        jedis.close();
    }

    @Test
    @DisplayName("✅ Успешно: size и isEmpty отражают текущее состояние Redis")
    void sizeAndIsEmptyReflectRedisState() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        jedis.hset(REDIS_MAP_KEY, "one", "1");

        assertFalse(map.isEmpty());
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("✅ Успешно: containsKey и containsValue корректно проверяют наличие ключей и значений")
    void containsKeyAndContainsValueWork() {
        map.put("one", "1");

        assertTrue(map.containsKey("one"));
        assertFalse(map.containsKey("missing"));
        assertFalse(map.containsKey(1));
        assertTrue(map.containsValue("1"));
        assertFalse(map.containsValue("2"));
        assertFalse(map.containsValue(1));
    }

    @Test
    @DisplayName("✅ Успешно: containsKey и containsValue возвращают false для пустой RedisMap")
    void containsKeyAndContainsValueReturnFalseForEmptyMap() {
        assertFalse(map.containsKey("one"));
        assertFalse(map.containsValue("1"));
    }

    @Test
    @DisplayName("✅ Успешно: get, put и remove работают по контракту Map")
    void getPutAndRemoveFollowMapContract() {
        assertNull(map.put("one", "1"));
        assertEquals("1", map.get("one"));
        assertEquals("1", map.put("one", "11"));
        assertEquals("11", map.get("one"));
        assertNull(map.get(123));
        assertNull(map.remove(123));
        assertEquals("11", map.remove("one"));
        assertNull(map.remove("one"));
    }

    @Test
    @DisplayName("✅ Успешно: putAll записывает все пары ключ-значение в Redis")
    void putAllWritesAllEntries() {
        map.putAll(Map.of("one", "1", "two", "2"));

        assertEquals(2, map.size());
        assertEquals("1", map.get("one"));
        assertEquals("2", map.get("two"));
    }

    @Test
    @DisplayName("✅ Успешно: clear полностью очищает Redis hash")
    void clearRemovesRedisHash() {
        map.put("one", "1");
        map.put("two", "2");

        map.clear();

        assertTrue(map.isEmpty());
        assertFalse(jedis.exists(REDIS_MAP_KEY));
    }

    @Test
    @DisplayName("✅ Успешно: keySet возвращает актуальный набор ключей")
    void keySetReturnsCurrentKeys() {
        map.putAll(Map.of("one", "1", "two", "2"));

        assertEquals(Set.of("one", "two"), map.keySet());
    }

    @Test
    @DisplayName("✅ Успешно: values возвращает актуальный набор значений")
    void valuesReturnCurrentValues() {
        map.putAll(Map.of("one", "1", "two", "2"));

        assertEquals(Set.of("1", "2"), Set.copyOf(map.values()));
    }

    @Test
    @DisplayName("✅ Успешно: values пустой RedisMap возвращает пустую коллекцию")
    void valuesReturnEmptyCollectionForEmptyMap() {
        assertTrue(map.values().isEmpty());
    }

    @Test
    @DisplayName("✅ Успешно: entrySet возвращает актуальный набор записей Map.Entry")
    void entrySetReturnsCurrentEntries() {
        map.putAll(Map.of("one", "1", "two", "2"));

        assertEquals(
                Set.of(Map.entry("one", "1"), Map.entry("two", "2")),
                map.entrySet()
        );
    }

    @Test
    @DisplayName("✅ Успешно: entrySet пустой RedisMap возвращает пустой набор")
    void entrySetReturnsEmptySetForEmptyMap() {
        assertTrue(map.entrySet().isEmpty());
    }

    @Test
    @DisplayName("❌ Исключение: конструктор, put и putAll не принимают null")
    void constructorAndPutRejectNulls() {
        assertThrows(NullPointerException.class, () -> new RedisMap(null, REDIS_MAP_KEY));
        assertThrows(NullPointerException.class, () -> new RedisMap(jedis, null));
        assertThrows(NullPointerException.class, () -> map.put(null, "1"));
        assertThrows(NullPointerException.class, () -> map.put("one", null));
        java.util.HashMap<String, String> invalid = new java.util.HashMap<>();
        invalid.put("one", null);
        assertThrows(NullPointerException.class, () -> map.putAll(invalid));
    }

    @Test
    @DisplayName("❌ Исключение: values возвращает неизменяемую коллекцию")
    void valuesAreUnmodifiable() {
        map.put("one", "1");

        assertThrows(UnsupportedOperationException.class, () -> map.values().add("2"));
    }

    @Test
    @DisplayName("❌ Исключение: entrySet возвращает неизменяемый набор")
    void entrySetIsUnmodifiable() {
        map.put("one", "1");

        assertThrows(UnsupportedOperationException.class,
                () -> map.entrySet().add(Map.entry("two", "2")));
    }

    @Test
    @DisplayName("❌ Исключение: putAll выбрасывает NullPointerException, если передана null Map")
    void putAllRejectsNullMap() {
        assertThrows(NullPointerException.class, () -> map.putAll(null));
    }

    @Test
    @DisplayName("❌ Исключение: putAll выбрасывает NullPointerException, если Map содержит null-ключ")
    void putAllRejectsMapWithNullKey() {
        HashMap<String, String> invalid = new HashMap<>();
        invalid.put(null, "1");

        assertThrows(NullPointerException.class, () -> map.putAll(invalid));
    }
}
