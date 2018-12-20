package com.coders.dope.di.module;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.coders.dope.di.key.ViewModelKey;
import com.coders.dope.models.FactoryViewModel;
import com.coders.dope.models.MessagesViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MessagesViewModel.class)
    abstract ViewModel bindMessagesViewModel(MessagesViewModel messagesViewModel);
    @Binds
    abstract ViewModelProvider.Factory factory(FactoryViewModel factoryViewModel);
}
