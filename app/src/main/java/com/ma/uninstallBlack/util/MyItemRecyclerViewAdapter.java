package com.ma.uninstallBlack.util;

import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.ma.uninstallBlack.MainActivity;
import com.ma.uninstallBlack.R;
import com.ma.uninstallBlack.beans.itemBean;

import java.util.List;


public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    public static List<itemBean> mValues;
    public View view;
    private static final String TAG = "RecyclerViewAdapter";

    public MyItemRecyclerViewAdapter(List<itemBean> mValues) {
        MyItemRecyclerViewAdapter.mValues = mValues;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        view = View.inflate(parent.getContext(), R.layout.item_layout,null);
        if (view.getViewTreeObserver().isAlive()){
            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (MainActivity.alertDialog.isShowing()){
                        MainActivity.alertDialog.cancel();
                    }
                    return true;
                }
            });
        }
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //holder.mItem = mValues.get(position);
        //holder.mIdView.setText(mValues.get(position).id);
        //holder.mContentView.setText(mValues.get(position).content);
        holder.setData(mValues.get(position));
    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView mImgView;
        public AppCompatTextView mTextView,mPackageName;
        public SwitchMaterial mSwitchMaterial;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mImgView = itemView.findViewById(R.id.item_appCompatImageView);
            this.mTextView = itemView.findViewById(R.id.item_appCompatTextView);
            this.mPackageName = itemView.findViewById(R.id.item_text_packageName);
            this.mSwitchMaterial = itemView.findViewById(R.id.item_switchWidget);
        }

        public void setData(itemBean itemBean) {
            mTextView.setText(itemBean.AppName);
            mImgView.setImageDrawable(itemBean.icon);
            mPackageName.setText(itemBean.packageName);
            mSwitchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (isChecked){
                    try {
                        MainActivity.appIPC.switchUninstallBlack(itemBean.packageName,true);
                        Log.i(TAG,mTextView.getText()+" 已开启, 当前禁止卸载状态："+MainActivity.appIPC.isUninstallBlack(itemBean.packageName));
                        MainActivity.editor.putBoolean(itemBean.packageName,true).commit();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }else {
                    try {
                        MainActivity.appIPC.switchUninstallBlack(itemBean.packageName,false);
                        MainActivity.editor.putBoolean(itemBean.packageName,false).commit();
                        Log.i(TAG,mTextView.getText()+" 已关闭, 当前禁止卸载状态："+MainActivity.appIPC.isUninstallBlack(itemBean.packageName));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });

            boolean z = MainActivity.sp.getBoolean(itemBean.packageName,false);
            mSwitchMaterial.setChecked(z);
        }
    }
}