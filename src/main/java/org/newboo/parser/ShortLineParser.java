package org.newboo.parser;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;

/**
 * 简化版行格式：
 * ${ip}:${port}:${service}:${version}:${group}
 * 其中${ip}:${port}:${service}必需，${version}:${group}可选
 */
public class ShortLineParser implements LineParser {

    @Override
    public URL parse(String line) throws LineParseException {
        if (line == null || line.length() == 0) {
            throw new LineParseException("empty");
        }
        String[] cats = line.split(":");
        if (cats.length < 3) {
            throw new LineParseException("error format");
        }
        URL url = new URL("dubbo", cats[0], Integer.parseInt(cats[1]), cats[2]);

        switch (cats.length) {
            case 3 : // ${ip}:${port}:${service}
                return url;
            case 4: // ${ip}:${port}:${service}:${version}
                return url.addParameter(CommonConstants.VERSION_KEY, cats[3]);
            case 5: // ${ip}:${port}:${service}:${version}:${group}
                url = url.addParameter(CommonConstants.VERSION_KEY, cats[3]);
                return url.addParameter(CommonConstants.GROUP_KEY, cats[4]);
            default:
                throw new LineParseException("error format " + line);
        }
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public boolean inCharge(String line) {
        return line != null && line.split(":").length >= 3;
    }
}
