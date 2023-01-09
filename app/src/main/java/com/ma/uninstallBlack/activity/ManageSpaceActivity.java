package com.ma.uninstallBlack.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ma.uninstallBlack.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class ManageSpaceActivity extends AppCompatActivity implements View.OnClickListener{

    private Dialog materialAlertDialogWebView;
    private  WebView webView;
    private  WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private LinearLayout layout = null;


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        org.lsposed.hiddenapibypass.HiddenApiBypass.addHiddenApiExemptions("");
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();

    
        layout = new LinearLayout(this);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.height = 550;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.alpha = 0.0f;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        VideoView videoView = new VideoView(ManageSpaceActivity.this);
        videoView.setId(R.id.videoview);
        videoView.requestFocus();
        videoView.setVideoURI(Uri.parse("https://ghproxy.com/?q=https%3A%2F%2Fgithub.com%2Fsbmatch%2Fprivacypage%2Fblob%2Fmain%2Foutput.mp4"));

        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                //Log.i("怎么事","what:"+what+" "+extra);
                return false;
            }
        });


        Dialog dialog = new MaterialAlertDialogBuilder(this).setTitle("清理中...").setCancelable(false).show();

        videoView.setOnPreparedListener(mp -> {
            mp.start();
            if (mp.isPlaying()){
                dialog.cancel();
                layoutParams.alpha = 1f;
                windowManager.updateViewLayout(layout,layoutParams);
            }
            Toast.makeText(getApplication(), "恭喜你成功被骗", Toast.LENGTH_SHORT).show();
        });
        videoView.setOnCompletionListener(mp -> {
            Log.i("mp",mp.toString());
            mp.stop();
            mp.release();
            Toast.makeText(getApplication(), "好感动~居然看完了", Toast.LENGTH_SHORT).show();
            windowManager.removeView(layout);
            new MaterialAlertDialogBuilder(ManageSpaceActivity.this)
                    .setTitle("你在搞什么飞机啊？")
                    .setMessage("你小子在搞什么飞机啊？？？")
                    .setPositiveButton("大胆！你在狗叫什么？", (dialog1, which) -> finish()).create().show();
        });


        String url =
                "<body>" +
                " <video id=\"video\" loop controls=\"controls\" width=\"100%\" height=\"100%\" >\n" +
                "    <source src=\"https://ghproxy.com/?q=https%3A%2F%2Fgithub.com%2Fsbmatch%2Fprivacypage%2Fblob%2Fmain%2Foutput.mp4\" type=\"video/mp4\"  />\n" +
                "    <object data=\"https://ghproxy.com/?q=https%3A%2F%2Fgithub.com%2Fsbmatch%2Fprivacypage%2Fblob%2Fmain%2Foutput.mp4\" type=\"video/mp4\"  >\n" +
                "    <embed src=\"https://ghproxy.com/?q=https%3A%2F%2Fgithub.com%2Fsbmatch%2Fprivacypage%2Fblob%2Fmain%2Foutput.mp4\" type=\"video/mp4\"  />\n" +
                "    </object>\n" +
                " </video>" +
                "</body>" +
                "<script type=\"text/javascript\">" +
                "  var video = document.getElementById(\"video\");" +
                "  video.play();" +
                "</script>";

        //webView.loadData(url,"text/html","UTF-8");
        //constraintLayout.addView(progressBar);
        //layout.addView(videoView);
        //constraintLayout.addView(btn,ctParams);
       //constraintSet.connect(materialTextView.getId(),ConstraintSet.LEFT,progressBar.getId(),ConstraintSet.RIGHT);
        //constraintSet.applyTo(constraintLayout);

        //initWebSettings(webView);
        //WebViewDialogBuilder(webView,ManageSpaceActivity.this);

        try {
            windowManager.addView(videoView, layoutParams);
        }catch (Exception e){
            Log.e("发生什么事了",e.getMessage());
            //showDialog(getApplication(),e.fillInStackTrace()+"");
        }


        try {
            Object obj = getBaseContext().getSystemService(AUDIO_SERVICE);
            @SuppressLint("PrivateApi")
            Class<?> clazz = Class.forName("android.media.AudioManager");
            @SuppressLint("DiscouragedPrivateApi")
            Method method = clazz.getDeclaredMethod("setParameters", String.class);
            method.setAccessible(true);
            //method.invoke(obj,AudioManager.STREAM_MUSIC,0,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            // 使用反射机制强制静音

            //LogUtils.i(HiddenApiBypass.invoke(clazz,obj,"setStreamVolume",AudioManager.STREAM_MUSIC,23,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE));
        } catch (ClassNotFoundException| RuntimeException | NoSuchMethodException e) {
            Log.e("反射",e.fillInStackTrace()+"");
            logcat("反射");
        } catch (Exception e) {
            e.printStackTrace();
        }


        this.setFinishOnTouchOutside(false);

       /* try {

            webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                    startActivity(new Intent(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_BROWSABLE).setData(Uri.parse(url)));
                }
            });

            webView.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    if (newProgress == 100){
                       Log.i("WebView",view.getOriginalUrl());
                       //materialAlertDialogWebView.show();
                    }
                    super.onProgressChanged(view, newProgress);
                }
            });
        }catch (Exception e){
            Log.e(ManageSpaceActivity.class.getSimpleName(),e.getMessage());
        }*/
    }

    private void initWebSettings(WebView webView) {

        WebSettings webSetting = webView.getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setJavaScriptEnabled(true); //允许运行JavaScript
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setMediaPlaybackRequiresUserGesture(false);
       // webSetting.setAppCacheEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webView.setForceDarkAllowed(true);
        }
    }

    private void WebViewDialogBuilder(View view, Context context) {
        materialAlertDialogWebView = new MaterialAlertDialogBuilder(context)
                //.setIcon(AppCompatResources.getDrawable(context,R.drawable.ic_launcher_foreground))
                .setView(view)
                .setOnDismissListener(dialog -> {
                    finish();
                    Log.i("Dialog",dialog.toString());
                }).create();
    }

    private StringBuilder logcat(String tag){
        StringBuilder buf = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("logcat *:S "+tag+":* -v raw -v tag -b main -d").getInputStream()));
            String line;
            while (( line = bufferedReader.readLine()) != null) {
                buf.append(line).append("\n");
            }
        }catch (Exception e){}

        new AlertDialog.Builder(this)
                .setTitle("Logcat")
                .setCancelable(true)
                .setMessage(buf)
                .show();

        return buf;
    }

    private void showDialog (Context context,String msg){
        new MaterialAlertDialogBuilder(context)
                .setCancelable(true)
                .setMessage(msg)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.down:

                break;
        }
    }
}