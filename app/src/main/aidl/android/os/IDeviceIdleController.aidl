// IDeviceIdleController.aidl
package android.os;

// Declare any non-default types here with import statements

interface IDeviceIdleController {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
      void removePowerSaveWhitelistApp(String name);
     String[] getUserPowerWhitelist();

}