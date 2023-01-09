/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.accounts;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;

/**
 * Central application service that provides account management.
 * @hide
 */
interface IAccountManager {

 boolean removeAccountExplicitly(in Account account);
 void removeAccountAsUser(in IAccountManagerResponse response, in Account account,boolean expectActivityLaunch, int userId);
  Account[] getAccountsAsUser(String accountType, int userId, String opPackageName);
   Account[] getAccounts ();
  Account[] getAccountsForPackage(String packageName, int uid, String opPackageName);
  Map getAccountsAndVisibilityForPackage(in String packageName, in String accountType);
  Map getPackagesAndVisibilityForAccount(in Account account);
  boolean setAccountVisibility(in Account a, in String packageName, int newVisibility);
    int getAccountVisibility(in Account a, in String packageName);
    void registerAccountListener(in String[] accountTypes, String opPackageName);
    void unregisterAccountListener(in String[] accountTypes, String opPackageName);
}