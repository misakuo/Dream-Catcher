package me.moxun.dreamcatcher.connector.console.command;

import android.support.annotation.Nullable;

/**
 * Created by moxun on 16/4/12.
 */
public interface CommandHandler {
    String desire();

    String help();

    @Nullable
    Object onCommand(@Nullable String param);
}
