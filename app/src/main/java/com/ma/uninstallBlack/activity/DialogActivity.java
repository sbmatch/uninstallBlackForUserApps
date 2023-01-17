package com.ma.uninstallBlack.activity;

import static com.ma.uninstallBlack.MainActivity.sp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ma.uninstallBlack.R;

public class DialogActivity extends AppCompatActivity {


    private SharedPreferences.Editor editor;
    private static AlertDialog dialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        if (editor == null){
            SharedPreferences sp = getSharedPreferences("StartSate", MODE_PRIVATE);
            editor = sp.edit();
        }

        View dialogview = View.inflate(getApplicationContext(), R.layout.dialog,null);

        TextView textView = dialogview.findViewById(R.id.textView1);
        textView.setVerticalScrollBarEnabled(true);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        String crashT = getIntent().getAction();
        textView.setText("请将此日志发送给开发者用于修复此问题, 我们非常期待您的反馈 \n\n"+crashT);
        AlertDialog.Builder builder = new AlertDialog.Builder(DialogActivity.this);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_baseline_bug_report_24);
        builder.setTitle("阿偶 发生崩溃了:(");
        builder.setView(dialogview).setNegativeButton("确定", (dialog, which) -> {
            editor.putString("崩溃",null).commit();
            editor.putBoolean("对话框显示状态",false).commit();
            finish();
        });

        editor.putBoolean("对话框显示状态",false).commit();

        dialog = builder.create();

        if (!sp.getBoolean("对话框显示状态",false)){
            if (!dialog.isShowing()){
                dialog.show();
                editor.putBoolean("对话框显示状态",true).commit();
            }
        }
        super.onCreate(savedInstanceState);
        //setContentView(dialogview);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sp.getBoolean("对话框显示状态",false)){
            if (dialog.isShowing()){
                dialog.cancel();
                editor.putBoolean("对话框显示状态",false).commit();
                DialogActivity.this.finish();
                //System.exit(0);
            }
        }
    }
}