package org.newboo.parser;

import org.apache.dubbo.common.URL;

public interface LineParser {

    // 解析一行
    URL parse(String line) throws LineParseException;

    // 执行顺序，越小越早执行
    int order();

    // 是否当前格式化
    boolean inCharge(String line);

}
