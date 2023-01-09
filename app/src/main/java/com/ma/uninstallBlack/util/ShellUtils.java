package com.ma.uninstallBlack.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellUtils {

    public ShellUtils() {

    }

    /**
     * @param cmd
     *
     * 执行命令
     * @return*/

    public static StringBuilder exec(String cmd){

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();

        try {
            process = Runtime.getRuntime().exec(cmd);
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while (( line = successResult.readLine()) != null) {
                successMsg.append(line).append("\n");
            }
            while (( line = errorResult.readLine()) != null) {
                errorMsg.append(line).append("\n");
            }

        }catch (Exception e){
            Log.e("error",e.getLocalizedMessage());
        }finally {
            try {
                assert errorResult != null;
                errorResult.close();
                successResult.close();
                process.destroy();
            }catch (IOException ignored){}
        }
        return successMsg;
    }
}
