package com.ma.uninstallBlack.service;


import static rikka.shizuku.SystemServiceHelper.getSystemService;

import android.accounts.Account;
import android.accounts.IAccountManager;
import android.accounts.IAccountManagerResponse;
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
import android.content.pm.VersionedPackage;
import android.hardware.ISensorPrivacyManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.blankj.utilcode.util.Utils;
import com.ma.uninstallBlack.BuildConfig;
import com.ma.uninstallBlack.IUserService;
import com.ma.uninstallBlack.util.MyFileObserver;
import com.ma.uninstallBlack.util.OtherUtils;
import com.ma.uninstallBlack.util.PackageInstallerUtils;

import org.apache.commons.io.monitor.FileAlterationListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;


public class UserService extends IUserService.Stub {

    static final String LOG_TAG = UserService.class.getSimpleName();
    public static ISensorPrivacyManager iSensorPrivacyManager = ISensorPrivacyManager.Stub.asInterface(getSystemService("sensor_privacy"));
    public static IActivityManager activityManager = IActivityManager.Stub.asInterface(getSystemService("activity"));
    public static IDeviceIdleController idleController =  IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
    public static IPackageManager ipackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
    public static IAccountManager iAccountManager = IAccountManager.Stub.asInterface(SystemServiceHelper.getSystemService("account"));
    public static IDevicePolicyManager iDevicePolicyManager = IDevicePolicyManager.Stub.asInterface(SystemServiceHelper.getSystemService("device_policy"));

    public static IShortcutService iShortcutService = IShortcutService.Stub.asInterface(ServiceManager.getService(Context.SHORTCUT_SERVICE));

    public static  WatchService watchService;
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
    public int ac_getOomAdjOfPid(int pid) throws RemoteException {
        return activityManager.getOomAdjOfPid(pid);
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
    public boolean isAppFreezerSupported() throws RemoteException {
        return activityManager.isAppFreezerSupported();
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
    public void startForegroundService(Intent service) throws RemoteException {
        Utils.getApp().startForegroundService(service);
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
    public Account[] getAccounts() throws RemoteException {

       return iAccountManager.getAccounts();
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
//        try {
//
//            @SuppressLint("PrivateApi")
//            Method mSetActivityController = activityManager.getClass().getMethod(
//                    "setActivityController", IActivityController.class, boolean.class);
//            mSetActivityController.invoke(activityManager, new MyIPCService.MyAvController(), true);
//
//        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public ComponentName getdpm() throws RemoteException {
        return OtherUtils.getProfileOwner();
    }

    @Override
    public void startWatchFromFileObserver(String file) throws RemoteException {


        new Thread(() -> {
            OtherUtils.startWatch(file, new MyFileObserver(),1000);
        }).start();
    }

    @Override
    public void stopWatchFromFileObserver() throws RemoteException {
        OtherUtils.stopWatch();
    }

    @Override
    public void setSystemUpdatePolicy(ComponentName who, SystemUpdatePolicy policy) throws RemoteException {
        iDevicePolicyManager.setSystemUpdatePolicy(who, policy);
    }

    @Override
    public SystemUpdatePolicy getSystemUpdatePolicy() throws RemoteException {
        return iDevicePolicyManager.getSystemUpdatePolicy();
    }

    @Override
    public void deleteExistingPackageAsUser(VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId) throws RemoteException {
        ipackageManager.deleteExistingPackageAsUser(versionedPackage, observer, userId);
    }

    @Override
    public void deletePackageVersioned(VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId, int flags) throws RemoteException {
        ipackageManager.deletePackageVersioned(versionedPackage, observer, userId, flags);
    }

    @Override
    public String doInstallApk(List<Uri> uris) throws RemoteException {

        ContentResolver cr = Utils.getApp().getContentResolver();
        StringBuilder res = new StringBuilder();

        try{
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int installFlags = PackageInstallerUtils.getInstallFlags(params);
            installFlags |= 0x00000004/*PackageManager.INSTALL_ALLOW_TEST*/ | 0x00000002/*PackageManager.INSTALL_REPLACE_EXISTING*/;
            String installerPackageName = (Shizuku.getUid() == 0) ? BuildConfig.APPLICATION_ID : "com.android.shell";
            int userId = (Shizuku.getUid() == 0) ? Process.myUserHandle().hashCode() : 0;
            PackageInstallerUtils.setInstallFlags(params,installFlags);
            IPackageInstaller _packageInstaller = ipackageManager.getPackageInstaller();
            PackageInstaller packageInstaller = PackageInstallerUtils.createPackageInstaller(_packageInstaller,installerPackageName,userId);
            int sessionId = packageInstaller.createSession(params);
            IPackageInstallerSession _session = IPackageInstallerSession.Stub.asInterface(new ShizukuBinderWrapper(_packageInstaller.openSession(sessionId).asBinder()));
            PackageInstaller.Session session = PackageInstallerUtils.createSession(_session);

            int i = 0;
            for (Uri uri : uris) {
                String name = i + ".apk";

                byte[] buf = new byte[8192];
                int len;
                try (InputStream is = cr.openInputStream(uri); OutputStream os = session.openWrite(name, 0, -1)) {
                    while ((len = is.read(buf)) > 0) {
                        os.write(buf, 0, len);
                        os.flush();
                        session.fsync(os);
                    }
                }

                i++;

                //Thread.sleep(1000);
            }

            res.append('\n').append("commit: ");

            Intent[] results = new Intent[]{null};
            CountDownLatch countDownLatch = new CountDownLatch(1);
            IntentSender intentSender = IntentSender.class.getConstructor(IIntentSender.class).newInstance(new IIntentSender.Stub(){

                @Override
                public int send(int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
                    return 0;
                }

                @Override
                public void send(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {

                }
            });

            session.commit(intentSender);
            countDownLatch.await();
            Intent result = results[0];
            int status = result.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            String message = result.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            res.append('\n').append("status: ").append(status).append(" (").append(message).append(")");

            session.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return res.toString().trim();
    }

}
