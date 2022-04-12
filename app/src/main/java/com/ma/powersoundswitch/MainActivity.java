package com.ma.powersoundswitch;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;


import android.os.IBinder;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.RomUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.ma.powersoundswitch.activity.SettingActivity;
import com.ma.powersoundswitch.fragment.mViewModel;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener{

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCenter.start(getApplication(), "b5f71581-37c7-42a2-b631-45a8a56a17df", Analytics.class, Crashes.class);
        Utils.init(getApplication());
        LogUtils.Config config = LogUtils.getConfig();
        config.setLog2FileSwitch(true);
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
        textview.setText("写入安全设置权限未授权");

        fm = getSupportFragmentManager();
        transition = fm.beginTransaction();

        //FragmentUtils.add(fm,new SettingFragment(),R.id.fragmentContainerView);

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

        String cmd ="cd "+PathUtils.getInternalAppDataPath()+ "/files";

        if (!FileUtils.isFileExists(PathUtils.getInternalAppDataPath()+"/files/rish")){
            initrish();
            LogUtils.i("File Not Fount: "+PathUtils.getInternalAppDataPath()+"/files/rish"+"\nBut We Are fixed it");
        }
    }

    private int checkPermissionStatus(String permission) //校验权限授权状态
    {
         return ContextCompat.checkSelfPermission(getBaseContext(),permission);
    }



    private void checkShizukuStatus() {

        try {

            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                // Granted
                Toast.makeText(getBaseContext(), "已授权shizuku", Toast.LENGTH_SHORT).show();

                ActivityUtils.startActivity(intent);

            } else {
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
            }

        }catch (IllegalStateException e){
            Toast.makeText(this, ""+e.fillInStackTrace(), Toast.LENGTH_SHORT).show();
            try {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Shizuku服务未运行,无法申请权限 \n您希望启动 Shizuku 吗？")
                        .setPositiveButton("启动", (dialog, which) -> {
                            try {
                                Intent intent1 = new Intent().setComponent(new ComponentName("moe.shizuku.privileged.api", "moe.shizuku.manager.MainActivity")).setAction(Intent.ACTION_VIEW);
                                this.startActivityForResult(intent1, 1234);
                            } catch (NullPointerException e1) {
                                LogUtils.e(e1.fillInStackTrace());
                                ToastUtils.showShort("Shizuku未安装或其他原因，未能启动");
                            }
                        })
                        .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {

                        }).show();
            }catch (Exception ignored){}
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


        if (id == 2){
            try {

                List list = Arrays.asList(Arrays.stream(PowerManager.class.getDeclaredMethods()).toArray());
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i<list.size(); i++){
                    stringBuffer.append(list.get(i)+"\n\n");
                }

                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage(PowerManager.class.getSimpleName()+"所有方法\n\n"+stringBuffer)
                        .setPositiveButton(R.string.lab_submit, (dialog, which) -> { })
                        .show();

            }catch (Exception e){
                LogUtils.e(e.fillInStackTrace());
            }

        }

        return super.onOptionsItemSelected(item);
    }


    private void answerCall() {
        PowerManager prMag = (PowerManager) getSystemService(Context.POWER_SERVICE);
        Class<PowerManager> c = PowerManager.class;
        Method mthCall = null;
        try {
            mthCall = c.getDeclaredMethod("reboot", (Class[]) null);
            mthCall.setAccessible(true);
            //IPowerManger iTel = (IPowerManger) mthCall.invoke(prMag,(Object[]) null);
            //iTel.reboot();
            this.finish();
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            e.printStackTrace();
        }
    }

    private Integer checkOps(String a) //调用ops权限管理器校验权限是否真的授权
    {
        AppOpsManager opsMgr = (AppOpsManager) getApplicationContext().getSystemService(APP_OPS_SERVICE);;
        // 检查权限是否已授权
        switch (opsMgr.checkOp(a, android.os.Process.myUid(), getPackageName())) {
            case (AppOpsManager.MODE_ALLOWED):
                LogUtils.i("AppOps", a + "已授权");
                return 0;
            case (AppOpsManager.MODE_IGNORED):
                LogUtils.e(a + " 权限被设置为忽略\n"+ RomUtils.getRomInfo().getName());
                return 1;
            case (AppOpsManager.MODE_ERRORED):
                LogUtils.e(a + " 权限被设置为拒绝\n"+ RomUtils.getRomInfo().getName());
                return 2;
        }
        return null;
    }

    private void initrish() {
        try {
            InputStream is = getBaseContext().getAssets().open("rish");
            inputStream2File(is,new File(PathUtils.getInternalAppDataPath()+"/files/rish"));
            InputStream is2 = getBaseContext().getAssets().open("rish_shizuku.dex");
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
            //ToastUtils.showShort("Shizuku已授权");

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

}
