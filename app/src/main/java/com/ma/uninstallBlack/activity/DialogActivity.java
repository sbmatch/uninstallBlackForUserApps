package com.ma.uninstallBlack.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.blankj.utilcode.util.ActivityUtils;
import com.ma.uninstallBlack.R;

public class DialogActivity extends AppCompatActivity {

     private View dialogview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        dialogview = View.inflate(DialogActivity.this, R.layout.dialog,null);
        TextView textView = dialogview.findViewById(R.id.textView1);
        textView.setText(getIntent().getAction());
        AppCompatButton ok = dialogview.findViewById(R.id.materialButton),no= dialogview.findViewById(R.id.materialButton2);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ActivityUtils.finishAllActivities();
                finish();
                //System.exit(0);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DialogActivity.this, "点击到取消了", Toast.LENGTH_SHORT).show();
                ActivityUtils.finishActivity(DialogActivity.class);
                finish();
            }
        });
        super.onCreate(savedInstanceState);
        setContentView(dialogview);

    }
}