// IPackageManager.aidl
package android.content.pm;

import android.content.pm.VersionedPackage;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstaller;
import android.content.pm.IOnChecksumsReadyListener;

// Declare any non-default types here with import statements

interface IPackageManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
   boolean setBlockUninstallForUser(String packageName, boolean blockUninstall, int userId);

   boolean getBlockUninstallForUser(String packageName, int userId);

   void grantRuntimePermission(String packageName, String permissionName, int userId);

   IPackageInstaller getPackageInstaller();

   ComponentName getInstantAppInstallerComponent();

   void requestPackageChecksums(in String packageName, boolean includeSplits, int optional, int required, in List trustedInstallers, in IOnChecksumsReadyListener onChecksumsReadyListener, int userId);

    /**
     * Delete a package for a specific user.
     *
     * @param versionedPackage The package to delete.
     * @param observer a callback to use to notify when the package deletion in finished.
     * @param userId the id of the user for whom to delete the package
     */
    void deleteExistingPackageAsUser(in VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId);
    void deletePackageVersioned(in VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId, int flags);
}