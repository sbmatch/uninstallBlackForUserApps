package com.ma.uninstallBlack;


import android.app.Dialog;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ma.uninstallBlack.beans.itemBean;
import com.ma.uninstallBlack.service.MyWorkService;
import com.ma.uninstallBlack.util.MyItemRecyclerViewAdapter;
import com.ma.uninstallBlack.fragment.mViewModel;
import com.ma.uninstallBlack.receiver.MyBroadcastReceiver;
import com.ma.uninstallBlack.util.OtherUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener, Shizuku.OnBinderReceivedListener{

    private Intent intent ;
    private Dialog dialog, desc;
    public static boolean isConn = false;
    private static final int REQUEST_CODE = 1000;
    public static final int JOB_ID = 540128;
    private static Bundle outState;
    public static SharedPreferences sp;
    public static SharedPreferences.Editor editor;
    public static mViewModel viewModel;
    private FragmentManager fm;
    private FragmentTransaction transition;
    public static ContentResolver cr;

    static  String LOG_TAG = MainActivity.class.getSimpleName();
    public static JobScheduler jobScheduler = null;
    private final PackageInfo packageInfo = null;
    public MyItemRecyclerViewAdapter myItemRecyclerViewAdapter;
    public static RecyclerView.LayoutManager manager;
    public static RecyclerView recyclerView;
    public static IAppIPC appIPC;
    public static DocumentFile documentFile;
    public static AlertDialog alertDialog;
    public MainAsyncTask mainAsyncTask = new MainAsyncTask();
    public static Uri uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2Fcom.netease.cloudmusic%2Fcache");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(null);
        getSupportActionBar().setTitle("可卸载管理");

        sp = getSharedPreferences("StartSate",MODE_PRIVATE);
        editor = sp.edit();

        Shizuku.addRequestPermissionResultListener(MainActivity.this);
        Shizuku.addBinderReceivedListener(MainActivity.this);

        desc = new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("关于卸载管理的说明")
                .setMessage(R.string.uninstall_desc)
                .setNegativeButton("确定",null)
                .create();

        recyclerView = findViewById(R.id.recyclerView);

        mainAsyncTask.execute(); //初始化一些数据

        RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        ProgressBar progressBar = new ProgressBar(MainActivity.this,null);
        progressBar.setId(R.id.main_dialog_progress);
        progressBar.setMax(100);
        new Thread(() -> {
            for (int i = 0; i<100; i++){
                progressBar.setProgress(i);
            }
        }).start();
        AppCompatTextView textView = new AppCompatTextView(MainActivity.this);
        textView.setText("正在加载数据");
        textView.setTextSize(20);
        textView.setId(R.id.main_dialog_text);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(24,6,6,6);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params1.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayout.addView(progressBar,params);
        relativeLayout.addView(textView,params1);
        builder.setView(relativeLayout);
        alertDialog = builder.create();

        checkShizukuPermission();


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


//        DateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss.sss", Locale.CHINA);
//        Calendar calendar = Calendar.getInstance();
//        String dateName = df.format(calendar.getTime());
//        String[] logcat_cmd = new String[]{"logcat","-b","main","-t",dateName};
//

       // requestPermissionsNative(this,new String[]{"android.permission.CALL_PHONE"},2);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getTitle().equals("关于卸载")){
            desc.show();
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


    @Override
    protected void onResume() {
        super.onResume();
        Intent w = new Intent(MainActivity.this, MyWorkService.class);
        if (sp.getBoolean("是否拿到Shizuku授权",false)){
            if (!OtherUtils.isServiceRunning(MainActivity.this,MyWorkService.class.getName())){
                Log.i(LOG_TAG,"正在启动并绑定workService ");
                try{
                    this.startForegroundService(w);
                    this.bindService(w,connection,BIND_IMPORTANT);
                }catch (RuntimeException e){
                    e.fillInStackTrace();
                }
            }else {
                Log.i(LOG_TAG,"显示页面");
                show_item();
            }
        }

    }

    public static class MainAsyncTask extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... strings) {

            Looper.prepare();

            Log.i(LOG_TAG,"正在工作线程中初始化数据...");

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //try {
                //@SuppressLint("PrivateApi")
                //Class<?> clazz = Class.forName("android.os");
                //@SuppressLint("DiscouragedPrivateApi")
                //Method method = clazz.getMethod("setAdvertiseIsEnabled",boolean.class);
                //method.setAccessible(true);
                // 使用反射机制强制静音
            //} catch (NullPointerException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {e.printStackTrace();}
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!sp.getBoolean("是否在第一次进入应用时显示过关于本应用的说明对话框",false)){
                editor.putBoolean("是否在第一次进入应用时显示过关于本应用的说明对话框",false).commit();
            }
            editor.putBoolean("是否拿到Shizuku授权",false).commit();
            editor.putBoolean("是否成功获取workService的接口",false).commit();
            editor.putBoolean("Shizuku是否正在运行",false).commit();
            //Toast.makeText(MainActivity.this, "加载数据中...", Toast.LENGTH_SHORT).show();
        }

    }



    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            appIPC = IAppIPC.Stub.asInterface(service);
            try {
                if (!appIPC.isUserServiceBinded()){
                    Log.w(LOG_TAG,"未绑定shizuku userservice 正在绑定");
                    appIPC.bus();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            editor.putBoolean("是否成功获取workService的接口",true).commit();

            //mainAsyncTask.execute();

            show_item();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void show_item() {
        List<itemBean> itemBeans = new ArrayList<itemBean>(){};
        List<PackageInfo> infos = Utils.getApp().getPackageManager().getInstalledPackages(0);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(LOG_TAG,"我是3s后执行");
            }
        };
        new Timer().schedule(timerTask,3000);

        for (PackageInfo i: infos){
            itemBean d =new itemBean();
            d.AppName = i.applicationInfo.loadLabel(Utils.getApp().getPackageManager()).toString();
            d.icon = i.applicationInfo.loadIcon(Utils.getApp().getPackageManager());
            d.packageName = i.packageName;
            try {
                d.isUnBlack = sp.getBoolean(i.packageName,false);
            }catch (Exception ignored){}

            if((i.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1){
                itemBeans.add(d);
            }
        }

        manager = new LinearLayoutManager(Utils.getApp().getBaseContext());
        myItemRecyclerViewAdapter = new MyItemRecyclerViewAdapter(itemBeans);

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(myItemRecyclerViewAdapter);

        for (itemBean ib : itemBeans){
            editor.putBoolean(ib.packageName,ib.isUnBlack).commit();
        }

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
                            if (!sp.getBoolean("是否在第一次进入应用时显示过关于本应用的说明对话框",false)){
                                desc.show();
                                editor.putBoolean("是否在第一次进入应用时显示过关于本应用的说明对话框",true).commit();
                            }
                        })
                        .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {
                            dialog.cancel();
                            System.exit(0);
                        }).show();

            }else{

                alertDialog.show();

                editor.putBoolean("是否拿到Shizuku授权",true).commit();

                Intent intent = new Intent("com.ma.lockscreen.receiver");
                intent.setComponent(new ComponentName(getPackageName(), MyBroadcastReceiver.class.getName()));
                //sendBroadcast(intent);

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
                }else {
                    //activityResultASF.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).putExtra("android.provider.extra.SHOW_ADVANCED",true).putExtra("android.content.extra.SHOW_ADVANCED",true).putExtra(DocumentsContract.EXTRA_INITIAL_URI,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION));
                }
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

        }catch (Exception e){
            e.printStackTrace();
        }
    }



    private final ActivityResultLauncher<Intent> activityResultASF = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    grantUriPermission(getPackageName(),result.getData().getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(result.getData().getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    editor.putString("dirUriPermission",result.getData().getData().toString()).commit();
                }
            });



    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {

        if (grantResult == 0){
            Log.i(LOG_TAG,"已授权 Shizuku");
            editor.putBoolean("是否拿到Shizuku授权",true).commit();
       }
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
    }
}
