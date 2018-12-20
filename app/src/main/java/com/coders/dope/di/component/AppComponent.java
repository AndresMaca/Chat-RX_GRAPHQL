package com.coders.dope.di.component;

import android.app.Application;

import com.coders.dope.App;
import com.coders.dope.di.module.ActivityModule;
import com.coders.dope.di.module.AppModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Singleton
@Component(modules = {AndroidInjectionModule.class, AppModule.class, ActivityModule.class})
public interface AppComponent {
    @Component.Builder
    interface Builder{
        @BindsInstance
        Builder application(Application application);
        AppComponent build();

    }
    void inject(App app);
}
