package me.moxun.dreamcatcher.connector.server;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Credentials;
import android.net.LocalSocket;
import android.util.Log;

import java.io.IOException;

import me.moxun.dreamcatcher.connector.util.LogUtil;

public abstract class SecureSocketHandler implements SocketHandler {
  private final Context mContext;

  public SecureSocketHandler(Context context) {
    mContext = context;
  }

  @Override
  public final void onAccepted(LocalSocket socket) throws IOException {
    try {
      enforcePermission(mContext, socket);
      onSecured(socket);
    } catch (PeerAuthorizationException e) {
      LogUtil.e("Unauthorized request: " + e.getMessage());
    }
  }

  protected abstract void onSecured(LocalSocket socket) throws IOException;

  private static void enforcePermission(Context context, LocalSocket peer)
      throws IOException, PeerAuthorizationException {
    Credentials credentials = peer.getPeerCredentials();

    int uid = credentials.getUid();
    int pid = credentials.getPid();

    if (LogUtil.isLoggable(Log.VERBOSE)) {
      LogUtil.v("Got request from uid=%d, pid=%d", uid, pid);
    }

    String requiredPermission = Manifest.permission.DUMP;
    int checkResult = context.checkPermission(requiredPermission, pid, uid);
    if (checkResult != PackageManager.PERMISSION_GRANTED) {
      throw new PeerAuthorizationException(
          "Peer pid=" + pid + ", uid=" + uid + " does not have " + requiredPermission);
    }
  }
}
