package com.ma.uninstallBlack.service;


import static rikka.shizuku.SystemServiceHelper.getSystemService;

import android.accounts.Account;
import android.accounts.IAccountManager;
import android.accounts.IAccountManagerResponse;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.IActivityController;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.IProcessObserver;
import android.app.admin.IDevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageInstallerSession;
import android.content.pm.IPackageManager;
import android.content.pm.IShortcutService;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.VersionedPackage;
import android.hardware.ISensorPrivacyManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.ma.uninstallBlack.BuildConfig;
import com.ma.uninstallBlack.IUserService;
import com.ma.uninstallBlack.util.MyFileObserver;
import com.ma.uninstallBlack.util.OtherUtils;
import com.ma.uninstallBlack.util.PackageInstallerUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;


public abstract class UserService extends IUserService.Stub {

    static final String LOG_TAG = UserService.class.getSimpleName();
    public static ISensorPrivacyManager iSensorPrivacyManager = ISensorPrivacyManager.Stub.asInterface(getSystemService("sensor_privacy"));
    public static IActivityManager activityManager = IActivityManager.Stub.asInterface(getSystemService("activity"));
    public static IDeviceIdleController idleController =  IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
    public static IPackageManager ipackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
    public static IAccountManager iAccountManager = IAccountManager.Stub.asInterface(SystemServiceHelper.getSystemService("account"));
    public static IDevicePolicyManager iDevicePolicyManager = IDevicePolicyManager.Stub.asInterface(SystemServiceHelper.getSystemService("device_policy"));

    public static IShortcutService iShortcutService = IShortcutService.Stub.asInterface(ServiceManager.getService(Context.SHORTCUT_SERVICE));

    @SuppressLint("StaticFieldLeak")
    public static  Context c_shell;

    static {
        try {
            c_shell = Utils.getApp().getApplicationContext().createPackageContext("com.android.shell",Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public UserService () {

    }

    @Override
    public void destroy() throws RemoteException {
        System.exit(0);
    }

    @Override
    public void exit() throws RemoteException {
        destroy();
    }

    @Override
    public String exec(String cmd) throws RemoteException {

        StringBuilder buf = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(cmd).getInputStream()));
            String line;
            while (( line = bufferedReader.readLine()) != null) {
                buf.append(line).append("\n");
            }
        }catch (Exception e){
            buf.append(e.getMessage()).append("\n");
        }
        return buf+"";
    }

    @Override
    public void removePowerWhitelistApp(String pkg_name) throws RemoteException {
        idleController.removePowerSaveWhitelistApp(pkg_name);
    }

    @Override
    public void addPowerSaveWhitelistApp(String name) throws RemoteException {
        idleController.addPowerSaveWhitelistApp(name);
    }

    @Override
    public int addPowerSaveWhitelistApps(List<String> packageNames) throws RemoteException {
        return idleController.addPowerSaveWhitelistApps(packageNames);
    }

    @Override
    public boolean isPowerSaveWhitelistExceptIdleApp(String name) throws RemoteException {
        return idleController.isPowerSaveWhitelistExceptIdleApp(name);
    }

    @Override
    public boolean isPowerSaveWhitelistApp(String name) throws RemoteException {
        return idleController.isPowerSaveWhitelistApp(name);
    }


    @Override
    public void sensor(boolean z) throws RemoteException {
        iSensorPrivacyManager.setSensorPrivacy(z);
    }

    @Override
    public void ac_reg_proObserver(IProcessObserver iProcessObserver) throws RemoteException {
        activityManager.registerProcessObserver(iProcessObserver);
    }

    @Override
    public void ac_unreg_proObserver(IProcessObserver iProcessObserver) throws RemoteException {
        activityManager.unregisterProcessObserver(iProcessObserver);
    }

    @Override
    public void ac_enableFreezer(boolean z) throws RemoteException {
        activityManager.enableAppFreezer(z);
    }


    @Override
    public void ac_killAllBackgroundProcesses() throws RemoteException {
        activityManager.killAllBackgroundProcesses();
    }

    @Override
    public void forceStopPackage(String packageName, int userId) throws RemoteException {
        activityManager.forceStopPackage(packageName,userId);
    }

    @Override
    public int getPackageProcessState(String packageName, String callingPackage) throws RemoteException {
        return activityManager.getPackageProcessState(packageName,callingPackage);
    }

    @Override
    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException {
        return activityManager.getRunningAppProcesses();
    }

    @Override
    public ComponentName startService(IApplicationThread caller, Intent service, String resolvedType, boolean requireForeground, String callingPackage, String callingFeatureId, int userId) throws RemoteException {
        return activityManager.startService(caller,service,resolvedType,requireForeground,callingPackage,callingFeatureId,userId);
    }


    @Override
    public boolean setBlockUninstallForUser(String packageName, boolean blockUninstall, int userId) throws RemoteException {

        return ipackageManager.setBlockUninstallForUser(packageName, blockUninstall, userId);
    }

    @Override
    public boolean getBlockUninstallForUser(String packageName, int userId) throws RemoteException {
        return ipackageManager.getBlockUninstallForUser(packageName, userId);
    }

    @Override
    public String[] getUserPowerWhitelist() throws RemoteException {
        return idleController.getUserPowerWhitelist();
    }

    @Override
    public void grantRuntimePermission(String packageName, String permissionName, int userId) throws RemoteException {
        ipackageManager.grantRuntimePermission(packageName, permissionName, userId);
    }

    @Override
    public boolean removeAccountExplicitly(Account account) throws RemoteException {
        return iAccountManager.removeAccountExplicitly(account);
    }

    @Override
    public void removeAccountAsUser(IAccountManagerResponse response, Account account, boolean expectActivityLaunch, int userId) throws RemoteException {
        iAccountManager.removeAccountAsUser(response,account,expectActivityLaunch,userId);
    }

    @Override
    public Account[] getAccountsAsUser(String accountType, int userId, String opPackageName) throws RemoteException {
        return iAccountManager.getAccountsAsUser(accountType,userId,opPackageName);
    }


    @Override
    public Account[] getAccountsForPackage(String packageName, int uid, String opPackageName) throws RemoteException {
        return iAccountManager.getAccountsForPackage(packageName,uid,opPackageName);
    }

    @Override
    public Map getAccountsAndVisibilityForPackage(String packageName, String accountType) throws RemoteException {
        return iAccountManager.getAccountsAndVisibilityForPackage(packageName,accountType);
    }

    @Override
    public Map getPackagesAndVisibilityForAccount(Account account) throws RemoteException {
        return iAccountManager.getPackagesAndVisibilityForAccount(account);
    }

    @Override
    public boolean setAccountVisibility(Account a, String packageName, int newVisibility) throws RemoteException {
        return iAccountManager.setAccountVisibility(a, packageName, newVisibility);
    }

    @Override
    public int getAccountVisibility(Account a, String packageName) throws RemoteException {
        return iAccountManager.getAccountVisibility(a, packageName);
    }

    @Override
    public void registerAccountListener(String[] accountTypes, String opPackageName) throws RemoteException {
        iAccountManager.registerAccountListener(accountTypes, opPackageName);
    }

    @Override
    public void unregisterAccountListener(String[] accountTypes, String opPackageName) throws RemoteException {
        iAccountManager.unregisterAccountListener(accountTypes, opPackageName);
    }

    @Override
    public void setActivityController(IActivityController controller) throws RemoteException {
        activityManager.setActivityController(controller, true);
    }

    @Override
    public void startWatchFromFileObserver(String file) throws RemoteException {

        new Thread(() -> {
            Looper.prepare();
            OtherUtils.startWatch(file, new MyFileObserver(),1000);
        }).start();
    }

    @Override
    public void stopWatchFromFileObserver() throws RemoteException {
        OtherUtils.stopWatch();
    }


    @Override
    public void deleteExistingPackageAsUser(VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId) throws RemoteException {
        ipackageManager.deleteExistingPackageAsUser(versionedPackage, observer, userId);
    }

    @Override
    public void deletePackageVersioned(VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId, int flags) throws RemoteException {
        ipackageManager.deletePackageVersioned(versionedPackage, observer, userId, flags);
    }
}
