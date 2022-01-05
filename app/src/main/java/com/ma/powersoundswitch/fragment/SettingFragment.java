package com.ma.powersoundswitch.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ClickUtils;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.ma.powersoundswitch.R;
import com.ma.powersoundswitch.mViewModel;
import com.microsoft.appcenter.AppCenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rikka.shizuku.Shizuku;


public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener,Preference.OnPreferenceChangeListener {

    // TODO: Rename parameter arguments, choose names that match

    private mViewModel viewModel;

    private ContentResolver cr;

    public SettingFragment() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(mViewModel.class); // 实例化发送端
        viewModel.getCallString().observe(this, s -> { //
            // LogUtils.i(s);
        });
        LogUtils.i("已经创建： "+this.getContext());
        findPreference("about").setOnPreferenceClickListener(this);
        findPreference("开关").setOnPreferenceChangeListener(this);
        findPreference("appcanter").setOnPreferenceChangeListener(this);
        findPreference("opensource").setOnPreferenceClickListener(this);
        cr = new ContentResolver(getContext()) {
            @Nullable
            @Override
            public String[] getStreamTypes(@NonNull Uri url, @NonNull String mimeTypeFilter) {
                return super.getStreamTypes(url, mimeTypeFilter);
            }
        };

        String cmd = "export PATH="+PathUtils.getInternalAppDataPath()+ "/files:"+"$PATH"+" " +
                " && echo PATH环境变量: $PATH"+
                " && cd "+PathUtils.getInternalAppDataPath()+ "/files"+
                " && pwd && ls -l ";

        if (!FileUtils.isFileExists(PathUtils.getInternalAppDataPath()+"/files/rish")){
            initRish(cmd);
            LogUtils.i("不存在"+PathUtils.getInternalAppDataPath()+"/files/rish");
        }else {
            LogUtils.i("文件存在");
           //LogUtils.e(ShellUtils.execCmd("/system/bin/sh "+PathUtils.getInternalAppDataPath()+"/files/rish -c whoami",false));
        }

  }

    private void initRish(String cmd) {
        try {
            InputStream is = requireContext().getAssets().open("rish");
            inputStream2File(is,new File(PathUtils.getInternalAppDataPath()+"/files/rish"));
            InputStream is2 = requireContext().getAssets().open("rish_shizuku.dex");
            inputStream2File(is2,new File(PathUtils.getInternalAppDataPath()+"/files/rish_shizuku.dex"));
        }catch (Exception e){
            LogUtils.e(e.fillInStackTrace());
        }

        ShellUtils.execCmd("/system/bin/chmod 777 "+PathUtils.getInternalAppDataPath()+"/files/rish",false);

    }

    /**
     * 将inputStream转化为file
     * @param is
     * @param file 要输出的文件目录
     */
    public static void inputStream2File (InputStream is, File file) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int len = 0;
            byte[] buffer = new byte[8192];

            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } finally {
            os.close();
            is.close();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_settings);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        LogUtils.i(preference.getKey());
        switch (preference.getKey()){
            case "opensource":

                break;
            case "about":
                //viewModel.add(AppUtils.getAppVersionName());
                //ToastUtils.showShort(AppUtils.getAppVersionName());
                new AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setIcon(R.drawable.ic_launcher_foreground)
                        .setTitle(R.string.about)
                        .setMessage(R.string.at)
                        .setPositiveButton(R.string.lab_submit, (dialog, which) -> { })
                        //.setNegativeButton(R.string.lab_cancel, (dialog, which) -> { LogUtils.i(dialog.toString()); })
                        .show();

                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        try {

            switch (preference.getKey()){
                case "开关":
                    if (((boolean) newValue)){
                        //Settings.Global.putInt(cr,"power_sounds_enabled",1);
                        ToastUtils.showShort("已开启");
                       ShellUtils.execCmd("sh "+PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"settings put global power_sounds_enabled 1\" &",false);
                       }else {
                        ToastUtils.showShort("已禁用充电音效");
                        //Settings.Global.putInt(cr,"power_sounds_enabled",0);
                        ShellUtils.execCmd("sh "+PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"settings put global power_sounds_enabled 0\" &",false);
                    }
                    LogUtils.e("电源音状态："+ Settings.Global.getString(cr,"power_sounds_enabled"));
                    break;
                case "appcanter":
                    if (((boolean) newValue)){
                        AppCenter.setEnabled(true);
                    }else {
                        AppCenter.setEnabled(false);
                    }
                    break;
            }

        }catch (Exception e){
            //e.fillInStackTrace();
            LogUtils.e(e.fillInStackTrace());
            ToastUtils.showShort(e.fillInStackTrace().toString());
            //ActivityUtils.finishActivity(requireActivity());
        }

        return true;
    }

   /* private void openCustomTabs(String url) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setShowTitle(true);
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url));
    }*/

}