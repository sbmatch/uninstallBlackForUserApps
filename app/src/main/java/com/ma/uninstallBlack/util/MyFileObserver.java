package com.ma.uninstallBlack.util;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.icu.text.SimpleDateFormat;
import android.os.Looper;
import android.os.ServiceManager;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.Utils;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.ma.uninstallBlack.MainActivity;
import com.ma.uninstallBlack.service.MyWorkService;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;


public class MyFileObserver extends FileAlterationListenerAdaptor {

    static final String LOG_TAG = "MyFileObserver";
    public static File Dir_path;

    @Override
    public void onStart(FileAlterationObserver observer) {
        //Log.w(LOG_TAG,"################# 启动文件监听 #################");
        super.onStart(observer);

    }

    @Override
    public void onDirectoryChange(File directory) {
        Log.w(LOG_TAG,"目录已改变: --> "+ directory.getAbsolutePath());
        if (directory.getAbsolutePath().contains("Ad")){
            Log.w(LOG_TAG,"尝试删除目录 "+directory.getName()+" --> "+directory.delete());
            FileUtils.delete(directory);
            ShizukuExecUtils.ShizukuExec(Utils.getApp().getBaseContext(),"rm -fr "+directory.getAbsolutePath());
        }
        super.onDirectoryChange(directory);
    }

    @Override
    public void onDirectoryCreate(File directory) {

        Log.i(LOG_TAG,new SimpleDateFormat("HH:mm:ss:SSS").format(new Date().getTime())+" 目录创建: --> "+ directory.getAbsolutePath());
        if (directory.getAbsolutePath().contains("Ad")){
            Log.w(LOG_TAG,"尝试删除目录 "+directory.getName()+" --> "+directory.delete());
            FileUtils.delete(directory);
            ShizukuExecUtils.ShizukuExec(Utils.getApp().getBaseContext(),"rm -fr "+directory.getAbsolutePath());
        }

        super.onDirectoryCreate(directory);
    }

    @Override
    public void onDirectoryDelete(File directory) {
        Log.w(LOG_TAG,"目录已删除: --> "+ directory.getAbsolutePath());
        if (directory.getAbsolutePath().contains("Ad")){
            Log.w(LOG_TAG,"创建Ad文件: "+FileUtils.createFileByDeleteOldFile("/storage/emulated/0/Android/data/com.netease.cloudmusic/cache/Ad"));
            //showNotificationReflect("网易云音乐广告已净化");
        }
        super.onDirectoryDelete(directory);
    }

    @Override
    public void onFileCreate(File file) {
        if (file.getName().contains("Ad")){
            Log.i(LOG_TAG,new SimpleDateFormat("HH:mm:ss:SSS").format(new Date().getTime())+" Ad同名文件已创建: --> " + file.getAbsolutePath());
            //showNotificationReflect("网易云音乐广告已净化");
        }
        super.onFileCreate(file);
    }

    @Override
    public void onFileDelete(File file) {

        if (file.getAbsolutePath().contains("Ad")){
            Log.i(LOG_TAG,new SimpleDateFormat("HH:mm:ss:SSS").format(new Date().getTime())+" 文件已删除: --> " + file.getAbsolutePath());
            FileUtils.createFileByDeleteOldFile("/storage/emulated/0/Android/data/com.netease.cloudmusic/cache/Ad");
        }
        super.onFileDelete(file);
    }

    @Override
    public void onStop(FileAlterationObserver observer) {
        //Log.w(LOG_TAG,"################# 停止文件监听 #################");
        super.onStop(observer);
    }

    public void showNotificationReflect(String msg){
        try {
            @SuppressLint("PrivateApi")
            Class<?> clazz = Class.forName("com.ma.uninstallBlack.service.MyWorkService");
            @SuppressLint("DiscouragedPrivateApi")
            Method method = clazz.getDeclaredMethod("showNotion", String.class);
            method.setAccessible(true);
            Object obj = clazz.newInstance();
            method.invoke(null,msg);
        } catch (ClassNotFoundException | RuntimeException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            FirebaseCrashlytics.getInstance().setCustomKey("反射","方法：android.content.pm.IPackageManager.getBlockUninstallForUser(String.class,int.class) 堆栈: "+e.fillInStackTrace());
        } catch (InstantiationException e) {
           e.fillInStackTrace();
        }
    }

}
