package me.moxun.dreamcatcher.wrapper;

import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarResponse;
import net.lightbody.bmp.core.har.HarTimings;

import javax.annotation.Nullable;

import me.moxun.dreamcatcher.connector.inspector.protocol.module.Network;
import me.moxun.dreamcatcher.connector.reporter.NetworkEventReporter;

/**
 * Created by moxun on 16/12/8.
 */

public class DCResponse extends DCHeader implements NetworkEventReporter.InspectorResponse {

    void notifyResponse() {
        harEntry.responseFinish();
    }

    private HarEntry harEntry;

    public DCResponse(HarEntry entry) {
        this.harEntry = entry;
    }

    public HarResponse getResponse() {
        return harEntry.getResponse();
    }

    @Override
    public String requestId() {
        return harEntry.getId();
    }

    @Override
    public String url() {
        return harEntry.getRequest().getUrl();
    }

    @Override
    public int statusCode() {
        return harEntry.getResponse().getStatus();
    }

    @Override
    public String reasonPhrase() {
        return harEntry.getResponse().getStatusText();
    }

    @Override
    public boolean connectionReused() {
        return false;
    }

    @Override
    public int connectionId() {
        return 0;
    }

    @Override
    public boolean fromDiskCache() {
        return false;
    }

    @Nullable
    @Override
    public Network.ResourceTiming getTiming() {
        HarTimings timings = harEntry.getTimings();

        Network.ResourceTiming resourceTiming = new Network.ResourceTiming();
        resourceTiming.requestTime = harEntry.getRequestTime();
        resourceTiming.proxyStart = timings.getWait();
        resourceTiming.proxyEnd = resourceTiming.proxyStart;
        resourceTiming.dnsStart = resourceTiming.proxyEnd;
        resourceTiming.dnsEnd = resourceTiming.dnsStart + timings.getDns();
        resourceTiming.connectStart = resourceTiming.dnsEnd;
        resourceTiming.connectEnd = resourceTiming.connectStart + timings.getConnect();
        resourceTiming.sslStart = resourceTiming.connectEnd;
        resourceTiming.sslEnd = resourceTiming.sslStart + timings.getSsl();
        resourceTiming.sendStart = resourceTiming.sslEnd;
        resourceTiming.sendEnd = resourceTiming.sendStart + timings.getSend();
        resourceTiming.receiveHeadersEnd = harEntry.getTotalTime();
        return resourceTiming;
    }
}
