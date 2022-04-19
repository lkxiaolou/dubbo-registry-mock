package org.newboo.Center;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VersionCenter {

    private final Map<String, String> versions;

    public VersionCenter() {
        versions = new ConcurrentHashMap<>();
    }

    public void setVersion(String service, String version) {
        versions.put(service, version);
    }

    public String getVersion(String service) {
        return versions.get(service) == null ? "" : versions.get(service);
    }
}
