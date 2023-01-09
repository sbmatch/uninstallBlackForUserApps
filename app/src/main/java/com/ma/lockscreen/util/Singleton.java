package com.ma.lockscreen.util;

public abstract class Singleton<T> {
    private T mTnstance;

    protected abstract T create();

    public final T get(){
        synchronized (this){
            if (mTnstance == null){
                mTnstance = create();
            }
            return mTnstance;
        }
    }
}
