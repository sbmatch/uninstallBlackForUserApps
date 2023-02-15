package com.ma.uninstallBlack.service;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.ma.uninstallBlack.util.BaseAppliation.jobScheduler;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.ma.uninstallBlack.IAppIPC;
import com.ma.uninstallBlack.MainActivity;
import com.ma.uninstallBlack.activity.DialogActivity;
import com.ma.uninstallBlack.receiver.MyBroadcastReceiver;
import com.ma.uninstallBlack.util.OtherUtils;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import rikka.shizuku.Shizuku;

public class MyIPCService extends Service implements MyBroadcastReceiver.BroadcastMsg {

    static final String LOG_TAG = MyIPCService.class.getSimpleName();
    public static boolean isConn = false;
    public static final int JOB2_ID = 100000;
    private IpcServiceMsg ipcServiceMsg = null;
    public IBinder s;
    public static LocalBroadcastManager localBroadcastManager;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    public MyBroadcastReceiver mReceiver;


    public MyIPCService(){

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return s;
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onCreate() {

        if (editor == null){
            sp = getSharedPreferences("StartSate",MODE_PRIVATE);
            editor = sp.edit();
        }

        //OtherUtils.showNotificationReflect("正在运行后台服务");
        //startForeground(2000,OtherUtils.notification);
        editor.putBoolean("isRegisterReceiver",false).commit();

        localBroadcastManager  = LocalBroadcastManager.getInstance(MyIPCService.this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(getPackageName()+".removeAccount");
        filter.setPriority(999);
        //localBroadcastManager.registerReceiver(localReceiver,filter);
        mReceiver = new MyBroadcastReceiver();
        注册动态广播接收器();

        //stopForeground(true);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try{
            Timer timer = new Timer();
            TimerTask task1 = new TimerTask() {
                @Override
                public void run() {

                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }

                    if (sp.getString("崩溃",null) != null){
                        startActivity(new Intent(getBaseContext(), DialogActivity.class).setFlags(FLAG_ACTIVITY_NEW_TASK).setAction(sp.getString("崩溃",null)));
                    }

                    if (sp.getString("ANR",null) != null){
                        startActivity(new Intent(getBaseContext(), DialogActivity.class).setFlags(FLAG_ACTIVITY_NEW_TASK).setAction(sp.getString("ANR",null)));
                    }

                    if (OtherUtils.isServiceRunning(MyWorkService.class.getName())){
                       NotificationManagerCompat.from(getBaseContext()).cancelAll();
                    }

                    Intent intent = new Intent("com.ma.lockscreen.receiver");
                    intent.setComponent(new ComponentName(getPackageName(), MyBroadcastReceiver.class.getName()));
                    sendBroadcast(intent);
                }
            };
            timer.schedule(task1,0,  1* 1000L); //立即开始 每0.5min执行一次

        }catch (Exception d){
            Log.e(LOG_TAG,d.toString());
        }

        return START_NOT_STICKY;
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
        //filter.addAction(Intent.ACTION_HEADSET_PLUG); //插上耳机了
        filter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED); //输入法改变了
        filter.addAction("android.media.VOLUME_CHANGED_ACTION"); //音量发生变化了
        filter.addAction("android.provider.Telephony.SECRET_CODE");
        filter.addAction("com.ma.lockscreen.receiver");
        filter.addAction("com.ma.lockscreen.receiver.exit");
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_LOCKED_BOOT_COMPLETED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_INSTALL_PACKAGE);
        filter.addAction(Intent.ACTION_UNINSTALL_PACKAGE);
        filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.setPriority(999); //设置最大优先级

        if (!sp.getBoolean("isRegisterReceiver",false)){
            Log.w(LOG_TAG,"注册动态广播接收器");
            editor.putBoolean("isRegisterReceiver",true).commit();
            mReceiver.setCallback(this);
            registerReceiver(mReceiver, filter); //注册
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isConn = true;
            Log.w(LOG_TAG,"已绑定："+name.getShortClassName());
            s = service;
            IAppIPC iAppIPC = IAppIPC.Stub.asInterface(service);
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
            //Log.w(LOG_TAG,"重新绑定"+MyWorkService.class.getSimpleName());
            //startForegroundService(new Intent(getBaseContext(),MyWorkService.class));
            //bindService(new Intent(getApplicationContext(),MyWorkService.class), connection, Context.BIND_IMPORTANT);
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        try{
            ipcServiceMsg.sendServiceMsg("died");
        }catch (Exception ignored){
        }
        super.onDestroy();
        try{
            //localBroadcastManager.unregisterReceiver(localReceiver);
            unregisterReceiver(mReceiver);
            unbindService(connection);
        }catch (Exception e){
            e.fillInStackTrace();
        }
    }

    @Override
    public void sendBroadcastMsg(String msg) {
        if (msg.equals("killUSelf")){
            Log.w(LOG_TAG,"************正在清除所有通知，关闭定时保活任务*************");
            jobScheduler.cancelAll();

                try{
                    Method s = MainActivity.class.getMethod("killToSelf");
                    s.invoke(MainActivity.class.newInstance());
                }catch (Exception e){
                    e.printStackTrace();
                }
            unregisterReceiver(mReceiver);
            unbindService(connection);
            ServiceUtils.stopService(MyWorkService.class);
            ServiceUtils.stopService(MyIPCService.class);
            //NotificationManagerCompat.from(getApplicationContext()).cancelAll();
        }
    }


    public interface IpcServiceMsg{
        void sendServiceMsg(String msg);
    }

    public void setCallback(IpcServiceMsg sendMsg){
        this.ipcServiceMsg= sendMsg;
    }
}
