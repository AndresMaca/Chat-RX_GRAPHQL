package com.coders.dope.di.module;

import com.coders.dope.activities.Main2Activity;
import com.coders.dope.activities.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract MainActivity contributesMainActivity();
    @ContributesAndroidInjector
    abstract Main2Activity contributesMain2Activity();
}
