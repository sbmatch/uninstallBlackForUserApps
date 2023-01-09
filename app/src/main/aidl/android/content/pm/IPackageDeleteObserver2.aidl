// IPackageDeleteObserver2.aidl
package android.content.pm;


import android.content.Intent;

// Declare any non-default types here with import statements

oneway interface IPackageDeleteObserver2 {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     void onUserActionRequired(in Intent intent);
     void onPackageDeleted(String packageName, int returnCode, String msg);

}