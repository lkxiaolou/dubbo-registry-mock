package org.newboo.utils;

import java.util.ArrayList;
import java.util.List;

public class LinesUtil {

    private static final String VERSION_LINE_PREFIX = "#version=";
    private static final String SKIP_LINE_PREFIX = "#";

    public static String buildLine(String line, boolean firstLine) {
        return firstLine ? line : "\n" + line;
    }

    public static boolean isSkipLine(String line) {
        if (line == null) {
            return true;
        }
        return line.trim().toLowerCase().startsWith(SKIP_LINE_PREFIX);
    }

    public static boolean isVersionLine(String line) {
        if (line == null) {
            return false;
        }
        return line.trim().toLowerCase().startsWith(VERSION_LINE_PREFIX);
    }

    public static String getVersionFromLine(String line) {
        if (!isVersionLine(line)) {
            return "";
        }
        return line.trim().toLowerCase().substring(VERSION_LINE_PREFIX.length());
    }

    public static String buildVersionLine(String version) {
        return VERSION_LINE_PREFIX + version;
    }

    public static String calLinesVersion(List<String> lines) {
        if (lines == null || lines.size() <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.length() > 0 && !isVersionLine(line)) {
                sb.append(line);
            }
            sb.append(buildLine(line, i == 0));
        }
        if (sb.length() == 0) {
            return "";
        }
        return Md5Util.getMD5(sb.toString());
    }

    public static List<String> replaceLinesVersion(List<String> lines, String version) {
        List<String> newLines = new ArrayList<>(lines.size());
        newLines.add(buildVersionLine(version));
        for (String line : lines) {
            if (!isVersionLine(line)) {
                newLines.add(line);
            }
        }
        return newLines;
    }

    public static List<String> removeLine(List<String> lines, String line) {
        List<String> newLines = new ArrayList<>(lines.size());
        for (String tmp : lines) {
            if (!isVersionLine(line) && !tmp.equals(line)) {
                newLines.add(line);
            }
        }
        return newLines;
    }
}
