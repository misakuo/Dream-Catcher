package me.moxun.dreamcatcher.wrapper;

import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarRequest;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nullable;

import me.moxun.dreamcatcher.connector.reporter.NetworkEventReporter;
import me.moxun.dreamcatcher.connector.reporter.RequestBodyHelper;

/**
 * Created by moxun on 16/12/8.
 */

public class DCRequest extends DCHeader implements NetworkEventReporter.InspectorRequest {

    private HarEntry harEntry;
    private RequestBodyHelper helper;

    public void attachBodyHelper(RequestBodyHelper helper) {
        this.helper = helper;
    }

    public DCRequest(HarEntry entry) {
        this.harEntry = entry;
    }

    public HarRequest getRequest() {
        return harEntry.getRequest();
    }

    @Override
    public String id() {
        return harEntry.getId();
    }

    @Override
    public String friendlyName() {
        return null;
    }

    @Nullable
    @Override
    public Integer friendlyNameExtra() {
        return null;
    }

    @Override
    public String url() {
        return harEntry.getRequest().getUrl();
    }

    @Override
    public String method() {
        return harEntry.getRequest().getMethod();
    }

    @Nullable
    @Override
    public byte[] body() throws IOException {
        byte[] body =  harEntry.getRequest().getContent().getBinaryContent();

        if (body != null) {
            OutputStream out = helper.createBodySink(firstHeaderValue("Content-Encoding"));
            try {
                out.write(body);
            } finally {
                out.close();
            }
            return helper.getDisplayBody();
        } else {
            return null;
        }
    }
}
