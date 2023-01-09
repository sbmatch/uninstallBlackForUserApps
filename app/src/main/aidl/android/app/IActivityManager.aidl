// IActivityManager.aidl
package android.app;

import android.app.ActivityManager;
import android.app.IProcessObserver;
import android.content.ComponentName;
import android.app.IApplicationThread;
import android.app.IActivityController;

// Declare any non-default types here with import statements

interface IActivityManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void registerProcessObserver(in IProcessObserver iProcessObserver);
    void unregisterProcessObserver(in IProcessObserver iProcessObserver);
    void killAllBackgroundProcesses();
    boolean isAppFreezerSupported();
    int getOomAdjOfPid(int pid);
    void forceStopPackage(String packageName, int userId);
    boolean enableAppFreezer(boolean z);
    int getPackageProcessState(String packageName, String callingPackage);
    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses();
    ComponentName startService(in IApplicationThread caller, in Intent service,
                in String resolvedType, boolean requireForeground, in String callingPackage,
                in String callingFeatureId, int userId);
    void setActivityController(in IActivityController watcher, boolean imAMonkey);
}