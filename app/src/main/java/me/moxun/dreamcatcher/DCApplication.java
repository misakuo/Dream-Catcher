package me.moxun.dreamcatcher;

import android.content.Intent;
import android.support.multidex.MultiDexApplication;

import me.moxun.dreamcatcher.service.ConnectorService;

/**
 * Created by moxun on 6/12/7.
 */

public class DCApplication extends MultiDexApplication {

    //forgive me...
    private int port = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, ConnectorService.class));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(new Intent(this, ConnectorService.class));
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
