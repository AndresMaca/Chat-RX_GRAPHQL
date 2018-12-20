package com.coders.dope.repositories;

import com.coders.dope.database.entity.ChatMessage;

import java.util.List;

public interface NewMessageSubject {
    void register(NewMessagesObserver newMessagesObserver);
    void delete(NewMessagesObserver newMessagesObserver);
    void notifyNewMessagesObserver(ChatMessage chatMessage);
    void notifyInitialMessages(List<ChatMessage> chatMessages);
    void notifyStatus(String status);
}
