package me.moxun.dreamcatcher.connector.inspector;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;

import me.moxun.dreamcatcher.connector.manager.Lifecycle;
import me.moxun.dreamcatcher.connector.manager.SimpleConnectorLifecycleManager;
import me.moxun.dreamcatcher.connector.server.SocketLike;
import me.moxun.dreamcatcher.connector.server.http.ExactPathMatcher;
import me.moxun.dreamcatcher.connector.server.http.HandlerRegistry;
import me.moxun.dreamcatcher.connector.server.http.HttpHandler;
import me.moxun.dreamcatcher.connector.server.http.HttpStatus;
import me.moxun.dreamcatcher.connector.server.http.LightHttpBody;
import me.moxun.dreamcatcher.connector.server.http.LightHttpRequest;
import me.moxun.dreamcatcher.connector.server.http.LightHttpResponse;
import me.moxun.dreamcatcher.connector.util.LogUtil;
import me.moxun.dreamcatcher.connector.util.ProcessUtil;
import me.moxun.dreamcatcher.event.CaptureEvent;

/**
 * Provides sufficient responses to convince Chrome's {@code chrome://inspect/devices} that we're
 * "one of them".  Note that we are being discovered automatically by the name of our socket
 * as defined in {@link android.net.LocalServerSocket}.  After discovery, we're required to provide
 * some context on how exactly to display and inspect what we have.
 */
public class ChromeDiscoveryHandler implements HttpHandler {
    private static final String PAGE_ID = "1";

    private static final String PATH_PAGE_LIST = "/json";
    private static final String PATH_VERSION = "/json/version";
    private static final String PATH_ACTIVATE = "/json/activate/" + PAGE_ID;
    private static final String PATH_SERVER_GET = "/json/remote";

    private static boolean INVALID = false;

    /**
     * Latest version of the WebKit Inspector UI that we've tested again (ideally).
     */
    private static final String WEBKIT_REV = "@188492";
    private static final String WEBKIT_VERSION = "537.36 (" + WEBKIT_REV + ")";

    private static final String USER_AGENT = "DreamCatcher";

    /**
     * Structured version of the WebKit Inspector protocol that we understand.
     */
    private static final String PROTOCOL_VERSION = "1.1";

    private final Context mContext;
    private final String mInspectorPath;

    @Nullable
    private LightHttpBody mVersionResponse;
    @Nullable
    private LightHttpBody mPageListResponse;

    public static void setInvalid(boolean invalid) {
        ChromeDiscoveryHandler.INVALID = invalid;
    }

    public ChromeDiscoveryHandler(Context context, String inspectorPath) {
        mContext = context;
        mInspectorPath = inspectorPath;
        SimpleConnectorLifecycleManager.setCurrentState(Lifecycle.CHROME_DISCOVERY_CONNECTED);
        CaptureEvent.send("Chrome discovery success");
    }

    public void register(HandlerRegistry registry) {
        registry.register(new ExactPathMatcher(PATH_PAGE_LIST), this);
        registry.register(new ExactPathMatcher(PATH_VERSION), this);
        registry.register(new ExactPathMatcher(PATH_ACTIVATE), this);
        registry.register(new ExactPathMatcher(PATH_SERVER_GET), this);
    }

    @Override
    public boolean handleRequest(SocketLike socket, LightHttpRequest request, LightHttpResponse response) {
        String path = request.uri.getPath();
        LogUtil.d(request.toString());
        try {
            if (PATH_VERSION.equals(path)) {
                handleVersion(response);
            } else if (PATH_PAGE_LIST.equals(path)) {
                handlePageList(response);
            } else if (PATH_ACTIVATE.equals(path)) {
                handleActivate(response);
            } else {
                response.code = HttpStatus.HTTP_NOT_IMPLEMENTED;
                response.reasonPhrase = "Not implemented";
                response.body = LightHttpBody.create("No support for " + path + "\n", "text/plain");
            }
        } catch (JSONException e) {
            response.code = HttpStatus.HTTP_INTERNAL_SERVER_ERROR;
            response.reasonPhrase = "Internal server error";
            response.body = LightHttpBody.create(e.toString() + "\n", "text/plain");
        }
        return true;
    }

    private void handleVersion(LightHttpResponse response)
            throws JSONException {
        if (mVersionResponse == null) {
            JSONObject reply = new JSONObject();
            reply.put("WebKit-Version", WEBKIT_VERSION);
            reply.put("User-Agent", USER_AGENT);
            reply.put("Protocol-Version", PROTOCOL_VERSION);
            reply.put("Browser", getAppLabelAndVersion());
            reply.put("Android-Package", mContext.getPackageName());
            mVersionResponse = LightHttpBody.create(reply.toString(), "application/json");
            LogUtil.d("Version: " + reply.toString());
        }
        setSuccessfulResponse(response, mVersionResponse);
    }

    private void handlePageList(LightHttpResponse response)
            throws JSONException {
        SimpleConnectorLifecycleManager.setCurrentState(Lifecycle.WAITING_FOR_WEBSOCKET);
        CaptureEvent.send("Waiting for websocket");
        if (mPageListResponse == null || INVALID) {
            JSONArray reply = new JSONArray();
            JSONObject page = new JSONObject();
            page.put("type", "app");
            page.put("title", makeTitle());
            page.put("id", PAGE_ID);
            page.put("description", "");

            page.put("webSocketDebuggerUrl", makeWSAddress());
            Uri chromeFrontendUrl = new Uri.Builder()
                    .scheme("http")
                    .authority("chrome-devtools-frontend.appspot.com")
                    .appendEncodedPath("serve_rev")
                    .appendEncodedPath(WEBKIT_REV)
                    .appendEncodedPath("devtools.html")
                    .appendQueryParameter("ws", mInspectorPath)
                    .build();
            page.put("devtoolsFrontendUrl", chromeFrontendUrl.toString());

            reply.put(page);
            mPageListResponse = LightHttpBody.create(reply.toString(), "application/json");
            LogUtil.w("PageList: " + reply.toString());
            INVALID = false;
        }
        setSuccessfulResponse(response, mPageListResponse);
    }

    private String makeWSAddress() {
        return "ws://" + mInspectorPath;
    }

    private String makeTitle() {
        StringBuilder b = new StringBuilder();
        b.append(getAppLabel());
        if (SimpleConnectorLifecycleManager.isProxyEnabled()) {
            b.append(" (Ready)");
        } else {
            b.append(" (Preparing ……)");
        }

        String processName = ProcessUtil.getProcessName();
        int colonIndex = processName.indexOf(':');
        if (colonIndex >= 0) {
            String nonDefaultProcessName = processName.substring(colonIndex);
            b.append(nonDefaultProcessName);
        }

        return b.toString();
    }

    private void handleActivate(LightHttpResponse response) {
        // Arbitrary response seem acceptable :)
        setSuccessfulResponse(
                response,
                LightHttpBody.create("Target activation ignored\n", "text/plain"));
    }

    private static void setSuccessfulResponse(
            LightHttpResponse response,
            LightHttpBody body) {
        response.code = HttpStatus.HTTP_OK;
        response.reasonPhrase = "OK";
        response.body = body;

        LogUtil.w(LogUtil.filter("ChromeDiscovery Response", response));
    }

    private String getAppLabelAndVersion() {
        StringBuilder b = new StringBuilder();
        PackageManager pm = mContext.getPackageManager();
        b.append(getAppLabel());
        b.append('/');
        try {
            PackageInfo info = pm.getPackageInfo(mContext.getPackageName(), 0 /* flags */);
            b.append(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return b.toString();
    }

    private CharSequence getAppLabel() {
        PackageManager pm = mContext.getPackageManager();
        return pm.getApplicationLabel(mContext.getApplicationInfo());
    }
}
