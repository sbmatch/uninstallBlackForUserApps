// IDeviceIdleController.aidl
package android.os;

// Declare any non-default types here with import statements

interface IDeviceIdleController {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     void addPowerSaveWhitelistApp(String name);
     int addPowerSaveWhitelistApps(in List<String> packageNames);
     void removePowerSaveWhitelistApp(String name);
     boolean isPowerSaveWhitelistExceptIdleApp(String name);
     boolean isPowerSaveWhitelistApp(String name);
     String[] getUserPowerWhitelist();

}