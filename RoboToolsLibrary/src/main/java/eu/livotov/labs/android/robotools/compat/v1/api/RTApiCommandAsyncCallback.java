package eu.livotov.labs.android.robotools.compat.v1.api;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 29.01.13
 */
public interface RTApiCommandAsyncCallback {

    void onBeforeCommandStart(RTApiCommand cmd);

    void onCommandCompleted(RTApiCommandResult result);

    void onCommandFailed(RTApiError error);
}
