package me.moxun.dreamcatcher.wrapper;

import android.util.Pair;

import java.util.ArrayList;

import javax.annotation.Nullable;

import me.moxun.dreamcatcher.connector.reporter.NetworkEventReporter;

/**
 * Created by moxun on 16/12/8.
 */

public class DCHeader implements NetworkEventReporter.InspectorHeaders {

    private final ArrayList<Pair<String, String>> headers = new ArrayList<>();
    private String contentType = "text/plain";

    public void addHeader(String key, String value) {
        Pair<String, String> header = new Pair<>(key, value);
        headers.add(header);
        if (key.toLowerCase().equals("content-type")) {
            contentType = value;
        }
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public int headerCount() {
        return headers.size();
    }

    @Override
    public String headerName(int index) {
        return headers.get(index).first;
    }

    @Override
    public String headerValue(int index) {
        return headers.get(index).second;
    }

    @Nullable
    @Override
    public String firstHeaderValue(String name) {
        for (Pair<String, String> pair : headers) {
            if (pair.first.equals(name)) {
                return pair.second;
            }
        }
        return null;
    }
}
