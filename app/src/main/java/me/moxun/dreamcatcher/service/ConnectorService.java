package me.moxun.dreamcatcher.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import me.moxun.dreamcatcher.connector.Connector;

/**
 * Created by moxun on 16/12/12.
 */

public class ConnectorService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Connector.open(getApplication());
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Connector.close();
    }
}
