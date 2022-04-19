package org.newboo.parser;

import org.apache.dubbo.common.URL;

public class URLLineParser implements LineParser {

    @Override
    public URL parse(String line) throws LineParseException {
        return URL.valueOf(line);
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public boolean inCharge(String line) {
        return line != null && line.startsWith("dubbo://");
    }
}
