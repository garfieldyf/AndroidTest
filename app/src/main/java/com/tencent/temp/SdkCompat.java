package com.tencent.temp;

import android.annotation.TargetApi;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import java.util.Map;
import java.util.Objects;

public final class SdkCompat {
    /**
     * @see Options#inPreferredConfig
     * @see Options#outConfig
     */
    public static Config getConfig(Options opts) {
        return IMPL.getConfig(opts);
    }

    /**
     * @see Map#replace(Object, Object)
     */
    public static <K, V> V replace(Map<K, V> map, K key, V value) {
        return IMPL.replace(map, key, value);
    }

    /**
     * @see Map#putIfAbsent(Object, Object)
     */
    public static <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
        return IMPL.putIfAbsent(map, key, value);
    }

    /**
     * @see Map#remove(Object, Object)
     */
    public static boolean remove(Map<?, ?> map, Object key, Object value) {
        return IMPL.remove(map, key, value);
    }

    /**
     * @see Map#getOrDefault(Object, Object)
     */
    public static <V> V getOrDefault(Map<?, V> map, Object key, V defaultValue) {
        return IMPL.getOrDefault(map, key, defaultValue);
    }

    /**
     * @see Map#replace(Object, Object, Object)
     */
    public static <K, V> boolean replace(Map<K, V> map, K key, V oldValue, V newValue) {
        return IMPL.replace(map, key, oldValue, newValue);
    }

    /* package */ static class SdkCompatImpl {
        public Config getConfig(Options opts) {
            return opts.inPreferredConfig;
        }

        public <K, V> V replace(Map<K, V> map, K key, V value) {
            V result = map.get(key);
            if (result != null || map.containsKey(key)) {
                result = map.put(key, value);
            }

            return result;
        }

        public <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
            final V result = map.get(key);
            return (result != null ? result : map.put(key, value));
        }

        public boolean remove(Map<?, ?> map, Object key, Object value) {
            final Object mappedValue = map.get(key);
            if (!Objects.equals(mappedValue, value) || (mappedValue == null && !map.containsKey(key))) {
                return false;
            }

            map.remove(key);
            return true;
        }

        public <V> V getOrDefault(Map<?, V> map, Object key, V defaultValue) {
            final V result = map.get(key);
            return (result != null || map.containsKey(key) ? result : defaultValue);
        }

        public <K, V> boolean replace(Map<K, V> map, K key, V oldValue, V newValue) {
            final Object mappedValue = map.get(key);
            if (!Objects.equals(mappedValue, oldValue) || (mappedValue == null && !map.containsKey(key))) {
                return false;
            }

            map.put(key, newValue);
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    /* package */ static class SdkCompatImpl24 extends SdkCompatImpl {
        @Override
        public <K, V> V replace(Map<K, V> map, K key, V value) {
            return map.replace(key, value);
        }

        @Override
        public <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
            return map.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(Map<?, ?> map, Object key, Object value) {
            return map.remove(key, value);
        }

        @Override
        public <V> V getOrDefault(Map<?, V> map, Object key, V defaultValue) {
            return map.getOrDefault(key, defaultValue);
        }

        @Override
        public <K, V> boolean replace(Map<K, V> map, K key, V oldValue, V newValue) {
            return map.replace(key, oldValue, newValue);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    /* package */ static final class SdkCompatImpl26 extends SdkCompatImpl24 {
        @Override
        public Config getConfig(Options opts) {
            return (opts.outConfig != null ? opts.outConfig : opts.inPreferredConfig);
        }
    }

    private static final SdkCompatImpl IMPL;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            IMPL = new SdkCompatImpl26();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            IMPL = new SdkCompatImpl24();
        } else {
            IMPL = new SdkCompatImpl();
        }
    }
}
