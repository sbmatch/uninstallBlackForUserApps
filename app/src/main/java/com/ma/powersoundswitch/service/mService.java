package com.ma.powersoundswitch.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import moe.shizuku.server.IRemoteProcess;
import moe.shizuku.server.IShizukuApplication;
import moe.shizuku.server.IShizukuService;
import moe.shizuku.server.IShizukuServiceConnection;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class mService extends Service {

    public mService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return new IShizukuService.Stub() {
            @Override
            public int getVersion() throws RemoteException {
                return 0;
            }

            @Override
            public int getUid() throws RemoteException {
                return 0;
            }

            @Override
            public int checkPermission(String s) throws RemoteException {
                return 0;
            }

            @Override
            public IRemoteProcess newProcess(String[] strings, String[] strings1, String s) throws RemoteException {
                return null;
            }

            @Override
            public String getSELinuxContext() throws RemoteException {
                return null;
            }

            @Override
            public String getSystemProperty(String s, String s1) throws RemoteException {
                return null;
            }

            @Override
            public void setSystemProperty(String s, String s1) throws RemoteException {

            }

            @Override
            public int addUserService(IShizukuServiceConnection iShizukuServiceConnection, Bundle bundle) throws RemoteException {
                return 0;
            }

            @Override
            public int removeUserService(IShizukuServiceConnection iShizukuServiceConnection, Bundle bundle) throws RemoteException {
                return 0;
            }

            @Override
            public void attachApplication(IShizukuApplication iShizukuApplication, String s) throws RemoteException {

            }

            @Override
            public void requestPermission(int i) throws RemoteException {

            }

            @Override
            public boolean checkSelfPermission() throws RemoteException {
                return false;
            }

            @Override
            public boolean shouldShowRequestPermissionRationale() throws RemoteException {
                return false;
            }

            @Override
            public void exit() throws RemoteException {

            }

            @Override
            public void attachUserService(IBinder iBinder, Bundle bundle) throws RemoteException {

            }

            @Override
            public void dispatchPackageChanged(Intent intent) throws RemoteException {

            }

            @Override
            public boolean isHidden(int i) throws RemoteException {
                return false;
            }

            @Override
            public void dispatchPermissionConfirmationResult(int i, int i1, int i2, Bundle bundle) throws RemoteException {

            }

            @Override
            public int getFlagsForUid(int i, int i1) throws RemoteException {
                return 0;
            }

            @Override
            public void updateFlagsForUid(int i, int i1, int i2) throws RemoteException {

            }
        };
    }
}