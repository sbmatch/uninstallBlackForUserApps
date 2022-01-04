package com.ma.powersoundswitch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class mViewModel extends ViewModel {

    private MutableLiveData<String> callString = new MutableLiveData<>();

    public LiveData<String> getCallString()
    {
        if (callString == null)
        {
            callString = new MutableLiveData<>();
        }
        return callString;
    }


    public void add(String str){
        callString.setValue(str);
    }

}
