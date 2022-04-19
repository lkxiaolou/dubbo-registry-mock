package org.newboo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static List<String> readLines(String fileName) {
        File file = new File(fileName);
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file),
                StandardCharsets.UTF_8))) {
            String tempString;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                lines.add(tempString);
            }
        } catch (Throwable ignore) {
            return lines;
        }

        return lines;
    }

    public static void appendLine(String fileName, String line) throws IOException {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.append(line);
        }
    }

    public static void writeLines(String fileName, List<String> lines) throws IOException {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            for (int i = 0; i < lines.size(); i++) {
                fileWriter.write(LinesUtil.buildLine(lines.get(i), i == 0));
            }
        }
    }

    public static String readAll(String fileName) {
        File file = new File(fileName);
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file),
                StandardCharsets.UTF_8))) {
            String tempString;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                content.append(tempString).append("\n");
            }
        } catch (Throwable ignore) {
            return "";
        }

        return content.toString().trim();
    }

    public static String readLine(String fileName) {
        File file = new File(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file),
                StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            return line == null || line.isEmpty()
                    ? null
                    : line;
        } catch (Throwable ignore) {
            return "";
        }
    }

}
