// IApplicationThread.aidl
package android.app;

// Declare any non-default types here with import statements

oneway interface IApplicationThread {
    void setProcessState(int state);
    void updateTimePrefs(int timeFormatPreference);
    void updateTimeZone();
    void processInBackground();
}