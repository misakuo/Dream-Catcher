package me.moxun.dreamcatcher.wrapper;

import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarResponse;

import me.moxun.dreamcatcher.connector.reporter.NetworkEventReporter;

/**
 * Created by moxun on 16/12/8.
 */

public class DCResponse extends DCHeader implements NetworkEventReporter.InspectorResponse {

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
}
