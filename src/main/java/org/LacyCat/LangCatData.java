package org.LacyCat;

import java.util.*;

public class LangCatData {
    private final Map<String, Map<String, Object>> groups = new HashMap<>();
    public void addKey(String group, String key, Object value) {
        groups.putIfAbsent(group, new HashMap<>());
        groups.get(group).put(key, value);
    }

    public Object getValue(String key) {
        String[] parts = key.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Key must be in '<group>.<key>' format.");
        }

        Map<String, Object> group = groups.get(parts[0]);
        if (group == null) {
            return null;
        }

        return group.get(parts[1]);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> groupEntry : groups.entrySet()) {
            builder.append("$").append(groupEntry.getKey()).append("$:\n");

            for (Map.Entry<String, Object> keyEntry : groupEntry.getValue().entrySet()) {
                builder.append("    *").append(keyEntry.getKey()).append("* -> ").append(formatValue(keyEntry.getValue())).append("\n");
            }
        }

        return builder.toString();
    }

    private String formatValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof Double || value instanceof Integer) {
            return value.toString();
        } else if (value instanceof List) {
            return formatList((List<Object>) value);
        }
        return value.toString();
    }

    private String formatList(List<Object> list) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < list.size(); i++) {
            builder.append(formatValue(list.get(i)));
            if (i < list.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
