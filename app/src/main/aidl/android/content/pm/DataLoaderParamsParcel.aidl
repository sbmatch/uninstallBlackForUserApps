package android.content.pm;

import android.content.pm.DataLoaderType;

/**
 * Class for holding data loader configuration parameters.
 * @hide
 */
parcelable DataLoaderParamsParcel {

    String packageName;
    String className;
    String arguments;
}