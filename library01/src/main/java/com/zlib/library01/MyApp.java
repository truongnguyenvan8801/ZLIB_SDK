package com.zlib.library01;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class MyApp implements DefaultLifecycleObserver {

    private DefaultAppModule defaultAppModule;
    private final Context context;

    public MyApp(Context context) {
        this.context = context;
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        Log.d("MyApp", "start");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        Log.d("MyApp", "stop");
    }
}
