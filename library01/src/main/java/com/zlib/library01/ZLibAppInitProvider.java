package com.zlib.library01;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.startup.Initializer;

import java.util.Collections;
import java.util.List;

public class ZLibAppInitProvider implements Initializer<MyApp> {

    @NonNull
    @Override
    public MyApp create(@NonNull Context context) {
        MyApp myApp = new MyApp(context);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(myApp);
        return myApp;
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
