package com.ma.uninstallBlack.util;

import android.os.Build;
import android.os.FileObserver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;

@RequiresApi(api= Build.VERSION_CODES.Q)
public class MyFileObserver extends FileObserver {

    String mPath;


    public MyFileObserver(@NonNull File file) {
        super(file,FileObserver.CREATE | FileObserver.ACCESS | FileObserver.DELETE_SELF);
        this.mPath =file.getAbsolutePath();
    }

    @Override
    public void onEvent(int event, @Nullable String path) {

        if (event == FileObserver.CREATE){
           if (new File(mPath+"/"+path).isFile()){
               Log.w(MyFileObserver.class.getSimpleName(),"路径:"+mPath+"/"+path+" 原因: 文件被创建");
           }else {
               Log.w(MyFileObserver.class.getSimpleName(),"路径:"+mPath+"/"+path+" 原因: 文件夹被创建");
           }
        }


        if (event == FileObserver.DELETE_SELF){
            Log.w(MyFileObserver.class.getSimpleName(),"路径:"+mPath+"/"+path+" 原因: 自删除");
        }

        if (event == FileObserver.ACCESS){
            Log.w(MyFileObserver.class.getSimpleName(),"此文件被访问："+mPath+"/"+path);
        }
    }
}
