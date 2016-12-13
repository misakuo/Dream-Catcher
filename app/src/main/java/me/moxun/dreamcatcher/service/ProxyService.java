package me.moxun.dreamcatcher.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.proxy.CaptureType;

import org.greenrobot.eventbus.EventBus;

import me.moxun.dreamcatcher.DCApplication;
import me.moxun.dreamcatcher.connector.manager.SimpleConnectorLifecycleManager;
import me.moxun.dreamcatcher.event.CaptureEvent;
import me.moxun.dreamcatcher.event.OperateEvent;

/**
 * Created by moxun on 16/12/9.
 */

public class ProxyService extends Service {
    private BrowserMobProxy proxy = new BrowserMobProxyServer();

    @Override
    public void onDestroy() {
        super.onDestroy();
        proxy.stop();
        postEvent("Stop monitoring");
        SimpleConnectorLifecycleManager.setProxyEnabled(false);
        EventBus.getDefault().post(new OperateEvent(OperateEvent.TARGET_PROXY, false));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                postEvent("Prepare to turn on proxy……");
                proxy.setTrustAllServers(true);
                try {
                    proxy.start(9999);
                } catch (Exception e) {
                    proxy.start();
                }
                ((DCApplication) getApplication()).setPort(proxy.getPort());
                postEvent("Proxy is bound to port " + proxy.getPort());
                proxy.enableHarCaptureTypes(CaptureType.REQUEST_HEADERS, CaptureType.REQUEST_COOKIES,
                        CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_HEADERS, CaptureType.RESPONSE_COOKIES,
                        CaptureType.RESPONSE_CONTENT, CaptureType.RESPONSE_BINARY_CONTENT, CaptureType.REQUEST_BINARY_CONTENT);
                Log.e("ProxyService", "Serve on port: " + proxy.getPort());
                postEvent("Start monitoring");
                proxy.newHar();
                EventBus.getDefault().post(new OperateEvent(OperateEvent.TARGET_PROXY, true));
                SimpleConnectorLifecycleManager.setProxyEnabled(true);
                Log.e("ProxyService", "Start monitoring");
                return Boolean.TRUE;
            }
        }.execute();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void postEvent(String s) {
        CaptureEvent.send(s);
    }
}
