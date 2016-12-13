package me.moxun.dreamcatcher.connector.server;


import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import me.moxun.dreamcatcher.connector.manager.Lifecycle;
import me.moxun.dreamcatcher.connector.manager.SimpleConnectorLifecycleManager;
import me.moxun.dreamcatcher.connector.util.IServerManager;
import me.moxun.dreamcatcher.connector.util.LogUtil;
import me.moxun.dreamcatcher.event.CaptureEvent;
import me.moxun.dreamcatcher.event.OperateEvent;

public class ServerManager implements IServerManager {
  private static final String THREAD_PREFIX = "DreamCatcherListener";
  private final LocalSocketServer mServer;

  private volatile boolean mStarted;
  private Thread listenerThread;

  public ServerManager(LocalSocketServer server) {
    mServer = server;
  }

  @Override
  public void start() {
    if (mStarted) {
      throw new IllegalStateException("Already started");
    }
    mStarted = true;
    mServer.reset();
    startServer(mServer);
  }

  @Override
  public void stop() {
    if (mStarted) {
      mServer.stop();
      mStarted = false;
    }
    EventBus.getDefault().post(new OperateEvent(OperateEvent.TARGET_CONNECTOR, false));
  }

  @Override
  public void restart() {
    stop();
    start();
  }

  private void startServer(final LocalSocketServer server) {
    Thread listener = new Thread(THREAD_PREFIX + "-" + server.getName()) {
      @Override
      public void run() {
        try {
          SimpleConnectorLifecycleManager.setCurrentState(Lifecycle.LOCAL_SERVER_SOCKET_OPENING);
          CaptureEvent.send("Local socket is open");
          EventBus.getDefault().post(new OperateEvent(OperateEvent.TARGET_CONNECTOR, true));
          server.run();
        } catch (IOException e) {
          LogUtil.e("Could not start DreamCatcher server: " + server.getName() +
                  ", cause: " + e.toString());
          SimpleConnectorLifecycleManager.setCurrentState(Lifecycle.SHUTDOWN);
          CaptureEvent.send("Exception on bind local socket");
          EventBus.getDefault().post(new OperateEvent(OperateEvent.TARGET_CONNECTOR, false, true, e.getMessage()));
        }
      }
    };
    listener.start();
    listenerThread = listener;
  }
}
