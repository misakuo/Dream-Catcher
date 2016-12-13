package me.moxun.dreamcatcher.connector;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import me.moxun.dreamcatcher.connector.inspector.DevtoolsSocketHandler;
import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsDomain;
import me.moxun.dreamcatcher.connector.inspector.protocol.module.FileSystem;
import me.moxun.dreamcatcher.connector.inspector.protocol.module.Network;
import me.moxun.dreamcatcher.connector.inspector.protocol.module.Profiler;
import me.moxun.dreamcatcher.connector.log.AELog;
import me.moxun.dreamcatcher.connector.manager.Lifecycle;
import me.moxun.dreamcatcher.connector.manager.SimpleConnectorLifecycleManager;
import me.moxun.dreamcatcher.connector.server.AddressNameHelper;
import me.moxun.dreamcatcher.connector.server.LazySocketHandler;
import me.moxun.dreamcatcher.connector.server.LocalSocketServer;
import me.moxun.dreamcatcher.connector.server.ProtocolDetectingSocketHandler;
import me.moxun.dreamcatcher.connector.server.ServerManager;
import me.moxun.dreamcatcher.connector.server.SocketHandler;
import me.moxun.dreamcatcher.connector.server.SocketHandlerFactory;
import me.moxun.dreamcatcher.connector.util.DreamCatcherCrashHandler;
import me.moxun.dreamcatcher.connector.util.LogUtil;
import me.moxun.dreamcatcher.connector.util.SocketServerManager;

/**
 * Created by moxun on 16/12/8.
 */

public class Connector {
    private static final String DEV_TOOLS_MAGIC_TAG = "_devtools_remote";

    private Connector() {

    }

    private static void initialize(final Initializer initializer) {
        loadInnerModule(initializer.mContext);
        initializer.start();
    }

    private static void loadInnerModule(Context context) {
        AELog.setLoggable(isDebuggable(context));
        setInternalLogEnabled(isDebuggable(context));

        if (!isDebuggable(context)) {
            AELog.setLoggable(false);
            setInternalLogEnabled(false);
        }
    }

    public static void open(final Context context) {
        initialize(new Initializer(context) {
            @Override
            protected Iterable<ChromeDevtoolsDomain> getInspectorModules() {
                return new DefaultInspectorModulesBuilder(context).finish();
            }
        });
    }

    public static void close() {
        SocketServerManager.stopServer();
        SimpleConnectorLifecycleManager.setCurrentState(Lifecycle.SHUTDOWN);
    }

    private static class PluginBuilder<T> {
        private final Set<String> mProvidedNames = new HashSet<>();
        private final Set<String> mRemovedNames = new HashSet<>();

        private final ArrayList<T> mPlugins = new ArrayList<>();

        private boolean mFinished;

        public void provide(String name, T plugin) {
            throwIfFinished();
            mPlugins.add(plugin);
            mProvidedNames.add(name);
        }

        public void provideIfDesired(String name, T plugin) {
            throwIfFinished();
            if (!mRemovedNames.contains(name)) {
                if (mProvidedNames.add(name)) {
                    mPlugins.add(plugin);
                }
            }
        }

        public void remove(String pluginName) {
            throwIfFinished();
            mRemovedNames.remove(pluginName);
        }

        private void throwIfFinished() {
            if (mFinished) {
                throw new IllegalStateException("Must not continue to build after finish()");
            }
        }

        public Iterable<T> finish() {
            mFinished = true;
            return mPlugins;
        }
    }

    private static final class DefaultInspectorModulesBuilder {
        private final Application mContext;
        private final PluginBuilder<ChromeDevtoolsDomain> mDelegate = new PluginBuilder<>();

        public DefaultInspectorModulesBuilder(Context context) {
            mContext = (Application) context.getApplicationContext();
        }

        private DefaultInspectorModulesBuilder provideIfDesired(ChromeDevtoolsDomain module) {
            mDelegate.provideIfDesired(module.getClass().getName(), module);
            return this;
        }

        public Iterable<ChromeDevtoolsDomain> finish() {
            provideIfDesired(new Network(mContext));
            provideIfDesired(new FileSystem(mContext));
            provideIfDesired(new Profiler());
            return mDelegate.finish();
        }
    }

    private static abstract class Initializer {
        private final Context mContext;

        protected Initializer(Context context) {
            mContext = context.getApplicationContext();
        }

        @Nullable
        protected abstract Iterable<ChromeDevtoolsDomain> getInspectorModules();

        final void start() {
            DreamCatcherCrashHandler.getInstance().attach();
            //create server to handle request.
            initServerManager();
            SocketServerManager.startServer(SocketServerManager.Type.LOCAL);

        }

        private void initServerManager() {
            LocalSocketServer localSocketServer = new LocalSocketServer(
                    "main",
                    AddressNameHelper.createCustomAddress(DEV_TOOLS_MAGIC_TAG),
                    new LazySocketHandler(new RealSocketHandlerFactory()));
            ServerManager serverManager = new ServerManager(localSocketServer);
            SocketServerManager.register(SocketServerManager.KEY_LOCAL_SERVER_MANAGER, serverManager);
        }

        private class RealSocketHandlerFactory implements SocketHandlerFactory {
            @Override
            public SocketHandler create() {
                ProtocolDetectingSocketHandler socketHandler = new ProtocolDetectingSocketHandler(mContext);
                //Create Http server to enable inspector
                Iterable<ChromeDevtoolsDomain> inspectorModules = getInspectorModules();
                if (inspectorModules != null) {
                    socketHandler.addHandler(
                            new ProtocolDetectingSocketHandler.AlwaysMatchMatcher(),
                            new DevtoolsSocketHandler(mContext, inspectorModules));
                }

                return socketHandler;
            }
        }
    }

    private static boolean isDebuggable(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private static class InitializerBuilder {
        final Context mContext;

        @javax.annotation.Nullable
        InspectorModulesProvider mInspectorModules;


        private InitializerBuilder(Context context) {
            mContext = context.getApplicationContext();
        }

        public InitializerBuilder enableInspector(InspectorModulesProvider modules) {
            mInspectorModules = modules;
            return this;
        }

        public Initializer build() {
            return new BuilderBasedInitializer(this);
        }
    }

    private static class BuilderBasedInitializer extends Initializer {
        @javax.annotation.Nullable
        private final InspectorModulesProvider mInspectorModules;

        private BuilderBasedInitializer(InitializerBuilder b) {
            super(b.mContext);
            mInspectorModules = b.mInspectorModules;
        }

        @javax.annotation.Nullable
        @Override
        protected Iterable<ChromeDevtoolsDomain> getInspectorModules() {
            return mInspectorModules != null ? mInspectorModules.get() : null;
        }
    }

    //EXTRA SETTINGS
    public static void setInternalLogEnabled(boolean enabled) {
        LogUtil.setLoggable(enabled);
    }

}
