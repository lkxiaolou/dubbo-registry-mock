package org.newboo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
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

    public static String getFileLastModifyTime(String fileName) {
        Path path = Paths.get(fileName);
        try {
            return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis()+"";
        } catch (Exception e) {
            return "";
        }
    }

    public static void main(String[] args) {
        watchDir("/tmp/mock-registry");
    }

    public static void watchDir(String dir) {
        Path path = Paths.get(dir);
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        System.out.println("create...");
                    } else if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        System.out.println("modify...");
                    } else if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        System.out.println("delete...");
                    }
                }
            }
        } catch (Exception e) {
        }
    }

}
