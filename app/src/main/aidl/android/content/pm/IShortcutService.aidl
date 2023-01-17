package android.content.pm;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ParceledListSlice;
import android.content.pm.ShortcutInfo;
//import com.android.internal.infra.AndroidFuture;


interface IShortcutService {

    //boolean setDynamicShortcuts(String packageName, in ParceledListSlice shortcutInfoList, int userId);

    //boolean addDynamicShortcuts(String packageName, in ParceledListSlice shortcutInfoList, int userId);

    void removeDynamicShortcuts(String packageName, in List<String> shortcutIds, int userId);

    void removeAllDynamicShortcuts(String packageName, int userId);

    //boolean updateShortcuts(String packageName, in ParceledListSlice shortcuts, int userId);

    //void requestPinShortcut(String packageName, in ShortcutInfo shortcut, in IntentSender resultIntent, int userId, in AndroidFuture<String> ret);

    //void createShortcutResultIntent(String packageName, in ShortcutInfo shortcut, int userId, in AndroidFuture<Intent> ret);

    void disableShortcuts(String packageName, in List<String> shortcutIds, CharSequence disabledMessage, int disabledMessageResId, int userId);

    void enableShortcuts(String packageName, in List<String> shortcutIds, int userId);

    //int getMaxShortcutCountPerActivity(String packageName, int userId);

    //int getRemainingCallCount(String packageName, int userId);

    //long getRateLimitResetTime(String packageName, int userId);

    //int getIconMaxDimensions(String packageName, int userId);

    void reportShortcutUsed(String packageName, String shortcutId, int userId);

    //void resetThrottling(); // system only API for developer opsions

    void onApplicationActive(String packageName, int userId); // system only API for sysUI

    //byte[] getBackupPayload(int user);

    //void applyRestore(in byte[] payload, int user);

    //boolean isRequestPinItemSupported(int user, int requestType);

    // System API used by framework's ShareSheet (ChooserActivity)
    //ParceledListSlice getShareTargets(String packageName, in IntentFilter filter, int userId);

    //boolean hasShareTargets(String packageName, String packageToCheck, int userId);

    //void removeLongLivedShortcuts(String packageName, in List<String> shortcutIds, int userId);

    //ParceledListSlice getShortcuts(String packageName, int matchFlags, int userId);

    void pushDynamicShortcut(String packageName, in ShortcutInfo shortcut, int userId);
}