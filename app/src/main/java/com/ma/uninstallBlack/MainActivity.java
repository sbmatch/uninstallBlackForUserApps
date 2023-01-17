package com.ma.uninstallBlack;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.PermissionChecker;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.ma.uninstallBlack.beans.itemBean;
import com.ma.uninstallBlack.fragment.mViewModel;
import com.ma.uninstallBlack.receiver.MyBroadcastReceiver;
import com.ma.uninstallBlack.service.MyWorkService;
import com.ma.uninstallBlack.util.MyItemRecyclerViewAdapter;
import com.ma.uninstallBlack.util.OtherUtils;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener, Shizuku.OnBinderReceivedListener{

    private Intent intent ;
    private Dialog dialog, desc;
    private static final int REQUEST_CODE = 1000;
    public static final int JOB_ID = 540128;
    public static SharedPreferences sp;
    public static SharedPreferences.Editor editor;
    public static mViewModel viewModel;
    static  String LOG_TAG = MainActivity.class.getSimpleName();
    public MyItemRecyclerViewAdapter myItemRecyclerViewAdapter;
    public static RecyclerView.LayoutManager manager;
    public static RecyclerView recyclerView;
    public static IAppIPC appIPC;
    public static DocumentFile documentFile;
    public static AlertDialog alertDialog;
    public MainAsyncTask mainAsyncTask = new MainAsyncTask();
    public FirebaseAnalytics firebaseAnalytics;
    public final FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
    private AppCompatTextView msgW;
    public static ISwitchBlockUninstall iSwitchBlockUninstall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (firebaseAnalytics == null){
            firebaseAnalytics =  FirebaseAnalytics.getInstance(this);
        }

        setContentView(R.layout.activity_main);
        ActionBar actionBar = MainActivity.this.getSupportActionBar();
        assert actionBar != null;
        actionBar.setBackgroundDrawable(null);

        sp = getSharedPreferences("StartSate",MODE_PRIVATE);
        editor = sp.edit();

        Shizuku.addRequestPermissionResultListener(MainActivity.this);
        Shizuku.addBinderReceivedListener(MainActivity.this);

        desc = new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("关于卸载管理的说明")
                //.setCancelable(false)
                .setMessage(R.string.uninstall_desc)
                .setNegativeButton("确定", (dialog, which) -> dialog.cancel())
                .create();

        recyclerView = findViewById(R.id.recyclerView);

//        try {
//            for (String s : getBaseContext().getResources().getAssets().list("arm64-v8a")) {
//
//                if (!FileUtils.isFileExists(PathUtils.getInternalAppDataPath()+"/files/"+s)){
//                    inputStream2File(getBaseContext().getAssets().open("arm64-v8a/"+s),new File(PathUtils.getInternalAppDataPath()+"/files/"+s));
//                    ShellUtils.exec("chmod -R 7777  "+ PathUtils.getInternalAppDataPath()+"/files/");
//                    editor.putBoolean("initWatch",true).commit();
//                }
//
//            }
//
////            if (new File(PathUtils.getInternalAppDataPath()+"/files/inotifywait").exists()){
////                Log.w(LOG_TAG,ShellUtils.exec("ls -l "+ PathUtils.getInternalAppDataPath()+"/files/").toString());
////               // LogUtils.w(com.blankj.utilcode.util.ShellUtils.execCmd(PathUtils.getInternalAppDataPath()+"/files/inotifywait"+"  -m --format '%e:%w%f' "+s,false).errorMsg);
////            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getTitle().equals(getString(R.string.aboutBlackUninstall))){
            desc.show();
        }

        if (item.getTitle().equals(getString(R.string.removeNeteaseAd))){
//            activityResultASF.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
//                    .putExtra("android.provider.extra.SHOW_ADVANCED",true)
//                    .putExtra("android.content.extra.SHOW_ADVANCED",true)
//                    .putExtra(DocumentsContract.EXTRA_INITIAL_URI,DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents","9420"))
//                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION));

            //ComponentName cmp = new ComponentName("com.android.permissioncontroller","com.android.permissioncontroller/.permission.ui.ManagePermissionsActivity");
            Intent data = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION").setData(Uri.parse("package:"+BuildConfig.APPLICATION_ID));
            if (OtherUtils.checkOps(Manifest.permission.MANAGE_EXTERNAL_STORAGE) != 0){
                manager_all_files_Result.launch(data);
            }else {
                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setMessage("我们使用 Apache Commons-IO 库中的 FileAlterationMonitor类监听网易云音乐的广告相关文件并进行删除且在同时创建一个同名文件，以此实现去广告的目的, 由于Google对高版本Android平台的相关限制已解除\n\n您可以选择现在开始监听或停止监听")
                        .setNegativeButton("停止监听", (dialog, which) -> iSwitchBlockUninstall.setRemoveAd(false))
                        .setNeutralButton("监听,之后不再询问", (dialog, which) -> {
                            iSwitchBlockUninstall.setRemoveAd(true);
                            editor.putBoolean("之后是否直接监听",true).commit();
                        })
                        .setPositiveButton("忘记我的选择", (dialog, which) -> editor.putBoolean("之后是否直接监听",false).commit())
                        .create().show();
            }

        }

        if (item.getTitle().equals(getString(R.string.removePowersave))){

        }


//
//        if (item.getTitle().equals("暴露通知")){
//
//            ExposureNotificationClient client = Nearby.getExposureNotificationClient(getBaseContext());
//
//            client.start().addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void unused) {
//                    Log.i("exposure",unused.toString());
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.e("exposure",e.getMessage(),e.fillInStackTrace());
//                    Status status = ((ApiException) e).getStatus();
//
//                    Log.e(LOG_TAG,"StatusCode: "+status.getStatusCode()+" ConnectionResult: "+status.getConnectionResult());
//
//                    if (status.getStatusCode() ==  17){
//                        new MaterialAlertDialogBuilder(MainActivity.this)
//                                .setCancelable(false)
//                                .setTitle("")
//                                .setMessage("客户端尝试从连接失败的 API 调用方法。可能的原因包括：\n" +
//                                        "\n" +
//                                        "API 以前无法连接并出现可解决的错误，但用户拒绝了解析。\n" +
//                                        "设备不支持 GmsCore。\n" +
//                                        "特定 API 无法在此设备上连接。")
//                                .setPositiveButton(R.string.lab_submit, (dialog, which) -> {
//                                    ClipboardUtils.copyText(e.getMessage());
//
//                                })
//                                .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {
//
//                                }).show();
//                    }
//                }
//            });
//
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBinderReceived() {
        editor.putBoolean("Shizuku是否正在运行",true).commit();
    }

//    private final ServiceConnection connection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//
//        }
//    };


    @Override
    protected void onResume() {
        super.onResume();
        Intent w = new Intent(MainActivity.this, MyWorkService.class);
        if (sp.getBoolean("是否拿到Shizuku授权",false)){

            if (!OtherUtils.isServiceRunning(MainActivity.this,MyWorkService.class.getName())){
                Log.i(LOG_TAG,"正在启动前台服务：workService ");
                startForegroundService(w);
            }else {
                if (sp.getBoolean("之后是否直接监听",false)){
                    iSwitchBlockUninstall.setRemoveAd(true);
                }
            }

            if (alertDialog !=  null){
                alertDialog.show();
            }

            show_item();

        }else {
            checkShizukuPermission();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @SuppressLint("StaticFieldLeak")
    public class MainAsyncTask extends AsyncTask<String,Void,Object>{

        public RelativeLayout.LayoutParams params,params1;
        public MaterialAlertDialogBuilder builder;
        public ProgressBar progressBar;
        public AppCompatTextView textView;
        public RelativeLayout relativeLayout;

        @Override
        protected String doInBackground(String... s) {

            this.relativeLayout = new RelativeLayout(MainActivity.this);
            this.builder = new MaterialAlertDialogBuilder(MainActivity.this);
            this.progressBar = new ProgressBar(MainActivity.this,null);
            this.progressBar.setId(R.id.main_dialog_progress);
            this.progressBar.setMax(100);
            new Thread(() -> {
                for (int i = 0; i<100; i++){
                    this.progressBar.setProgress(i);
                }
            }).start();
            this.textView = new AppCompatTextView(MainActivity.this);
            this.textView.setText("正在加载数据");
            this.textView.setTextSize(20);
            this.textView.setId(R.id.main_dialog_text);
            this.params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            this.params.setMargins(24,6,6,6);
            this.params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            this.params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            this.params1.addRule(RelativeLayout.CENTER_IN_PARENT);

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            editor.putBoolean("是否拿到Shizuku授权",false).commit();
            if (!sp.getBoolean("是否成功获取workService的接口",false)){
                editor.putBoolean("是否成功获取workService的接口",false).commit();
            }
            editor.putBoolean("Shizuku是否正在运行",false).commit();
        }

        @Override
        protected void onPostExecute(Object o) {
            this.relativeLayout.addView(progressBar,params);
            this.relativeLayout.addView(textView,params1);
            this.builder.setView(relativeLayout);
            this.builder.setCancelable(false);
            alertDialog = builder.create();
            //o = alertDialog;
            super.onPostExecute(o);

        }
    }

//    private final ServiceConnection connection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            appIPC = IAppIPC.Stub.asInterface(service);
//            try {
//                if (!appIPC.isUserServiceBinded()){
//                    Log.w(LOG_TAG,"未绑定shizuku userservice 正在绑定");
//                    appIPC.bus();
//                }
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//            editor.putBoolean("是否成功获取workService的接口",true).commit();
//            show_item();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//
//        }
//    };

    private void show_item() {
        List<itemBean> itemBeans = new ArrayList<itemBean>(){};
        List<PackageInfo> infos = getPackageManager().getInstalledPackages(0);

        for (PackageInfo i: infos){
            itemBean d =new itemBean();
            d.AppName = i.applicationInfo.loadLabel(getPackageManager()).toString();
            d.icon = i.applicationInfo.loadIcon(getPackageManager());
            d.packageName = i.packageName;
            try {
                d.isUnBlack = sp.getBoolean(i.packageName,OtherUtils.getBlockUninstallForUserReflect(i.packageName,0));
            }catch (Exception u){u.printStackTrace();}

            if((i.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1){
                itemBeans.add(d);
                editor.putBoolean(d.packageName,d.isUnBlack).commit();
            }
        }

        manager = new LinearLayoutManager(getBaseContext());
        myItemRecyclerViewAdapter = new MyItemRecyclerViewAdapter(itemBeans);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(myItemRecyclerViewAdapter);


    }


    private void checkShizukuPermission() {
        
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED){
                // Request the permission
                new MaterialAlertDialogBuilder (MainActivity.this)
                        .setCancelable(false)
                        .setTitle("权限申请")
                        .setMessage("要授权 Shizuku 吗")
                        .setPositiveButton(R.string.lab_submit, (dialog, which) -> {
                            Shizuku.requestPermission(REQUEST_CODE);
                        })
                        .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {
                            dialog.cancel();
                            System.exit(0);
                        }).show();

            }

        }catch (Exception e){
            e.printStackTrace();
            msgW = findViewById(R.id.msgWaning);
            msgW.setPadding(36,12,12,12);
            msgW.setTextSize(42);
            msgW.setText("出现异常：Shizuku 没有运行或没有安装");
        }



//
//                if (AccountManager.get(getBaseContext()).getAccounts().length > 0){
//                    for (Account account: AccountManager.get(getBaseContext()).getAccounts()){
//                        if (account.type.equals("com.google") || account.type.equals("com.xiaomi")){
//                            Log.w(LOG_TAG,"获取到系统类型的账户: "+account.type+", 不做任何动作");
//                        }
//
//                        synchronized(this){
//                            buffer.append("账号名：" + account.name + "\n");
//                            Intent intent1 =   AccountManager.newChooseAccountIntent(null,null,null,null,null,new String[]{},null);
//
//                            Dialog dialog3 =    new MaterialAlertDialogBuilder(MainActivity.this)
//                                    .setMessage("由于Android的限制，请在系统授权对话框中选择账号授权"+msg)
//                                    .setPositiveButton("账号授权页",(dialog, which) -> {
//                                        activityResultLauncher.launch(intent1);
//                                        buffer.setLength(0);
//                                    })
//                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.cancel();
//                                        }
//                                    }).setCancelable(false).create();
//
//                            try {
//                                if (!dialog3.isShowing()){
//                                    //dialog3.cancel();
//                                    Log.w(LOG_TAG,"dialog 没有显示，显示中");
//                                    //dialog3.show();
//                                }
//                            }catch (Exception e){
//                                Log.e(LOG_TAG,e.getMessage());
//                            }
//
//                            break;
//                        }
//
//
//
//                    }
//                }
//                if (!buffer.toString().equals("")){
//                    msg = "\n\n* 注: 下方是已获得授权的账号列表\n\n" + buffer;
//                }


    }


    public void 红红火火恍恍惚惚或或或或或或或或或或或或或或或或或(){
        Uri uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2Fcom.netease.cloudmusic%2Fcache");

        if (getContentResolver().getPersistedUriPermissions().size() > 0){
            for (android.content.UriPermission uriPermission : getContentResolver().getPersistedUriPermissions()) {
                Log.w(LOG_TAG, "Ad目录授权: " + (uriPermission.getUri().getPath().equals(uri.getPath())));

                if (uri.getPath().equals(uriPermission.getUri().getPath())) {
                    try {
                        documentFile = DocumentFile.fromTreeUri(getBaseContext(), Uri.parse(sp.getString("dirUriPermission", "")));
                        for (DocumentFile d : documentFile.listFiles()) {
                            if (d.getName().equals("Ad")){
                                if (d.isDirectory()) {
                                    d.delete();
                                    documentFile.createFile("", "Ad");
                                    ToastUtils.showShort("已替换Ad文件夹");
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    private final ActivityResultLauncher<Intent> activityResultASF = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getData() != null){
                        grantUriPermission(getPackageName(),result.getData().getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(result.getData().getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        editor.putString("dirUriPermission",result.getData().getData().toString()).commit();
                    }

                }
            });

    private final ActivityResultLauncher<Intent> manager_all_files_Result = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (OtherUtils.checkOps(Manifest.permission.MANAGE_EXTERNAL_STORAGE) != 0){
                Log.w(LOG_TAG,"未授权 --> 所有文件管理权限");
            }else {
                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setMessage("我们使用 Apache Commons-IO 库中的 FileAlterationMonitor类监听网易云音乐的广告相关文件并进行删除且在同时创建一个同名文件，以此实现去广告的目的, 由于Google对高版本Android平台的相关限制已解除\n\n您可以选择现在继续监听或停止监听")
                        .setNegativeButton("停止监听", (dialog, which) -> iSwitchBlockUninstall.setRemoveAd(false))
                        .setNeutralButton("继续监听", (dialog, which) -> iSwitchBlockUninstall.setRemoveAd(true))
                        .create().show();
            }
        }
    });



        @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {

        if (grantResult == PermissionChecker.PERMISSION_GRANTED){
            Log.i(LOG_TAG,"已授权 Shizuku");
            editor.putBoolean("是否拿到Shizuku授权",true).commit();
            if (!desc.isShowing() && (!sp.getBoolean("是否在第一次进入应用时显示过关于本应用的说明对话框",false))){
                desc.show();
                editor.putBoolean("是否在第一次进入应用时显示过关于本应用的说明对话框",true).commit();
            }
       }
    }

    public interface ISwitchBlockUninstall{
        void SwitchMsg(String pkgName,boolean blockUninstall);
        String setRemoveAd(boolean z);
        void addPowerSaveForUser(String packageName);
        void removePowerSaveForUser(String packageName);
        boolean isPowerSaveWhitelistApp(String name) throws RemoteException;
    }

    public void setSwitchUninstallBlackInterfaceCallback(ISwitchBlockUninstall iswitchBlockUninstall){
        iSwitchBlockUninstall = iswitchBlockUninstall;
    }


   public static class SettingFm extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            addPreferencesFromResource(R.xml.perf_tips_off);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mainAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
            mainAsyncTask.cancel(true);
            firebaseCrashlytics.setCustomKey("asyncTaskIsCancel",mainAsyncTask.isCancelled());
        }
    }
}
