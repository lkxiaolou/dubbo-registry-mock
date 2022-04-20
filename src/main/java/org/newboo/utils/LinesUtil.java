package org.newboo.utils;

import org.apache.dubbo.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LinesUtil {

    private static final String SKIP_LINE_PREFIX = "#";

    public static String buildLine(String line, boolean firstLine) {
        return firstLine ? line : "\n" + line;
    }

    public static boolean isSkipLine(String line) {
        if (StringUtils.isEmpty(line)) {
            return true;
        }
        return line.trim().toLowerCase().startsWith(SKIP_LINE_PREFIX);
    }

    public static List<String> removeLine(List<String> lines, String line) {
        List<String> newLines = new ArrayList<>(lines.size());
        for (String tmp : lines) {
            if (!tmp.equals(line)) {
                newLines.add(line);
            }
        }
        return newLines;
    }
}
