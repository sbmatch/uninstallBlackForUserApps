package com.ma.uninstallBlack.activity;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ShellUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ma.uninstallBlack.BuildConfig;
import com.ma.uninstallBlack.IUserService;
import com.ma.uninstallBlack.service.UserService;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import rikka.shizuku.Shizuku;

public class LockScreenActivity extends AppCompatActivity {
    private static final String COMMAND = "logcat -d ";
    private static final String COMMAND_HARD_INFO = "[command]: "+COMMAND;
    private static final String COMMAND_REMOVE_WHITELIST = "dumpsys deviceidle whitelist -";

    private StringBuilder buf = new StringBuilder();

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        HiddenApiBypass.addHiddenApiExemptions("");
        super.onCreate(savedInstanceState);

        try {
            Shizuku.bindUserService(userServiceArgs, userServiceConnection);
        }catch (SecurityException e){
            Log.e("shizuku",e.fillInStackTrace()+"");
        }
    }


    private IUserService userService;

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            // TODO Auto-generated method stub
            if (userService == null)
                return;
            userService.asBinder().unlinkToDeath(mDeathRecipient, 0);
            userService = null;
            Shizuku.bindUserService(userServiceArgs,userServiceConnection);
        }
    };

    private final Shizuku.UserServiceArgs userServiceArgs = new Shizuku.UserServiceArgs(new ComponentName(BuildConfig.APPLICATION_ID, UserService.class.getName())).processNameSuffix("services");

    private final ServiceConnection userServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {

            StringBuilder res = new StringBuilder();
            res.append("onServiceConnected: ").append(componentName.getClassName()).append('\n');

            if (binder != null && binder.pingBinder()) {
                userService = IUserService.Stub.asInterface(binder);
                ShellUtils.execCmd("logcat -c",false,false);

                try {
                    Object objManger = getBaseContext().getPackageManager();
                    // 获取管理器对象
                    @SuppressLint("PrivateApi")
                    Class<?> clazz_z = Class.forName("android.content.pm.PackageManager");
                    @SuppressLint("DiscouragedPrivateApi")
                    Method isSuspended = clazz_z.getDeclaredMethod("isPackageSuspended",String.class);
                    isSuspended.setAccessible(true);

                    Method setSuspend = clazz_z.getDeclaredMethod("setPackageSuspend",String.class,boolean.class,PersistableBundle.class, PersistableBundle.class,String.class);
                    setSuspend.setAccessible(true);
                    // 使用反射机制拿到受限方法
                    String pkaName = "com.android.browser";
                    Toast.makeText(LockScreenActivity.this,""+isSuspended.invoke(objManger,pkaName), Toast.LENGTH_SHORT).show();
                   // Log.v("pkg_suspend", String.valueOf(is_Suspend));

                    String[] pkgName_List = new String[]{"com.wuba"};
                    isSuspended.invoke(pkgName_List,true,null,null,"抱歉，此程序已被设备管理员禁用");

                    //SystemServiceApi.PackageManger_setSuspend(pkgName_List,true,null,null,"抱歉，此程序已被设备管理员禁用");

                    //if (is_Suspend) {
                        ///removeFromWhitelist.invoke(obj, info);
                        //Log.i("remove","[command]: "+COMMAND_REMOVE_WHITELIST+info+"\n"+userService.doSomething(COMMAND_REMOVE_WHITELIST+info));
                    //}

                } catch (UnsupportedOperationException |ClassNotFoundException | NoSuchMethodException e) {
                    e.printStackTrace();
                    //Toast.makeText(LockScreenActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    new MaterialAlertDialogBuilder(LockScreenActivity.this).setMessage(e.fillInStackTrace()+"").show();
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                try {
                    //执行锁屏指令
                   // userService.doSomething("input keyevent " + KeyEvent.KEYCODE_POWER);
                   // ActivityUtils.startHomeActivity();
                } catch (Exception e) {
                    Log.e("lock",e.getMessage());
                }

            } else {
                res.append("invalid binder for ").append(componentName).append(" received");
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("shizukuService",componentName +" disconnected");
            Shizuku.bindUserService(userServiceArgs,userServiceConnection);
        }
    };

    private StringBuilder logcat(String tag){

        Process process;
        BufferedReader successResult;
        BufferedReader errorResult;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(COMMAND+tag);
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            //errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while (( line = successResult.readLine()) != null) {
                successMsg.append(line).append("\n");
            }
        }catch (Exception e){

        }
        return successMsg;
    }
}
