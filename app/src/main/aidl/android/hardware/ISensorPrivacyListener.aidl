// ISensorPrivacyListener.aidl
package android.hardware;

// Declare any non-default types here with import statements

interface ISensorPrivacyListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onSensorPrivacyChanged(boolean z);
}