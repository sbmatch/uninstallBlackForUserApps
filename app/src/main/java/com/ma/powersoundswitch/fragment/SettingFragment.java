package com.ma.powersoundswitch.fragment;


import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_SENDTO;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.RomUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.ma.powersoundswitch.R;
import com.ma.powersoundswitch.activity.ContentUriUtil;
import com.microsoft.appcenter.AppCenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;


public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener,Preference.OnPreferenceChangeListener {

    // TODO: Rename parameter arguments, choose names that match

    private mViewModel viewModel;

    private ContentResolver cr;

    private Preference about,AppCanter,开源,电源音,低电量音,自定义低电量音路径,锁屏音,自定义锁屏音路径,解锁音,自定义解锁音路径,bugreply;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String NullOgg;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                // Handle the returned Uri
                if (uri != null) {
                    自定义低电量音路径.setSummary(ContentUriUtil.getPath(requireContext(), uri));
                    Settings.Global.putString(cr, "low_battery_sound",ContentUriUtil.getPath(requireContext(), uri));
                    ToastUtils.showShort(getString(R.string.low_battery_sound) + "已设置为\n" + ContentUriUtil.getPath(requireContext(), uri));
                    LogUtils.i("原始Uri：" + uri + "\n文件真实路径：" + ContentUriUtil.getPath(requireContext(), uri));
                }
            });

    ActivityResultLauncher<String> mGetContent2 = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                // Handle the returned Uri
                if (uri != null) {
                    自定义锁屏音路径.setSummary(ContentUriUtil.getPath(requireContext(), uri));
                    Settings.Global.putString(cr, "lock_sound",ContentUriUtil.getPath(requireContext(), uri));
                    ToastUtils.showShort(getString(R.string.low_battery_sound) + "已设置为\n" + ContentUriUtil.getPath(requireContext(), uri));
                    LogUtils.i("原始Uri：" + uri + "\n文件真实路径：" + ContentUriUtil.getPath(requireContext(), uri));
                }
            });

    ActivityResultLauncher<String> mGetContent3 = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                // Handle the returned Uri
                if (uri != null) {
                    自定义解锁音路径.setSummary(ContentUriUtil.getPath(requireContext(), uri));
                    Settings.Global.putString(cr, "unlock_sound",ContentUriUtil.getPath(requireContext(), uri));
                    ToastUtils.showShort(getString(R.string.low_battery_sound) + "已设置为\n" + ContentUriUtil.getPath(requireContext(), uri));
                    LogUtils.i("原始Uri：" + uri + "\n文件真实路径：" + ContentUriUtil.getPath(requireContext(), uri));
                }
            });

    public SettingFragment() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(mViewModel.class); // 实例化发送端

        about = findPreference("about");
        AppCanter =findPreference("appcanter");
        开源 = findPreference("opensource");
        电源音 =findPreference("power_sound");
        低电量音 = findPreference("low_battery_sound");
        自定义低电量音路径 = findPreference("low_battery_sound_path");
        锁屏音 = findPreference("lock_sound");
        自定义锁屏音路径 = findPreference("lock_sound_path");
        解锁音 = findPreference("unlock_sound");
        自定义解锁音路径 = findPreference("unlock_sound_path");
        bugreply = findPreference("bugreply");

        ToastUtils.make().setDurationIsLong(true).setLeftIcon(R.drawable.ic_baseline_error_24).setGravity(Gravity.CENTER,0,0).setMode(ToastUtils.MODE.DARK).setTextSize(24).show("设置后重启即可生效");

        about.setOnPreferenceClickListener(this);
        AppCanter.setOnPreferenceChangeListener(this);
        开源.setOnPreferenceClickListener(this);
        电源音.setOnPreferenceChangeListener(this);
        低电量音.setOnPreferenceChangeListener(this);
        自定义低电量音路径.setOnPreferenceClickListener(this);
        锁屏音.setOnPreferenceChangeListener(this);
        自定义锁屏音路径.setOnPreferenceClickListener(this);
        解锁音.setOnPreferenceChangeListener(this);
        自定义解锁音路径.setOnPreferenceClickListener(this);
        bugreply.setOnPreferenceClickListener(this);

        sp = requireContext().getSharedPreferences("status",MODE_PRIVATE);
        editor = requireContext().getSharedPreferences("status",MODE_PRIVATE).edit();

        cr = new ContentResolver(getContext()) {
            @Nullable
            @Override
            public String[] getStreamTypes(@NonNull Uri url, @NonNull String mimeTypeFilter) {
                return super.getStreamTypes(url, mimeTypeFilter);
            }
        };

        initConfig();
        initNullOggPath();
    }

    private void initNullOggPath() {
        //准备资源
        InputStream is = getResources().openRawResource(R.raw.disable);
        FileIOUtils.writeFileFromIS(PathUtils.getExternalAppMusicPath() + "/" + "disable.ogg", is);
        NullOgg = PathUtils.getExternalAppMusicPath() + "/" + "disable.ogg";
    }

    private void initConfig() {
        editor.putString("low_battery_sound_path","/system/media/audio/ui/LowBattery.ogg").commit();
        editor.putString("lock_sound_path","/system/media/audio/ui/Lock.ogg").commit();
        editor.putString("unlock_sound_path","/system/media/audio/ui/Unlock.ogg").commit();
    }


    private int checkPermissionStatus(String permission) //校验权限是否授权
    {
        if (ContextCompat.checkSelfPermission(requireContext(),permission) != 0) {
            LogUtils.e(permission+" 未授权");

            ShellUtils.execCmd("sh "+ PathUtils.getInternalAppDataPath()+"/files/rish -c "
                    +"\"pm grant com.ma.powersoundswitch "
                    + Manifest.permission.WRITE_SECURE_SETTINGS
                    + "\" &",false);

        }else {
            LogUtils.i("已备份默认数据"+sp.getAll());

        }
        return ContextCompat.checkSelfPermission(requireContext(),permission);
    }


    private void saveConfigInfo(){
        switch (RomUtils.getRomInfo().getName()){
            case "xiaomi":
            //case "":
                if (Settings.Global.getString(cr,"low_battery_sound").equals(sp.getString("low_battery_sound_path",""))) {
                    LogUtils.i("已适配" + RomUtils.getRomInfo().getName());
                }
                break;
            default:
                ToastUtils.make().setGravity(Gravity.CENTER,0,0).setMode(ToastUtils.MODE.DARK).show("抱歉，本应用仅适配了"+RomUtils.getRomInfo().getName()+"\n\n可联系开发者申请适配");
                LogUtils.e("当前设备未适配："+RomUtils.getRomInfo().getName());
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
        switch (preference.getKey()){
            case "low_battery_sound_path":
                ToastUtils.showShort("请选择ogg格式文件");

                mGetContent.launch("audio/ogg");
                //startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("audio/ogg").addCategory(Intent.CATEGORY_OPENABLE), 666);

                break;
            case "lock_sound_path":
                ToastUtils.showShort("请选择ogg格式文件");

                mGetContent2.launch("audio/ogg");

                break;
            case "unlock_sound_path":
                ToastUtils.showShort("请选择ogg格式文件");
                mGetContent3.launch("audio/ogg");
                break;
            case "bugreply":
               /* try {
                    File file = new File(String.valueOf(LogUtils.getLogFiles().get(0)));

                    requireActivity().startActivity(new Intent(ACTION_SEND).putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                            .putExtra(Intent.EXTRA_SUBJECT, AppUtils.getAppName() +" 用户反馈")
                            .putExtra(Intent.EXTRA_TEXT, "你好，我亲爱的用户。\n是遇到了什么问题或者有什么意见想告诉我什么吗?\n附件是本应用产生的log，它将帮助我更好的定位问题" + "\n") //正文
                            .setData(Uri.parse("mailto:3207754367@qq.com")));

                }catch (IndexOutOfBoundsException outOfBoundsException){
                    LogUtils.e(outOfBoundsException.fillInStackTrace()+"\n"+LogUtils.getLogFiles());
                }*/
                openCustomTabs("https://privacy.mpcloud.top/valine");

                break;
            case "opensource":
                openCustomTabs("https://github.com/sbmatch/powersoundswitch");
                break;
            case "about":
                //ToastUtils.showShort(AppUtils.getAppVersionName());
                new AlertDialog.Builder(requireContext())
                        .setCancelable(false)
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

        if (checkPermissionStatus(Manifest.permission.WRITE_SECURE_SETTINGS) == 0)
        {
            try {
                switch (preference.getKey()) {
                    case "power_sound":
                            if (((boolean) newValue)) {
                                LogUtils.i("已开启");
                                Settings.Global.putInt(cr, "power_sounds_enabled", 1);
                                ToastUtils.showShort("已开启");
                                //ShellUtils.execCmd("sh "+PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"settings put global power_sounds_enabled 1\" &",false);
                            } else {
                                ToastUtils.showShort("已禁用" + getString(R.string.power_sound));
                                //LogUtils.e(newValue);
                                Settings.Global.putInt(cr, "power_sounds_enabled",0);
                                //ShellUtils.execCmd("sh "+PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"settings put global power_sounds_enabled 0\" &",false);
                            }
                        LogUtils.e("电源音状态：" + Settings.Global.getString(cr, "power_sounds_enabled"));
                        break;
                    case "low_battery_sound":
                        LogUtils.i(preference.getKey()+" is "+newValue);
                        if (((boolean) newValue)) {
                           // ToastUtils.showShort(getString(R.string.low_battery_sound) + "已设置为\n" + Settings.Global.getString(cr, "low_battery_sound"));

                            /*viewModel.getCallString().observe(this, s -> {
                                Settings.Global.putString(cr, "low_battery_sound", s);
                                p2.setSummary(s);
                                ToastUtils.showShort(getString(R.string.low_battery_sound) + "已设置为\n" + s);
                            });*/

                            Settings.Global.putString(cr, "low_battery_sound",sp.getString("low_battery_sound_path",""));
                            自定义低电量音路径.setSummary("当前是系统默认值");
                            ToastUtils.showShort("已恢复为默认值");

                        }else {
                            Settings.Global.putString(cr, "low_battery_sound",NullOgg);
                            ToastUtils.showShort(getString(R.string.low_battery_sound) + "已禁用");
                            自定义低电量音路径.setSummary("已禁用");
                        }
                        break;
                    case "lock_sound":
                        LogUtils.i(preference.getKey()+" is "+newValue);
                        if (((boolean) newValue)) {

                            Settings.Global.putString(cr, "lock_sound",sp.getString("lock_sound_path",""));
                            自定义锁屏音路径.setSummary("当前是系统默认值");
                            ToastUtils.showShort("已恢复为默认值");
                            LogUtils.i("已恢复为默认值"+sp.getString("lock_sound_path",""));

                        }else {
                            Settings.Global.putString(cr, "lock_sound",NullOgg);
                            ToastUtils.showShort(getString(R.string.lock_sound) + "已禁用");
                            自定义锁屏音路径.setSummary("已禁用");

                        }
                        break;
                    case "unlock_sound":
                        LogUtils.i(preference.getKey()+" is "+newValue);
                        if (((boolean) newValue)) {

                            /*viewModel.getCallString().observe(this, s -> {
                                Settings.Global.putString(cr, "unlock_sound", s);
                                p9.setSummary(s);
                                ToastUtils.showShort(getString(R.string.low_battery_sound) + "已设置为\n" + s);
                            });*/

                            Settings.Global.putString(cr, "unlock_sound",sp.getString("unlock_sound_path",""));
                            自定义解锁音路径.setSummary("当前是系统默认值");
                            ToastUtils.showShort("已恢复为默认值");
                            LogUtils.i("已恢复为默认值"+sp.getString("unlock_sound",""));

                        }else {
                            Settings.Global.putString(cr, "unlock_sound",NullOgg);
                            ToastUtils.showShort(getString(R.string.lock_sound) + "已禁用");
                            自定义解锁音路径.setSummary("已禁用");
                        }
                        break;
                    case "appcanter":
                        if (((boolean) newValue)) {
                            AppCenter.setEnabled(true);
                        } else {
                            AppCenter.setEnabled(false);
                        }
                        break;
                }

            } catch (Exception e) {
                //e.fillInStackTrace();
                LogUtils.e(e.fillInStackTrace());
                ToastUtils.showShort(e.fillInStackTrace().toString());
                //ActivityUtils.finishActivity(requireActivity());
            }
        }else {
            ToastUtils.showShort("未授权");
        }
        return true;
    }

    private void openCustomTabs(String url) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setShowTitle(true);
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url));
    }

}