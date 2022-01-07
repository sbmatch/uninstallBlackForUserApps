package com.ma.powersoundswitch.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import moe.shizuku.server.IRemoteProcess;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class mService extends Service {

    public mService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("statusbar"));
    }
}