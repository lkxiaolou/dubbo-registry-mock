package org.newboo;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.newboo.Center.PathCenter;
import org.newboo.Center.VersionCenter;
import org.newboo.parser.LineParseException;
import org.newboo.parser.LineParser;
import org.newboo.parser.ShortLineParser;
import org.newboo.parser.URLLineParser;
import org.newboo.utils.FileUtil;
import org.newboo.utils.LinesUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockService {

    private final List<LineParser> formatters;

    private static final Logger logger = LoggerFactory.getLogger(MockService.class);

    // 文件路径中心
    private final PathCenter pathCenter;

    // 版本中心
    private final VersionCenter versionCenter;

    public MockService(String discoverDir) {
        this.pathCenter = new PathCenter(discoverDir);
        this.versionCenter = new VersionCenter();
        this.formatters = buildFormatters();
    }

    /**
     * @param service 服务名
     * @return 是否有变更
     */
    public boolean scan(String service) {
        String fileName = this.pathCenter.getServicePath(service);
        String version = FileUtil.getFileLastModifyTime(fileName);

        // 未变更直接忽略
        if (versionCenter.getVersion(service).equals(version)) {
            return false;
        }

        List<String> lines = FileUtil.readLines(fileName);
        Map<String, Set<URL>> newCache = new HashMap<>();

        for (String line : lines) {
            // 注释跳过
            if (LinesUtil.isSkipLine(line)) {
                continue;
            }

            // 解析url
            try {
                URL url = format(line);
                newCache.putIfAbsent(url.getServiceInterface(), new HashSet<>());
                newCache.get(url.getServiceInterface()).add(url);
            } catch (Exception e) {
                logger.error("format url error", e);
            }
        }

        // 更新version
        this.versionCenter.setVersion(service, version);
        return true;
    }

    public static void main(String[] args) {
        List<LineParser> formatters = buildFormatters();
        System.out.println(formatters);
    }

    private URL format(String line) throws LineParseException {
        for (LineParser formatter : this.formatters) {
            if (formatter.inCharge(line)) {
                return formatter.parse(line);
            }
        }
        throw new LineParseException("formatter not found");
    }

    private static List<LineParser> buildFormatters() {
        List<LineParser> formatters = new ArrayList<>();
        formatters.add(new ShortLineParser());
        formatters.add(new URLLineParser());
        formatters.sort(Comparator.comparingInt(LineParser::order));
        return formatters;
    }

    public void writeUrl(URL url) throws IOException {
        String fileName = pathCenter.getServicePath(url.getServiceInterface());

        // 写入文件
        String line = url.toFullString();
        FileUtil.appendLine(fileName, line);
    }

    public void removeUrl(URL url) throws IOException {
        String fileName = pathCenter.getServicePath(url.getServiceInterface());
        String line = url.toFullString();

        List<String> lines = FileUtil.readLines(fileName);
        lines = LinesUtil.removeLine(lines, line);

        FileUtil.writeLines(fileName, lines);
    }

    public List<URL> getUrls(String service) throws Exception {
        if (!scan(service)) {
            throw new ServiceNotChangeException();
        }

        String fileName = pathCenter.getServicePath(service);
        List<String> lines = FileUtil.readLines(fileName);
        List<URL> urls = new ArrayList<>(lines.size());
        for (String line : lines) {
            if (!LinesUtil.isSkipLine(line)) {
                urls.add(format(line));
            }
        }
        return urls;
    }
}
