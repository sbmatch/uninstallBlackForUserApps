package com.ma.powersoundswitch;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ReflectUtils;
import com.blankj.utilcode.util.RomUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ma.powersoundswitch.activity.ContentUriUtil;
import com.ma.powersoundswitch.activity.SettingActivity;
import com.ma.powersoundswitch.fragment.SettingFragment;
import com.ma.powersoundswitch.fragment.mViewModel;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import moe.shizuku.server.IRemoteProcess;
import moe.shizuku.server.IShizukuService;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuApiConstants;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener, View.OnClickListener {

    private Intent intent ;
    private Dialog dialog;
    private static final int REQUEST_CODE = 1000;
    private static Bundle outState;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private mViewModel viewModel;
    private ImageView imageView ,imageView2;
    private MaterialButton button;
    private MaterialCardView cardView,cardView2;
    private TextView textview,textview2,ttextView,ttextView2;
    private  IBinder iBinder ;
    private StatusBarManager mStatusBarManager;
    private FragmentManager fm;
    private FragmentTransaction transition;
    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCenter.start(getApplication(), "b5f71581-37c7-42a2-b631-45a8a56a17df", Analytics.class, Crashes.class);
        Utils.init(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(this, SettingActivity.class);
        cardView = findViewById(R.id.materialCardView1);
        imageView = findViewById(R.id.imageview);
        textview= findViewById(R.id.tv);
        textview2 = findViewById(R.id.tv2);

        cardView.setCardBackgroundColor(getColor(R.color.浅粉色));

       // button = findViewById(R.id.materia_button);
       // button.setOnClickListener(this);

        sp = getSharedPreferences("StartSate",MODE_PRIVATE);
        editor = getSharedPreferences("StartSate",MODE_PRIVATE).edit();

        viewModel = new ViewModelProvider(this).get(mViewModel.class);

        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        textview.setTextSize(18);

        imageView.setImageResource(R.drawable.ic_baseline_priority_high_24);
        textview.setText("写入安全设置"+"\r未授权");

        fm = getSupportFragmentManager();
        transition = fm.beginTransaction();

        FragmentUtils.add(fm,new SettingFragment(),R.id.fragmentContainerView);
        FragmentUtils.show(fm);


        /*if (sp != null) {
            if (!sp.getBoolean("start",false)) {
                ToastUtils.showShort("此应用已拒绝启动,请授权后重试");
                ActivityUtils.startHomeActivity();
                LogUtils.e("此应用已拒绝启动！");
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //AppUtils.exitApp();
                    System.exit(0);
                }).start();
            }else {
                editor.putBoolean("start", true).commit();
                startActivity(intent);
            }
        }*/

        String cmd = "export PATH="+PathUtils.getInternalAppDataPath()+ "/files:"+"$PATH"+" " +
                " && echo PATH环境变量: $PATH"+
                " && cd "+PathUtils.getInternalAppDataPath()+ "/files"+
                " && pwd && ls -l ";

        if (!FileUtils.isFileExists(PathUtils.getInternalAppDataPath()+"/files/rish")){
            initRish(cmd);
            LogUtils.i("File Not Fount: "+PathUtils.getInternalAppDataPath()+"/files/rish"+"\nBut We Are fixed it");
        }else {
            //checkPermissionStatus(Manifest.permission.WRITE_SECURE_SETTINGS);
           // checkShizukuStatus();
            LogUtils.i(ShellUtils.execCmd(cmd,false).successMsg);
            LogUtils.i("rish 已初始化");
        }
    }

    private int checkPermissionStatus(String permission) //调用权限管理器校验权限是否真的授权
    {
        LogUtils.i("正在校验 "+permission+" 权限授权状态");
        return ContextCompat.checkSelfPermission(getBaseContext(),permission);
    }



    private void checkShizukuStatus() {
        try {

            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                // Granted
                LogUtils.i("已授权shizuku");
                //textview.setText("已授权Shizuku");
                //imageView.setImageResource(R.drawable.ic_twotone_done_24);
                textview2.setVisibility(View.GONE);

                if (checkPermissionStatus(Manifest.permission.WRITE_SECURE_SETTINGS) != 0){
                    ShellUtils.execCmd("sh "+ PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"pm grant com.ma.powersoundswitch "+ Manifest.permission.WRITE_SECURE_SETTINGS+ "\" &",false);

                    cardView.setCardBackgroundColor(getColor(R.color.柠檬绿));
                    textview.setText("已授权");
                    textview.setTextColor(Color.WHITE);
                    editor.putBoolean("granted", true).commit();
                    imageView.setImageResource(R.drawable.ic_twotone_done_24);
                    //startActivity(intent);
                }else {
                    LogUtils.i("写入安全设置已授权");
                   // ToastUtils.showShort("写入安全设置已授权");
                    cardView.setCardBackgroundColor(getColor(R.color.柠檬绿));
                    textview.setText("已授权");
                    textview.setTextColor(Color.WHITE);
                    editor.putBoolean("granted", true).commit();
                    imageView.setImageResource(R.drawable.ic_twotone_done_24);
                }

            } else {

               if (checkPermissionStatus(Manifest.permission.WRITE_SECURE_SETTINGS) != 0){
                // Request the permission
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("权限申请")
                            .setMessage("要授权 Shizuku 吗")
                            .setPositiveButton(R.string.lab_submit, (dialog, which) -> {
                                Shizuku.requestPermission(REQUEST_CODE);
                            })
                            .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {
                                LogUtils.i(dialog.toString());
                            })
                            .show();
               }else {

                if (checkPermissionStatus(Manifest.permission.WRITE_SECURE_SETTINGS) != 0){
                    ShellUtils.execCmd("sh "+ PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"pm grant com.ma.powersoundswitch "+ Manifest.permission.WRITE_SECURE_SETTINGS+ "\" &",false);

                    cardView.setCardBackgroundColor(getColor(R.color.柠檬绿));
                    textview.setText("已授权");
                    textview.setTextColor(Color.WHITE);
                    editor.putBoolean("granted", true).commit();
                    imageView.setImageResource(R.drawable.ic_twotone_done_24);
                    //startActivity(intent);
                }else {
                    LogUtils.i("写入安全设置已授权");
                    cardView.setCardBackgroundColor(getColor(R.color.柠檬绿));
                    textview.setText("已授权");
                    textview.setTextColor(Color.WHITE);
                    editor.putBoolean("granted", true).commit();
                    imageView.setImageResource(R.drawable.ic_twotone_done_24);
                }
               }
            }

        }catch (IllegalStateException e){
            LogUtils.e(e.fillInStackTrace());
            try {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("运行环境检查")
                        .setMessage("Shizuku服务未运行")
                        .setPositiveButton("启动Shizuku", (dialog, which) -> {
                            try {
                                Intent intent1 = new Intent().setComponent(new ComponentName("moe.shizuku.privileged.api", "moe.shizuku.manager.MainActivity")).setAction(Intent.ACTION_VIEW);
                                this.startActivityForResult(intent1, 1234);
                            } catch (NullPointerException e1) {
                                LogUtils.e(e1.fillInStackTrace());
                                ToastUtils.showShort("Shizuku未安装或其他原因，未能启动");
                            }
                        })
                        .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {
                            LogUtils.i(dialog.toString());
                        }).show();
            }catch (Exception e1){
                LogUtils.e(e1.fillInStackTrace());
            }
            //ToastUtils.showShort("Shizuku服务未运行");
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

      //  LogUtils.e("Activity 返回");
        if (resultCode == RESULT_OK){
            if (requestCode == 1234) {
                checkShizukuStatus();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    private void openCustomTabs(String url) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setShowTitle(true);
        intentBuilder.setInstantAppsEnabled(true);
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.add(0,1,0,"反馈问题").setIcon(R.drawable.ic_baseline_bug_report_24);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, @NonNull Menu menu) {

        if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
            try {
                Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(menu, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onMenuOpened(featureId, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            ActivityUtils.startActivity(intent);
            //return true;
        }
        if (id == 1) {
            //checkShizukuStatus();
            Uri uri = Uri.parse("https://tenapi.cn/qq/?qq=3207754367");

            Intent intent3 = new Intent(Intent.ACTION_SENDTO).setData(Uri.parse("mailto:3207754367@qq.com"));
            startActivity(intent3);

            //startActivity(new Intent(Intent.ACTION_VIEW,uri).setClassName("com.android.browser","com.android.browser.BrowserActivity"));
                   // .setClassName("com.tencent.mobileqq","com.tencent.mobileqq.activity.QQBrowserActivity"));
           // openCustomTabs("https://tenapi.cn/qq/?qq=3207754367");
        }

        return super.onOptionsItemSelected(item);
    }

    private void Ops检查权限状态(String a) //调用ops权限管理器校验权限是否真的授权
    {
        AppOpsManager opsMgr = (AppOpsManager) getApplicationContext().getSystemService(APP_OPS_SERVICE);;
        // 检查权限是否已授权
        switch (opsMgr.checkOp(a, android.os.Process.myUid(), getPackageName())) {
            case (AppOpsManager.MODE_ALLOWED):
                LogUtils.i("AppOps", a + "已授权");
                switch (a){
                    case "android:audio_media_volume":
                        break;
                    case "android:write_secure_settings":
                        ToastUtils.showShort("已授权"+a);
                        break;
                }
                break;
            case (AppOpsManager.MODE_IGNORED):
                LogUtils.e(a + " 权限被设置为忽略\n"+"锁我喉是吧!? 搞偷袭!? 小伙子，你不讲武德\n"+ RomUtils.getRomInfo().getName()+" 是吧？ 算你狠");
            case (AppOpsManager.MODE_ERRORED):
                LogUtils.e(a + " 权限被设置为拒绝\n"+"锁我喉是吧!? 搞偷袭!? 小伙子，你不讲武德\n"+ RomUtils.getRomInfo().getName()+" 是吧？ 算你狠");
                break;
        }
    }

    private void 申请权限(String 权限) {

        PermissionUtils.permission(权限).callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                LogUtils.i("成功获取权限："+权限);
                ToastUtils.showLong(权限+" 已授权");
            }
            @Override
            public void onDenied() {

            }
        }).explain((activity, denied, shouldRequest) -> {
            LogUtils.e(denied.toString());
        }).request();
    }

    private void initRish(String cmd) {
        try {
            InputStream is = getBaseContext().getAssets().open("rish");
            inputStream2File(is,new File(PathUtils.getInternalAppDataPath()+"/files/rish"));
            InputStream is2 = getBaseContext().getAssets().open("rish_shizuku.dex");
            inputStream2File(is2,new File(PathUtils.getInternalAppDataPath()+"/files/rish_shizuku.dex"));
        }catch (Exception e){
            LogUtils.e(e.fillInStackTrace());
        }

        ShellUtils.execCmd("/system/bin/chmod 777 "+PathUtils.getInternalAppDataPath()+"/files/rish",false);
        //Ops检查权限状态(AppOpsManager.permissionToOp(Manifest.permission.WRITE_SECURE_SETTINGS));
        //checkPermissionStatus(Manifest.permission.WRITE_SECURE_SETTINGS);
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
    protected void onPause() {
        super.onPause();
        outState = new Bundle();
        outState.putBoolean("start", false);
        //onSaveInstanceState(outState);
        editor.putBoolean("start", false).commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
      //  LogUtils.e("进入重载状态");
       // ToastUtils.showShort("进入暂停状态");
        checkShizukuStatus();

    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {

        if (grantResult == 0){
            LogUtils.i("太好了，Shizuku授权完成");
            ToastUtils.showShort("Shizuku已授权");
            //imageView.setImageResource(R.drawable.ic_twotone_done_24);
            textview2.setVisibility(View.GONE);

            ShellUtils.execCmd("sh "+ PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"pm grant com.ma.powersoundswitch "+ Manifest.permission.WRITE_SECURE_SETTINGS+ "\" &",false);

            checkShizukuStatus();

        }else {

            try {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("权限申请")
                        .setMessage("因本应用仅适配Shizuku \n\n如需继续使用请授权，否则程序无法工作\n\n您想再次授权吗？")
                        .setPositiveButton("授权", (dialog, which) -> {
                            Shizuku.requestPermission(REQUEST_CODE);
                        })
                        .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {
                            viewModel.add("未授权");
                            ToastUtils.showShort("好的，我们尊重您的决定");
                        }).show();

            }catch (Exception e2){
                LogUtils.e(e2.fillInStackTrace());
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

        }
    }
}
