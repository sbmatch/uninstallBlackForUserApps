package com.ma.uninstallBlack.service;

import static android.provider.Settings.ACTION_ALL_APPS_NOTIFICATION_SETTINGS;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.icu.text.SimpleDateFormat;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ma.uninstallBlack.IAppIPC;
import com.ma.uninstallBlack.receiver.MyBroadcastReceiver;
import com.ma.uninstallBlack.util.OtherUtils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MyIPCService extends Service implements MyBroadcastReceiver.BroadcastMsg {

    static final String LOG_TAG = MyIPCService.class.getSimpleName();
    public static boolean isConn = false;
    public static boolean isServiceOK = false;
    private IpcServiceMsg ipcServiceMsg = null;
    private static boolean isRegisterReceiver = false;
    public IAppIPC iAppIPC;
    public IBinder s;
    public static LocalBroadcastManager localBroadcastManager;
    public static MyBroadcastReceiver mReceiver = new MyBroadcastReceiver();
    private localReceiver localReceiver = new localReceiver();


    public MyIPCService(){

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return s;
    }


    @Override
    public void onCreate() {

        OtherUtils.showNotification(getBaseContext(),new Intent().setAction(ACTION_ALL_APPS_NOTIFICATION_SETTINGS), "2001","MyIPcService","MyIpcService后台保活通知", NotificationManager.IMPORTANCE_MIN);
        isServiceOK = true;
        Log.w(LOG_TAG,"我已存活，感觉良好");
        localBroadcastManager  = LocalBroadcastManager.getInstance(MyIPCService.this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(getPackageName()+".removeAccount");
        filter.setPriority(1000);
        localBroadcastManager.registerReceiver(localReceiver,filter);
        new MyBroadcastReceiver().setCallback(this);
        try{
            startForeground(2001,OtherUtils.notification);
         }catch (RuntimeException e){Log.e(LOG_TAG,e.getMessage());}

        if (!isRegisterReceiver){
            注册动态广播接收器();
        }


        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try{
            stopForeground(true);

            Timer timer = new Timer();
            TimerTask task1 = new TimerTask() {
                @Override
                public void run() {

                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }

                    Log.i(LOG_TAG,"时间："+ new SimpleDateFormat("HH:mm:ss").format(new Date().getTime()));
                }
            };
            timer.schedule(task1,0, 60 * 1000L); //立即开始 每1min执行一次

        }catch (Exception ignored){}

        return START_STICKY;
    }

    public  void 注册动态广播接收器() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BOOT_COMPLETED); //启动了
        filter.addAction(Intent.ACTION_SCREEN_ON); //屏幕被点亮了
        filter.addAction(Intent.ACTION_SCREEN_OFF); //屏幕被关闭了
        filter.addAction(Intent.ACTION_POWER_CONNECTED); //电源已经连接了
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED); //电源被断开了
        filter.addAction(Intent.ACTION_BATTERY_CHANGED); //电池发生变化了
        filter.addAction(Intent.ACTION_TIME_TICK); //设备的时间变化了
        filter.addAction(Intent.ACTION_HEADSET_PLUG); //插上耳机了
        filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED); //输入法改变了
        filter.addAction("android.media.VOLUME_CHANGED_ACTION"); //音量发生变化了
        filter.addAction("android.provider.Telephony.SECRET_CODE");
        filter.addAction("com.ma.lockscreen.receiver");
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_LOCKED_BOOT_COMPLETED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.setPriority(1000); //设置最大优先级

        Log.w(LOG_TAG,"注册动态广播接收器");
        isRegisterReceiver = true;
        try{
            registerReceiver(mReceiver, filter); //注册
        }catch (Exception e){
            unregisterReceiver(mReceiver);
            //registerReceiver(mReceiver, filter); //注册
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isConn = true;
            Log.w(LOG_TAG,"已绑定："+name.getShortClassName());
            s = service;
            iAppIPC = IAppIPC.Stub.asInterface(service);
            try{
                if (ipcServiceMsg != null ){
                    ipcServiceMsg.sendServiceMsg("bindUserService");
                }
            }catch (Exception e){
                Log.e(LOG_TAG,e.getMessage());
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConn = false;
            Log.w(LOG_TAG,"重新绑定"+MyWorkService.class.getSimpleName());
            startForegroundService(new Intent(getBaseContext(),MyWorkService.class));
            bindService(new Intent(getApplicationContext(),MyWorkService.class), connection, Context.BIND_IMPORTANT);
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }


    class localReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(context.getPackageName()+".removeAccount")){
                //startForegroundService(new Intent(context,MyWorkService.class));
                bindService(new Intent(context,MyWorkService.class), connection, Context.BIND_IMPORTANT);
            }
        }
    }

    @Override
    public void onDestroy() {
        try{
            ipcServiceMsg.sendServiceMsg("died");
        }catch (Exception ignored){
        }
        super.onDestroy();

        Intent intent = new Intent("com.ma.lockscreen.receiver");
        intent.setComponent(new ComponentName(getPackageName(), MyBroadcastReceiver.class.getName()));
        //sendBroadcast(intent);
        try{
            OtherUtils.stopWatch();
            localBroadcastManager.unregisterReceiver(localReceiver);
            unregisterReceiver(mReceiver);
            unbindService(connection);
        }catch (RuntimeException e){
            e.fillInStackTrace();
        }
    }

    @Override
    public void sendBroadcastMsg(String msg) {
        if (!msg.equals("true")){
            Log.w(LOG_TAG,"发动 魔法卡 死者苏生 从墓地复活"+MyWorkService.class.getSimpleName());
            startForegroundService(new Intent(getBaseContext(),MyWorkService.class));
        }
    }


    public interface IpcServiceMsg{
        void sendServiceMsg(String msg);
    }

    public void setCallback(IpcServiceMsg sendMsg){
        this.ipcServiceMsg= sendMsg;
    }
}
