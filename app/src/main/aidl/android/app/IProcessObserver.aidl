// IProcessObserver.aidl
package android.app;

// Declare any non-default types here with import statements

interface IProcessObserver {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onProcessDied(int pid, int uid);
    void onForegroundServicesChanged(int pid, int uid, int serviceTypes);
    void onForegroundActivitiesChanged(int pid, int uid, boolean z);
    void onProcessStateChanged(int pid, int uid, int procState);
}