package android.content.pm;

//import android.app.IApplicationThread;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
//import android.content.LocusId;
//import android.content.pm.ApplicationInfo;
import android.content.pm.IOnAppsChangedListener;
import android.content.pm.LauncherActivityInfoInternal;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutQueryWrapper;
import android.content.pm.IPackageInstallerCallback;
import android.content.pm.IShortcutChangeCallback;
//import android.content.pm.PackageInstaller;
import android.content.pm.ParceledListSlice;
//import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
//import android.graphics.Rect;
import android.os.Bundle;
import android.os.UserHandle;
//import android.os.ParcelFileDescriptor;

//import com.android.internal.infra.AndroidFuture;

import java.util.List;


interface ILauncherApps {
    //void addOnAppsChangedListener(String callingPackage, in IOnAppsChangedListener listener);
    //void removeOnAppsChangedListener(in IOnAppsChangedListener listener);
    //ParceledListSlice getLauncherActivities(String callingPackage, String packageName, in UserHandle user);

    boolean isPackageEnabled(String callingPackage, String packageName, in UserHandle user);
    Bundle getSuspendedPackageLauncherExtras(String packageName, in UserHandle user);

    boolean isActivityEnabled(String callingPackage, in ComponentName component, in UserHandle user);

    //ParceledListSlice getShortcuts(String callingPackage, in ShortcutQueryWrapper query, in UserHandle user);


    boolean startShortcut(String callingPackage, String packageName, String featureId, String id, in Rect sourceBounds, in Bundle startActivityOptions, int userId);

    //int getShortcutIconResId(String callingPackage, String packageName, String id, int userId);


    boolean hasShortcutHostPermission(String callingPackage);
    boolean shouldHideFromSuggestions(String packageName, in UserHandle user);

    //ParceledListSlice getShortcutConfigActivities(  String callingPackage, String packageName, in UserHandle user);
    IntentSender getShortcutConfigActivityIntent(String callingPackage, in ComponentName component, in UserHandle user);
    PendingIntent getShortcutIntent(String callingPackage, String packageName, String shortcutId, in Bundle opts, in UserHandle user);

    // Unregister is performed using package installer
    //void registerPackageInstallerCallback(String callingPackage, in IPackageInstallerCallback callback);
    //ParceledListSlice getAllSessions(String callingPackage);

    //void registerShortcutChangeCallback(String callingPackage, in ShortcutQueryWrapper query, in IShortcutChangeCallback callback);
    //void unregisterShortcutChangeCallback(String callingPackage, in IShortcutChangeCallback callback);

    void cacheShortcuts(String callingPackage, String packageName, in List<String> shortcutIds, in UserHandle user, int cacheFlags);
    void uncacheShortcuts(String callingPackage, String packageName, in List<String> shortcutIds, in UserHandle user, int cacheFlags);

    String getShortcutIconUri(String callingPackage, String packageName, String shortcutId, int userId);
}