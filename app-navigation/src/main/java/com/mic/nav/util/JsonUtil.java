package com.mic.nav.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import java.util.HashSet;  // 添加这个导入
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON 转换工具类 (使用 Gson)
 * 提供 JSON 字符串与 Map 之间的转换功能
 */
public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private static final Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
    private static final Type listType = new TypeToken<List<Object>>(){}.getType();

    /**
     * 将 JSON 字符串转换为 Map
     * @param jsonString JSON 字符串
     * @return 转换后的 Map 对象
     */
    public static Map<String, Object> jsonToMap(String jsonString) {
        try {
            if (jsonString == null || jsonString.trim().isEmpty()) {
                return new HashMap<>();
            }
            return gson.fromJson(jsonString, mapType);
        } catch (Exception e) {
            System.err.println("JSON 转 Map 失败: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 将 Map 转换为 JSON 字符串
     * @param map 要转换的 Map 对象
     * @return 格式化后的 JSON 字符串
     */
    public static String mapToJson(Map<String, Object> map) {
        try {
            if (map == null) {
                return "{}";
            }
            Gson gson = new GsonBuilder().create();
            return gson.toJson(map);
        } catch (Exception e) {
            System.err.println("Map 转 JSON 失败: " + e.getMessage());
            return "{}";
        }
    }

    /**
     * 将 JSON 字符串转换为指定类型的对象
     * @param jsonString JSON 字符串
     * @param clazz 目标类型
     * @return 转换后的对象，失败返回 null
     */
    public static <T> T jsonToObject(String jsonString, Class<T> clazz) {
        try {
            if (jsonString == null || jsonString.trim().isEmpty()) {
                return null;
            }
            return gson.fromJson(jsonString, clazz);
        } catch (Exception e) {
            System.err.println("JSON 转对象失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将对象转换为 JSON 字符串
     * @param obj 要转换的对象
     * @return 格式化后的 JSON 字符串
     */
    public static String objectToJson(Object obj) {
        try {
            if (obj == null) {
                return "{}";
            }
            return gson.toJson(obj);
        } catch (Exception e) {
            System.err.println("对象转 JSON 失败: " + e.getMessage());
            return "{}";
        }
    }

    /**
     * 从 Map 中安全地获取字符串值
     */
    public static String getString(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    public static String getString(Map<String, Object> map, String key) {
        return getString(map, key, "");
    }

    /**
     * 从 Map 中安全地获取整数值
     */
    public static int getInt(Map<String, Object> map, String key, int defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static int getInt(Map<String, Object> map, String key) {
        return getInt(map, key, 0);
    }

    /**
     * 从 Map 中安全地获取长整数值
     */
    public static long getLong(Map<String, Object> map, String key, long defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static long getLong(Map<String, Object> map, String key) {
        return getLong(map, key, 0L);
    }

    /**
     * 从 Map 中安全地获取双精度浮点数值
     */
    public static double getDouble(Map<String, Object> map, String key, double defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static double getDouble(Map<String, Object> map, String key) {
        return getDouble(map, key, 0.0);
    }

    /**
     * 从 Map 中安全地获取布尔值
     */
    public static boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return defaultValue;
    }

    public static boolean getBoolean(Map<String, Object> map, String key) {
        return getBoolean(map, key, false);
    }

    /**
     * 从 Map 中安全地获取列表
     */
    @SuppressWarnings("unchecked")
    public static List<Object> getList(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return new ArrayList<>();
        }
        Object value = map.get(key);
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return new ArrayList<>();
    }

    /**
     * 从 Map 中安全地获取 Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return new HashMap<>();
        }
        Object value = map.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return new HashMap<>();
    }

    /**
     * 检查 Map 中是否包含指定键
     */
    public static boolean containsKey(Map<String, Object> map, String key) {
        return map != null && map.containsKey(key);
    }

    /**
     * 获取 Map 的所有键
     */
    public static Set<String> getKeys(Map<String, Object> map) {
        return map != null ? map.keySet() : new HashSet<>();
    }

    /**
     * 获取 Map 的大小
     */
    public static int getSize(Map<String, Object> map) {
        return map != null ? map.size() : 0;
    }

    /**
     * 判断 JSON 字符串是否有效
     */
    public static boolean isValidJson(String jsonString) {
        try {
            JsonParser.parseString(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 格式化 JSON 字符串
     */
    public static String formatJson(String jsonString) {
        try {
            JsonElement element = JsonParser.parseString(jsonString);
            return gson.toJson(element);
        } catch (Exception e) {
            return jsonString;
        }
    }

    /**
     * 合并两个 Map
     */
    public static Map<String, Object> mergeMaps(Map<String, Object> map1, Map<String, Object> map2) {
        Map<String, Object> result = new HashMap<>();
        if (map1 != null) {
            result.putAll(map1);
        }
        if (map2 != null) {
            result.putAll(map2);
        }
        return result;
    }

    /**
     * 打印 Map 内容（用于调试）
     */
    public static void printMap(Map<String, Object> map) {
        printMap(map, "", true);
    }

    private static void printMap(Map<String, Object> map, String prefix, boolean isRoot) {
        if (map == null) {
            System.out.println(prefix + "null");
            return;
        }

        if (!isRoot) {
            System.out.println(prefix + "{");
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String newPrefix = isRoot ? "" : prefix + "  ";

            if (value instanceof Map) {
                System.out.println(newPrefix + key + ": {");
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                printMap(nestedMap, newPrefix + "  ", false);
                System.out.println(newPrefix + "}");
            } else if (value instanceof List) {
                System.out.println(newPrefix + key + ": [");
                List<?> list = (List<?>) value;
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    if (item instanceof Map) {
                        System.out.println(newPrefix + "  [" + i + "]: {");
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        printMap(itemMap, newPrefix + "    ", false);
                        System.out.println(newPrefix + "  }");
                    } else {
                        System.out.println(newPrefix + "  [" + i + "]: " + item);
                    }
                }
                System.out.println(newPrefix + "]");
            } else {
                System.out.println(newPrefix + key + ": " + value);
            }
        }

        if (!isRoot) {
            System.out.println(prefix + "}");
        }
    }
}