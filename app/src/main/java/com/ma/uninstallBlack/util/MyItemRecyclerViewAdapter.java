package com.ma.uninstallBlack.util;

import static com.ma.uninstallBlack.MainActivity.sp;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.ma.uninstallBlack.MainActivity;
import com.ma.uninstallBlack.R;
import com.ma.uninstallBlack.beans.itemBean;
import com.ma.uninstallBlack.service.MyWorkService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;


public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    public static List<itemBean> mValues;
    public View view;
    public static final String LOG_TAG = "RecyclerViewAdapter";

    public MyItemRecyclerViewAdapter(List<itemBean> mValues) {
        MyItemRecyclerViewAdapter.mValues = mValues;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ViewTreeObserver.OnPreDrawListener listener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (MainActivity.alertDialog != null && MainActivity.alertDialog.isShowing()){
                    MainActivity.alertDialog.cancel();
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        };
        view = View.inflate(parent.getContext(), R.layout.item_layout,null);
        if (view.getViewTreeObserver().isAlive()){
            view.getViewTreeObserver().addOnPreDrawListener(listener);
        }
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            holder.setData(mValues.get(position));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView mImgView;
        public AppCompatTextView mTextView,mPackageName;
        public SwitchMaterial mSwitchMaterial;
        public boolean zz = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mImgView = itemView.findViewById(R.id.item_appCompatImageView);
            this.mTextView = itemView.findViewById(R.id.item_appCompatTextView);
            this.mPackageName = itemView.findViewById(R.id.item_text_packageName);
            this.mSwitchMaterial = itemView.findViewById(R.id.item_switchWidget);
        }

        public void setData(itemBean itemBean) throws RemoteException {
            mTextView.setText(itemBean.AppName);
            mImgView.setImageDrawable(itemBean.icon);
            mPackageName.setText(itemBean.packageName);

            mSwitchMaterial.setOnClickListener(v -> {

                if (OtherUtils.isServiceRunning(MyWorkService.class.getName()) && sp.getBoolean("是否绑定userService",false)){
                    if (zz){
                        switchBlockUninstallForUserReflect(itemBean.packageName,true);
                        Log.i(LOG_TAG,mTextView.getText()+" 已开启, 当前禁止卸载状态："+OtherUtils.getBlockUninstallForUserReflect(itemBean.packageName,0));
                        MainActivity.editor.putBoolean(itemBean.packageName,true).commit();
                        ToastUtils.showShort(itemBean.AppName+" 已禁止卸载");
                    }else {
                        switchBlockUninstallForUserReflect(itemBean.packageName,false);
                        MainActivity.editor.putBoolean(itemBean.packageName,false).commit();
                        ToastUtils.showShort(itemBean.AppName+" 已允许卸载");
                        Log.i(LOG_TAG,mTextView.getText()+" 已关闭, 当前禁止卸载状态："+OtherUtils.getBlockUninstallForUserReflect(itemBean.packageName,0));
                    }
                }else {
                    mSwitchMaterial.setChecked(itemBean.isUnBlack);
                    OtherUtils.showNotificationReflect("空指针异常");
                }

            });
            mSwitchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
                zz = isChecked;
            });

            boolean z = sp.getBoolean(itemBean.packageName,itemBean.isUnBlack); // 从 SharedPreferences 获取应用状态
            mSwitchMaterial.setChecked(z);
        }
    }

    public static void switchBlockUninstallForUserReflect(String pkg, boolean blockUninstall) {
        if (MainActivity.iSwitchBlockUninstall != null){
            Log.w(LOG_TAG,"正在调用接口...");
            try{
                OtherUtils.userService.setBlockUninstallForUser(pkg,blockUninstall,0);
            }catch (Throwable e){
                Log.e(LOG_TAG,e.toString());
            }
        }
    }
}