package com.coders.dope.repositories;

import com.coders.dope.database.entity.ChatMessage;

import java.util.List;

public interface NewMessagesObserver {
    void update(ChatMessage newChatMessage);
    void loadInitialMessages(List<ChatMessage> chatMessages);
    void updateChatStatus(String socketStatus);

    void initialState(String status);
}
