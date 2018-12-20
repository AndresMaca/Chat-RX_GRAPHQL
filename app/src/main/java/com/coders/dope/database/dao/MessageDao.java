package com.coders.dope.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.coders.dope.database.entity.ChatMessage;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface MessageDao {
    @Insert(onConflict = REPLACE)
    void save(ChatMessage chatMessage);

    @Query("SELECT * FROM chatMessage WHERE id = :messageId")
    LiveData<ChatMessage> load(String messageId);

    @Query("SELECT * FROM chatMessage")
    List<ChatMessage> loadAllMessagesWithExecutor();

    @Query("SELECT * FROM chatMessage")
    LiveData<List<ChatMessage>> loadAllMessages();

    @Query("SELECT * FROM chatMessage")
    List<ChatMessage> loadAllStartMessages();

    @Query("SELECT * FROM chatMessage LIMIT :lastMessages")
    LiveData<ChatMessage[]> loadLastMessages(int lastMessages);
}
