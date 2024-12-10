package org.LacyCat;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

public class LangCatParser {
    private static final Pattern GROUP_PATTERN = Pattern.compile("\\$(.*?)\\$:");
    private static final Pattern KEY_PATTERN = Pattern.compile("\\*(.*?)\\* -> (.+)");

    // 데이터 파싱
    public static LangCatData parse(String input) {
        LangCatData data = new LangCatData();
        Scanner scanner = new Scanner(input);

        String currentGroup = null;
        Set<String> groups = new HashSet<>();
        Map<String, Set<String>> keysPerGroup = new HashMap<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            // 그룹 인식
            Matcher groupMatcher = GROUP_PATTERN.matcher(line);
            if (groupMatcher.matches()) {
                currentGroup = groupMatcher.group(1);

                if (currentGroup == null || currentGroup.isEmpty()) {
                    throw new LangCatParseException("Group name cannot be empty.");
                }
                if (groups.contains(currentGroup)) {
                    throw new LangCatParseException("Duplicate group name detected: " + currentGroup);
                }
                groups.add(currentGroup);
                keysPerGroup.put(currentGroup, new HashSet<>());
                continue;
            }

            // 키-값 인식
            if (currentGroup != null) {
                Matcher keyMatcher = KEY_PATTERN.matcher(line);
                if (keyMatcher.matches()) {
                    String key = keyMatcher.group(1);
                    String rawValue = keyMatcher.group(2).trim();

                    if (key == null || key.isEmpty()) {
                        throw new LangCatParseException("Key name cannot be empty in group: " + currentGroup);
                    }
                    if (keysPerGroup.get(currentGroup).contains(key)) {
                        throw new LangCatParseException("Duplicate key name detected in group '" + currentGroup + "': " + key);
                    }
                    if (rawValue.isEmpty()) {
                        throw new LangCatParseException("Value for key '" + key + "' in group '" + currentGroup + "' cannot be empty.");
                    }

                    Object value = parseValue(rawValue);
                    data.addKey(currentGroup, key, value);
                    keysPerGroup.get(currentGroup).add(key);
                }
            }
        }
        return data;
    }

    // .lact 파일 저장
    public static void saveToFile(LangCatData data, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(data.toString());
        }
    }

    // .lact 파일 로드
    public static LangCatData loadFromFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(new File(filePath).toPath()));
        return parse(content);
    }

    // 파일명.그룹명.키명 형태로 값 로드
    public static Object loadValue(String filePath, String groupKey) throws IOException {
        String[] parts = groupKey.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("GroupKey must be in '<filename>.<group>.<key>' format.");
        }

        String fileName = parts[0] + ".lact";
        String group = parts[1];
        String key = parts[2];

        LangCatData data = loadFromFile(fileName);
        return data.getValue(group + "." + key);
    }

    private static Object parseValue(String rawValue) {
        if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
            return rawValue.substring(1, rawValue.length() - 1);
        } else if (rawValue.equals("True") || rawValue.equals("False")) {
            return Boolean.parseBoolean(rawValue);
        } else if (rawValue.contains(".")) {
            try {
                return Double.parseDouble(rawValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid float value: " + rawValue);
            }
        } else if (rawValue.startsWith("[") && rawValue.endsWith("]")) {
            return parseList(rawValue);
        } else {
            try {
                return Integer.parseInt(rawValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer value: " + rawValue);
            }
        }
    }

    private static List<Object> parseList(String rawValue) {
        rawValue = rawValue.substring(1, rawValue.length() - 1);
        String[] items = rawValue.split(",");
        List<Object> list = new ArrayList<>();
        for (String item : items) {
            list.add(parseValue(item.trim()));
        }
        return list;
    }
}
