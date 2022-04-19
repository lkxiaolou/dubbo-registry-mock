package org.newboo.Center;

import java.io.File;

public class PathCenter {

    private String pathDir;

    public PathCenter(String pathDir) {
        this.pathDir = pathDir;
    }

    public String getServicePath(String service) {

        File dir = new File(this.pathDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return this.pathDir + service;
    }
}
