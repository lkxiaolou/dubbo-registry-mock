package org.newboo;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.NamedThreadFactory;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.apache.dubbo.rpc.RpcException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MockRegistry extends FailbackRegistry {

    private final MockService mockService;

    private static final String DISCOVERY_DEFAULT_DIR = "/tmp/mock-registry/";
    private static final String DISCOVERY_FILE_DIR_KEY = "discovery_file";

    public MockRegistry(URL url) {
        super(url);
        String basePath = DISCOVERY_DEFAULT_DIR;
        if (StringUtils.isNotEmpty(url.getParameter(DISCOVERY_FILE_DIR_KEY))) {
            basePath = url.getParameter(DISCOVERY_FILE_DIR_KEY);
        }

        mockService = new MockService(basePath);

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory("file_scan", true));
        scheduledExecutorService.scheduleWithFixedDelay(new SubscribeAgent(), 1000L, 5000, TimeUnit.MILLISECONDS);

    }

    @Override
    public void doRegister(URL url) {
        try {
            mockService.writeUrl(url);
        } catch (Throwable e) {
            throw new RpcException("Failed to register " + url, e);
        }
    }

    @Override
    public void doUnregister(URL url) {
        try {
            mockService.removeUrl(url);
        } catch (Throwable e) {
            throw new RpcException("Failed to unregister " + url, e);
        }
    }

    @Override
    public void doSubscribe(URL url, NotifyListener listener) {
        try {
            List<URL> urls = mockService.getUrls(url.getServiceInterface());
            listener.notify(urls);
        } catch (ServiceNotChangeException ignored) {
        } catch (Throwable e) {
            throw new RpcException("Failed to subscribe " + url, e);
        }
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public class SubscribeAgent implements Runnable {
        @Override
        public void run() {
            try {
                // 已经订阅的url
                Map<URL, Set<NotifyListener>> subscribeds = getSubscribed();
                if (subscribeds == null || subscribeds.isEmpty()) {
                    return;
                }

                for (Map.Entry<URL, Set<NotifyListener>> entry : subscribeds.entrySet()) {
                    for (NotifyListener listener : entry.getValue()) {
                        doSubscribe(entry.getKey(), listener);
                    }
                }
            } catch (Throwable t) {
                // ignore
            }
        }
    }
}
