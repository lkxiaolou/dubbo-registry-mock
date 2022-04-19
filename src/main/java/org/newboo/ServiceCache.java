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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServiceCache {

    private final List<LineParser> formatters;

    private static final Logger logger = LoggerFactory.getLogger(ServiceCache.class);

    // 缓存，key为服务名
    private Map<String, Set<URL>> cache;

    // cache的读写锁
    private final ReadWriteLock cacheReadLock;

    // 文件路径中心
    private final PathCenter pathCenter;

    // 版本中心
    private final VersionCenter versionCenter;

    public ServiceCache(String discoverDir) {
        this.pathCenter = new PathCenter(discoverDir);
        this.versionCenter = new VersionCenter();
        this.formatters = buildFormatters();
        this.cache = new HashMap<>();
        this.cacheReadLock = new ReentrantReadWriteLock();
    }

    /**
     * @param service 服务名
     * @return 是否有变更
     */
    public boolean resetCache(String service) {
        List<String> lines = FileUtil.readLines(this.pathCenter.getServicePath(service));
        Map<String, Set<URL>> newCache = new HashMap<>();
        String version = "";

        for (String line : lines) {
            // 解析version
            if (LinesUtil.isVersionLine(line)) {
                version = LinesUtil.getVersionFromLine(line);
                continue;
            }

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

        // 更新缓存，只有version变化时更新
        if (!versionCenter.getVersion(service).equals(version)) {
            // 更新缓存
            this.cacheReadLock.writeLock().lock();
            this.cache = newCache;
            this.cacheReadLock.writeLock().unlock();

            // 更新version
            this.versionCenter.setVersion(service, version);

            return true;
        }

        return false;
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

        // 计算version
        List<String> lines = FileUtil.readLines(fileName);
        String version = LinesUtil.calLinesVersion(lines);
        lines = LinesUtil.replaceLinesVersion(lines, version);

        // 写入version
        FileUtil.writeLines(fileName, lines);
    }

    public void removeUrl(URL url) throws IOException {
        String fileName = pathCenter.getServicePath(url.getServiceInterface());
        String line = url.toFullString();

        List<String> lines = FileUtil.readLines(fileName);
        lines = LinesUtil.removeLine(lines, line);

        // 计算version
        String version = LinesUtil.calLinesVersion(lines);
        lines = LinesUtil.replaceLinesVersion(lines, version);

        // 写入version
        FileUtil.writeLines(fileName, lines);
    }

    public List<URL> getUrls(String service) throws Exception {
        if (!resetCache(service)) {
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
