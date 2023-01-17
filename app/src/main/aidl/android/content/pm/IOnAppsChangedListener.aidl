package android.content.pm;

import android.content.pm.ParceledListSlice;
import android.os.Bundle;
import android.os.UserHandle;

oneway interface IOnAppsChangedListener {
    void onPackageRemoved(in UserHandle user, String packageName);
    void onPackageAdded(in UserHandle user, String packageName);
    void onPackageChanged(in UserHandle user, String packageName);
    void onPackagesAvailable(in UserHandle user, in String[] packageNames, boolean replacing);
    void onPackagesUnavailable(in UserHandle user, in String[] packageNames, boolean replacing);
    void onPackagesSuspended(in UserHandle user, in String[] packageNames,
            in Bundle launcherExtras);
    void onPackagesUnsuspended(in UserHandle user, in String[] packageNames);
    //void onShortcutChanged(in UserHandle user, String packageName, in ParceledListSlice shortcuts);
    void onPackageLoadingProgressChanged(in UserHandle user, String packageName, float progress);
}