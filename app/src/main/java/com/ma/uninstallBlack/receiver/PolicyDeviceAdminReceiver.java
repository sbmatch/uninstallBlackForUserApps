package com.ma.uninstallBlack.receiver;

import static com.ma.uninstallBlack.util.OtherUtils.dpm;

import android.annotation.SuppressLint;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.ma.uninstallBlack.BuildConfig;
import com.ma.uninstallBlack.util.OtherUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class PolicyDeviceAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);

        try {
            @SuppressLint("SystemApi")
            Method method = dpm.getClass().getMethod("getSystemUpdatePolicy");
            SystemUpdatePolicy policy = (SystemUpdatePolicy) method.invoke(dpm);
            @SuppressLint("SoonBlockedPrivateApi")
            Field field = policy.getClass().getDeclaredField("mPolicyType");
            field.setAccessible(true);
            field.setInt(policy,3);
            OtherUtils.setSystemUpdatePolicyReflect(policy);
            Log.w("SystemUpdatePolicy","系统更新政策："+ OtherUtils.getSystemUpdatePolicyReflect());
        } catch (Exception e) {
            e.printStackTrace();
        }

        OtherUtils.clearDeviceOwnerApp();
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        super.onDisabled(context, intent);
    }
}
