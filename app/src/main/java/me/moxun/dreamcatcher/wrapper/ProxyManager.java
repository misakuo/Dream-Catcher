package me.moxun.dreamcatcher.wrapper;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import me.moxun.dreamcatcher.connector.reporter.DefaultResponseHandler;
import me.moxun.dreamcatcher.connector.reporter.NetworkEventReporter;
import me.moxun.dreamcatcher.connector.reporter.NetworkEventReporterImpl;
import me.moxun.dreamcatcher.connector.reporter.RequestBodyHelper;
import me.moxun.dreamcatcher.event.CaptureEvent;

/**
 * Created by moxun on 16/12/8.
 */

public class ProxyManager {
    private static final AtomicInteger sSequenceNumberGenerator = new AtomicInteger(0);
    private final NetworkEventReporter DCHook = NetworkEventReporterImpl.get();
    private final int mRequestId;

    @Nullable
    private String mRequestIdString;

    @Nullable
    private RequestBodyHelper mRequestBodyHelper;

    private ProxyManager() {
        mRequestId = sSequenceNumberGenerator.getAndIncrement();
        mRequestBodyHelper = new RequestBodyHelper(DCHook, getDCRequestId());
        CaptureEvent.send("Capture request with id " + mRequestId);
    }

    public RequestBodyHelper getRequestBodyHelper() {
        return mRequestBodyHelper;
    }

    public static ProxyManager newInstance() {
        return new ProxyManager();
    }


    public void requestWillBeSent(DCRequest request) {
        Log.e("Manager", "requestWillBeSent " + request.id());
        DCHook.requestWillBeSent(request);
    }

    public void responseHeadersReceived(DCResponse response) {
        DCHook.responseHeadersReceived(response);
    }

    public void httpExchangeFailed(String errorText) {
        DCHook.httpExchangeFailed(getDCRequestId(), errorText);
    }

    public void interpretResponseStream(DCResponse response) {
        InputStream inputStream = new ByteArrayInputStream(response.getResponse().getContent().getBinaryContent());
        inputStream = DCHook.interpretResponseStream(getDCRequestId(),
                response.getContentType(),
                null,
                inputStream,
                new DefaultResponseHandler(DCHook,getDCRequestId()));
        try {
            read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        responseReadFinished();
    }

    private byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        byte[] result = out.toByteArray();
        out.flush();
        out.close();
        return result;
    }

    public void responseReadFailed(String errorText) {
        httpExchangeFailed(errorText);
    }

    public void responseReadFinished() {
        DCHook.responseReadFinished(getDCRequestId());
    }

    public void dataSent(DCRequest request) {
        DCHook.dataSent(getDCRequestId(), (int) request.getRequest().getBodySize(), 0);
        mRequestBodyHelper.reportDataSent();
    }

    public void dataReceived(int bodySize) {
        //DCHook.dataReceived(getDCRequestId(), bodySize, 0);
    }

    public String getDCRequestId() {
        if (mRequestIdString == null) {
            mRequestIdString = String.valueOf(mRequestId);
        }
        return mRequestIdString;
    }
}
