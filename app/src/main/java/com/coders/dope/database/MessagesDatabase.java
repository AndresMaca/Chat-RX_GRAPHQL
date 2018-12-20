package com.coders.dope.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.coders.dope.database.converter.DateConverter;
import com.coders.dope.database.dao.MessageDao;
import com.coders.dope.database.entity.ChatMessage;

@Database(entities = {ChatMessage.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class MessagesDatabase extends RoomDatabase{
    private static volatile MessagesDatabase INSTANCE;
    public abstract MessageDao messageDao();
}
