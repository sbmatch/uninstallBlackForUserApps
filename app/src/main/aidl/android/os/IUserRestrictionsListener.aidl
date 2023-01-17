package android.os;

import android.os.Bundle;

oneway interface IUserRestrictionsListener {
    void onUserRestrictionsChanged(int userId, in Bundle newRestrictions, in Bundle prevRestrictions);
}