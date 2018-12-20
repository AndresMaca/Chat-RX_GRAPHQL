package com.coders.dope;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.coders.dope.di.component.DaggerAppComponent;
import com.coders.dope.repositories.MessagesRepository;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

public class App extends Application implements HasActivityInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;
    public static Context context;

    @Inject
    MessagesRepository messagesRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        this.initDagger();
        context= getApplicationContext();
    }
    void initDagger(){
        DaggerAppComponent.builder().application(this).build().inject(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    public void onTerminate() {
        messagesRepository.closeConnection();
        super.onTerminate();
    }
}
