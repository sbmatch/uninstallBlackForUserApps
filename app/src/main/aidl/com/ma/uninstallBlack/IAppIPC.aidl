package com.ma.uninstallBlack;

import android.accounts.Account;

interface IAppIPC {
    String isServiceAvailable(String clazzName);
     boolean removeUserAccount(in Account account);
     boolean isUserServiceBinded();
     boolean isUninstallBlack(String packageName);
     void switchUninstallBlack(String packageName, boolean z);
     void bus();
}