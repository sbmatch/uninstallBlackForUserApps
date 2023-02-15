package com.ma.uninstallBlack.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import rikka.shizuku.Shizuku;

public class ShizukuExecUtils {
    private static Process p;
    private static Thread h1, h2, h3;
    private static String inline;

    public ShizukuExecUtils(){

    }
    public static String ShizukuExec(String cmd) {
        try {
            p = Shizuku.newProcess(new String[]{"sh"}, null, null);
            OutputStream out = p.getOutputStream();
            out.write((cmd+"\nexit\n").getBytes());
            out.flush();
            out.close();
            h2 = new Thread(() -> {
                try {
                    BufferedReader mReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while ((inline = mReader.readLine()) != null) {
                       Log.i("InputStream",inline);
                    }
                    mReader.close();
                } catch (Exception ignored) {
                }
            });
            h2.start();
            h3 = new Thread(() -> {

                try {
                    BufferedReader mReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    while ((inline = mReader.readLine()) != null) {
                        Log.e("ErrorStream",inline);
                    }
                    mReader.close();
                } catch (Exception ignored) {}

            });
            h3.start();
            p.waitFor();

            return inline;
            //String exitValue = String.valueOf(p.exitValue());
            //Log.i("",String.format("返回值：%s", exitValue));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return inline;
    }
}
