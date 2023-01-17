package android.content.pm;


import android.content.pm.ShortcutInfo;
import android.os.UserHandle;

import java.util.List;

/**
 * Interface for LauncherApps#ShortcutChangeCallbackProxy.
 *
 * @hide
 */
oneway interface IShortcutChangeCallback
{
    void onShortcutsAddedOrUpdated(String packageName, in List<ShortcutInfo> shortcuts, in UserHandle user);

    void onShortcutsRemoved(String packageName, in List<ShortcutInfo> shortcuts, in UserHandle user);
}