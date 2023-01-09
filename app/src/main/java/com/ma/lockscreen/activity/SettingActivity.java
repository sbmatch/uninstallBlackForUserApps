package com.ma.lockscreen.activity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.AppUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ma.lockscreen.R;
import com.ma.lockscreen.fragment.mViewModel;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;


public class SettingActivity extends AppCompatActivity {
    private FragmentManager fm;
    private FragmentTransaction transition;
    private mViewModel viewModel;
    //private AdRequest adRequest;
    private final static String adTestId = "ca-app-pub-3940256099942544/1033173712" ;//"ca-app-pub-3940256099942544/5224354917";
    private final static String adId = "ca-app-pub-6149360771976686/1984751116";
    private final String TAG = "SettingActivity";
    private View view;
    private MaterialAlertDialogBuilder builder;
    private TextView textView;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        List<String> testDeviceIds = Collections.singletonList("9C27F8EF3B9F2CC46D3EEE1867818A91");
        //RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        //MobileAds.setRequestConfiguration(configuration);

        //adRequest = new AdRequest.Builder().build();
/*
        MobileAds.initialize(getBaseContext(), initializationStatus -> {
            try {
                Runtime.getRuntime().exec("whoami").getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("Ads", "这是测试设备吗？： "+new AdRequest.Builder().build().isTestDevice(getBaseContext()));
        });*/


       /* TTAdSdk.init(this, new TTAdConfig.Builder().appId("5261678").allowShowNotify(false).asyncInit(true).supportMultiProcess(true).directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI).build(), new TTAdSdk.InitCallback() {
            @Override
            public void success() {
                LogUtils.e("完成初始化");
            }

            @Override
            public void fail(int i, String s) {

            }
        });


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (AdvertisingIdClient.isAdvertisingIdProviderAvailable(getBaseContext())){
                            LogUtils.i(AdvertisingIdClient.getAdvertisingIdInfo(getBaseContext()).get().getId());
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        ToastUtils.showLong(e.getLocalizedMessage());
                    }

                }
            }).start();

        */

        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting);

        ActionBar actionBar = getSupportActionBar();
        setActionBar(actionBar);

        //HiddenApiBypass.addHiddenApiExemptions("");

        fm = getSupportFragmentManager();
        transition = fm.beginTransaction();

        viewModel = new ViewModelProvider(this).get(mViewModel.class);

        view = View.inflate(getBaseContext(),R.layout.dialog,null);
        textView = view.findViewById(R.id.textView1);

        setTextViewFlag(textView);
    }


    private void setActionBar(ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(AppUtils.getAppName());
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void setTextViewFlag(TextView textView) {
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setFocusable(true);
        textView.setTextIsSelectable(true);
        textView.setLongClickable(true);
        textView.setEnabled(true);
        textView.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            viewModel.add(ContentUriUtil.getPath(this,data.getData()));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent().setComponent(new ComponentName("com.miui.home","com.miui.home.launcher.Launcher")));
        //ActivityUtils.startHomeActivity();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    AppUtils.exitApp();
                    System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        //menu.add(0,1,0,"点我看广告").setIcon(R.drawable.ic_baseline_card_giftcard_24).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        //menu.add(0,3,0,"powerManger").setIcon(R.drawable.ic_baseline_update_24).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

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


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        /*

        if (id == 1) {

            //ToastUtils.showShort("广告加载中...");


            InterstitialAd.load(SettingActivity.this, adTestId, adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    new MaterialAlertDialogBuilder(SettingActivity.this)
                            .setCancelable(false)
                            .setMessage("这将浪费你几秒时间")
                            .setPositiveButton(R.string.lab_submit, (dialog, which) -> {
                                if (interstitialAd.getResponseInfo().getResponseId() != null) {
                                    interstitialAd.show(getParent());
                                }
                            })
                            .setNegativeButton(R.string.lab_cancel, (dialog, which) -> { })
                            .show();
                    super.onAdLoaded(interstitialAd);
                }
            });

            RewardedAd.load(SettingActivity.this, adTestId, adRequest, new RewardedAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                    StringBuilder buf = new StringBuilder();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("logcat *:S Ads:* -v raw -v tag -b main -d").getInputStream()));
                        String line;
                        while (( line = bufferedReader.readLine()) != null) {
                            buf.append(line).append("\n");
                        }
                    }catch (Exception e){LogUtils.e(e.getMessage());}


                    new MaterialAlertDialogBuilder(SettingActivity.this)
                            .setCancelable(true)
                            .setMessage(buf)
                            .show();
                   // 穿山甲();
                    super.onAdFailedToLoad(loadAdError);
                }

                @Override
                public void onAdLoaded(@NonNull RewardedAd rewardedAd) {

                    new MaterialAlertDialogBuilder(SettingActivity.this)
                            .setCancelable(false)
                            .setMessage("这将浪费你几秒时间")
                            .setPositiveButton(R.string.lab_submit, (dialog, which) -> {
                                try {
                                    if (rewardedAd.getResponseInfo().getResponseId() != null) {
                                        rewardedAd.show(getParent(), rewardItem -> ToastUtils.showShort("观看广告(1/1) 已完成"));
                                    }
                                }catch (NullPointerException e){
                                    LogUtils.e(e.fillInStackTrace());
                                }
                            })
                            .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {
                            }).show();
                    super.onAdLoaded(rewardedAd);
                }
            });

            return true;
        }
        */


        /*
        if (id == 3){

            view  = View.inflate(getBaseContext(),R.layout.dialog,null);

            ListAdapter listAdapter = new ArrayAdapter<Objects>(getBaseContext(),R.layout.dialog,R.id.textView1,HiddenApiBypass.getDeclaredMethods(PowerManager.class));

            new MaterialAlertDialogBuilder(this)
                    .setView(view)
                    .setAdapter(listAdapter, (dialog, which) -> {
                        LogUtils.i("第"+which+"条"+": "+listAdapter.getItem(which).toString());
                        //ClipboardUtils.copyText(listAdapter.getItem(which).toString());
                        ToastUtils.showLong(listAdapter.getItem(which).toString());
                    })
                    //.show()
            ;

            try {

                PowerManager manager = (PowerManager) getSystemService(POWER_SERVICE);
                Method method = HiddenApiBypass.getDeclaredMethod(manager.getClass(),
                        //"isIgnoringBatteryOptimizations",
                        "isInteractive",
                        new Class[]{});
                //Boolean obj = (Boolean) method.invoke(manager,BuildConfig.APPLICATION_ID);
                Boolean obj = (Boolean) method.invoke(manager);
                //int obj = (int) method.invoke(manager);
                //method.invoke(manager,null);
                LogUtils.i("设备是否亮屏："+obj);
                Toast.makeText(getBaseContext(), "设备是否亮屏："+obj, Toast.LENGTH_SHORT).show();

            }catch (Exception e){
                LogUtils.e(e.getMessage());
            }
        }

         */
            return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();

    }


    private void mAtDialog(String systemService,View view) {
        builder = new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setView(view)
                .setTitle(systemService)
                .setNegativeButton(R.string.lab_submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        Dialog dialog = builder.show();
        dialog.show();
    }
/*

    private void 穿山甲() {

        TTAdNative mTTAdNative=TTAdSdk.getAdManager().createAdNative(this);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(String.valueOf(947626923))
                .setRewardName("广告") //奖励的名称 选填
                .setRewardAmount(1)  //奖励的数量 选填
                .setUserID("test1")//tag_id
                .setMediaExtra("media_extra") //附加参数
                .setOrientation(TTAdConstant.VERTICAL) //必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .setAdLoadType(TTAdLoadType.PRELOAD)//推荐使用，用于标注此次的广告请求用途为预加载（当做缓存）还是实时加载，方便后续为开发者优化相关策略
                .build();

        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ttRewardVideoAd) {
                ttRewardVideoAd.showRewardVideoAd(SettingActivity.this,TTAdConstant.RitScenes.HOME_OPEN_BONUS, "scenes_test");

            }

            @Override
            public void onRewardVideoCached() {

            }

            @Override
            public void onRewardVideoCached(TTRewardVideoAd ttRewardVideoAd) {

            }
        });
    }
*/
}
