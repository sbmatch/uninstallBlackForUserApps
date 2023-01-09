package android.content.pm;

import android.content.pm.ApkChecksum;

/**
 * Listener that gets notified when checksums are available.
 * {@hide}
 */
oneway interface IOnChecksumsReadyListener {
    void onChecksumsReady(in List<ApkChecksum> checksums);
}