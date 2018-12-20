package com.coders.dope.di.module;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.ResponseField;
import com.apollographql.apollo.cache.normalized.CacheKey;
import com.apollographql.apollo.cache.normalized.CacheKeyResolver;
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy;
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper;
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.coders.dope.database.MessagesDatabase;
import com.coders.dope.database.dao.MessageDao;
import com.coders.dope.repositories.MessagesRepository;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import static com.coders.dope.api.NetworkEndpoints.BASE_URL;
import static com.coders.dope.api.NetworkEndpoints.SUBSCRIPTION_BASE_URL;

@Module(includes = ViewModelModule.class)
public class AppModule {
    private static final String TAG = AppModule.class.getSimpleName();
    private static final String SQL_CACHE_NAME = "githuntdb";
    @Provides
    @Singleton
    MessagesDatabase provideDatabase(Application application) {
        return Room.databaseBuilder(application, MessagesDatabase.class, "MessageDatabase.db").build();

    }

    @Provides
    @Singleton
    MessageDao provideMessageDao(MessagesDatabase messagesDatabase) {
        return messagesDatabase.messageDao();
    }



    @Provides
    @Singleton
    Executor provideExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    private static final String CHAT_SERVER_URL = "http://192.168.0.16:3000/"; //Todo make Constant Class


    //Graphql

    @Singleton
    @Provides
    NormalizedCacheFactory providesNormalizedCacheFactory(Application application){
        return  new LruNormalizedCacheFactory(EvictionPolicy.NO_EVICTION)
                .chain(new SqlNormalizedCacheFactory(new ApolloSqlHelper(application, SQL_CACHE_NAME)));


    }
    @Provides
    @Singleton
    CacheKeyResolver providesCacheKeyResolver(){
        return new CacheKeyResolver() {
            @NotNull
            @Override
            public CacheKey fromFieldRecordSet(@NotNull ResponseField field, @NotNull Map<String, Object> recordSet) {
                String typeName = (String) recordSet.get("__typename");
                if ("User".equals(typeName)) {
                    String userKey = typeName + "." + recordSet.get("login");
                    return CacheKey.from(userKey);
                }
                if (recordSet.containsKey("id")) {
                    String typeNameAndIDKey = recordSet.get("__typename") + "." + recordSet.get("id");
                    return CacheKey.from(typeNameAndIDKey);
                }
                return CacheKey.NO_KEY;
            }

            // Use this resolver to customize the key for fields with variables: eg entry(repoFullName: $repoFullName).
            // This is useful if you want to make query to be able to resolved, even if it has never been run before.
            @NotNull @Override
            public CacheKey fromFieldArguments(@NotNull ResponseField field, @NotNull Operation.Variables variables) {
                return CacheKey.NO_KEY;
            }
        };
    }

    @Provides
    OkHttpClient providesOkHttpClient(){
        return new OkHttpClient.Builder()
                .build();
    }
    @Singleton
    @Provides
    ApolloClient providesApolloClient(OkHttpClient okHttpClient, NormalizedCacheFactory normalizedCacheFactory, CacheKeyResolver cacheKeyResolver){
        Log.d(TAG,"Apollo created");
        return ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .normalizedCache(normalizedCacheFactory, cacheKeyResolver)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(SUBSCRIPTION_BASE_URL, okHttpClient))
                .build();
    }

    @Provides
    @Singleton
    MessagesRepository provideMessageRepository(Executor executor, MessageDao messageDao, ApolloClient apolloClient){
        return new MessagesRepository(executor,messageDao, apolloClient);

    }

}
