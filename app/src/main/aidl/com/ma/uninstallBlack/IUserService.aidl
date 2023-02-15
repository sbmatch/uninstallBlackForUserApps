package com.ma.uninstallBlack;


import android.app.ActivityManager;
import android.app.IProcessObserver;
import android.app.IApplicationThread;
import android.content.pm.VersionedPackage;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallObserver2;
import android.net.Uri;
import android.accounts.Account;
import android.accounts.IAccountManagerResponse;
import android.app.IActivityManager;
import android.app.IActivityController;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.pm.ParceledListSlice;
import android.content.pm.ShortcutInfo;

interface IUserService {
    void destroy() = 16777114;
    void exit() = 1;
    String exec(String cmd) = 2;
    void removePowerWhitelistApp(String pkg_name) = 4;
    void addPowerSaveWhitelistApp(String name) = 46;
    int addPowerSaveWhitelistApps(in List<String> packageNames) = 47;
    boolean isPowerSaveWhitelistExceptIdleApp(String name) = 48;
    boolean isPowerSaveWhitelistApp(String name) = 49;
    void sensor(boolean z) = 5;
    void ac_reg_proObserver(in IProcessObserver iProcessObserver) = 6;
    void ac_unreg_proObserver(in IProcessObserver iProcessObserver) = 12;
    void ac_enableFreezer(boolean z) = 7;

    void ac_killAllBackgroundProcesses() =9;
    void forceStopPackage(String packageName, int userId) = 10;

    int getPackageProcessState(String packageName, String callingPackage) = 15;
    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() = 16;
    ComponentName startService(in IApplicationThread caller, in Intent service,
                    in String resolvedType, boolean requireForeground, in String callingPackage,
                    in String callingFeatureId, int userId) = 17;


    boolean setBlockUninstallForUser(String packageName, boolean blockUninstall, int userId) = 20;
    boolean getBlockUninstallForUser(String packageName, int userId) = 21;
    String[] getUserPowerWhitelist() = 22;

   void grantRuntimePermission(String packageName, String permissionName, int userId) = 24;

 boolean removeAccountExplicitly(in Account account) = 25;
 void removeAccountAsUser(in IAccountManagerResponse response,
  in Account account, boolean expectActivityLaunch, int userId) = 30;

  Account[] getAccountsAsUser(String accountType, int userId, String opPackageName) = 26;
  Account[] getAccountsForPackage(String packageName, int uid, String opPackageName) = 27;

  Map getAccountsAndVisibilityForPackage(in String packageName, in String accountType) = 31;
  Map getPackagesAndVisibilityForAccount(in Account account) = 32;
  boolean setAccountVisibility(in Account a, in String packageName, int newVisibility) =33;
    int getAccountVisibility(in Account a, in String packageName) = 34;

    void registerAccountListener(in String[] accountTypes, String opPackageName) =28;
    void unregisterAccountListener(in String[] accountTypes, String opPackageName) = 29;

    void setActivityController(in IActivityController controller) = 37;

    void startWatchFromFileObserver(in String file) = 39;

    void stopWatchFromFileObserver() = 40;

    void deleteExistingPackageAsUser(in VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId) = 43;
    void deletePackageVersioned(in VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId, int flags) = 44;
}
