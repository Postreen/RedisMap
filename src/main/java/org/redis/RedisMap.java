package org.redis;


import redis.clients.jedis.JedisPooled;

import java.util.*;

public class RedisMap extends AbstractMap<String, String> {
    private final JedisPooled jedis;
    private final String mapKey;

    public RedisMap(JedisPooled jedis, String mapKey) {
        this.jedis = Objects.requireNonNull(jedis, "jedis must not be null");
        this.mapKey = Objects.requireNonNull(mapKey, "mapKey must not be null");
    }

    @Override
    public int size() {
        return Math.toIntExact(jedis.hlen(mapKey));
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String value && jedis.hexists(mapKey, value);
    }

    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof String stringValue)) {
            return false;
        }
        return jedis.hvals(mapKey).contains(stringValue);
    }

    @Override
    public String get(Object key) {
        if (!(key instanceof String stringKey)) {
            return null;
        }
        return jedis.hget(mapKey, stringKey);
    }

    @Override
    public String put(String key, String value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");

        String previous = jedis.hget(mapKey, key);
        jedis.hset(mapKey, key, value);
        return previous;
    }

    @Override
    public String remove(Object key) {
        if (!(key instanceof String stringKey)) {
            return null;
        }
        String previous = jedis.hget(mapKey, stringKey);
        if (previous != null) {
            jedis.hdel(mapKey, stringKey);
        }
        return previous;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        Objects.requireNonNull(m, "map must not be null");
        if (m.isEmpty()) {
            return;
        }

        Map<String, String> copy = new HashMap<>();
        for (Map.Entry<? extends String, ? extends String> entry : m.entrySet()) {
            String key = Objects.requireNonNull(entry.getKey(), "map contains null key");
            String value = Objects.requireNonNull(entry.getValue(), "map contains null value");
            copy.put(key, value);
        }

        jedis.hset(mapKey, copy);
    }

    @Override
    public void clear() {
        jedis.del(mapKey);
    }

    @Override
    public Set<String> keySet() {
        return new AbstractSet<>() {
            @Override
            public java.util.Iterator<String> iterator() {
                return snapshotKeys().iterator();
            }

            @Override
            public int size() {
                return RedisMap.this.size();
            }

            @Override
            public boolean contains(Object o) {
                return RedisMap.this.containsKey(o);
            }
        };
    }

    @Override
    public Collection<String> values() {
        return new AbstractCollection<>() {
            @Override
            public java.util.Iterator<String> iterator() {
                return snapshotValues().iterator();
            }

            @Override
            public int size() {
                return RedisMap.this.size();
            }

            @Override
            public boolean contains(Object o) {
                return RedisMap.this.containsValue(o);
            }
        };
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return new AbstractSet<>() {
            @Override
            public java.util.Iterator<Entry<String, String>> iterator() {
                return snapshotEntries().iterator();
            }

            @Override
            public int size() {
                return RedisMap.this.size();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Entry<?, ?> entry)) {
                    return false;
                }
                Object key = entry.getKey();
                Object value = entry.getValue();
                return key instanceof String stringKey
                        && value instanceof String stringValue
                        && Objects.equals(RedisMap.this.get(stringKey), stringValue);
            }
        };
    }

    private Set<String> snapshotKeys() {
        return Set.copyOf(jedis.hkeys(mapKey));
    }

    private List<String> snapshotValues() {
        return List.copyOf(jedis.hvals(mapKey));
    }

    private Set<Entry<String, String>> snapshotEntries() {
        Set<Entry<String, String>> entries = new HashSet<>();
        jedis.hgetAll(mapKey).forEach((key, value) -> entries.add(new SimpleEntry<>(key, value)));
        return Collections.unmodifiableSet(entries);
    }
}
